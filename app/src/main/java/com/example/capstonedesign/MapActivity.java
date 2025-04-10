package com.example.capstonedesign;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

// Firestore 관련 import
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // 검색창
    private EditText searchBar;

    // Firestore 인스턴스
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map); // res/layout/map.xml 사용

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();

        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 구글맵 초기화 (최신 렌더러 사용)
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, renderer -> {});

        // MapFragment 참조 및 지도 초기화
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 검색창 연결
        searchBar = findViewById(R.id.search_bar);

        // 키보드의 검색 버튼(돋보기)이나 엔터 키를 누르면 이벤트 처리
        searchBar.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                String query = searchBar.getText().toString().trim();
                if (!query.isEmpty()) {
                    // 검색 시 Firestore에서 해당 장소 접두어를 검색 (소문자로 변환)
                    searchLeisureSports(query.toLowerCase());
                }
                return true;
            }
            return false;
        });
    }

    // 접두어 검색 방식으로 Firestore에서 "sports_locations" 컬렉션의 문서를 검색하는 메서드
    private void searchLeisureSports(String searchQuery) {
        // Firestore 쿼리: "name" 필드를 기준으로 정렬한 후, 검색어로 시작하는 문서를 찾음
        db.collection("sports_locations")
                .orderBy("name")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // 지도에 기존 마커 삭제 후, 검색 결과 마커 표시
                        mMap.clear();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            // 문서에 "latitude", "longitude", "name" 필드가 존재한다고 가정
                            Double lat = doc.getDouble("latitude");
                            Double lng = doc.getDouble("longitude");
                            String placeName = doc.getString("name");

                            // 값이 null이 아닌지 확인 후 마커 추가
                            if (lat != null && lng != null && placeName != null) {
                                LatLng placeLatLng = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeName));
                                // 첫 검색 결과 기준 카메라 이동 (필요에 따라 여러 결과 처리 가능)
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15));
                            } else {
                                Log.d("Firestore", "문서 필드 누락: " + doc.getId());
                            }
                        }
                    } else {
                        Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "검색 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 위치 권한 체크
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // 내 위치 아이콘 활성화
        mMap.setMyLocationEnabled(true);

        // 현재 위치 가져오기 및 지도에 표시
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17));
                        mMap.addMarker(new MarkerOptions().position(myLocation).title("내 위치"));
                    } else {
                        // 위치 정보를 가져올 수 없으면 기본 좌표(서울시청)로 이동
                        LatLng defaultLocation = new LatLng(37.5665, 126.9780);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
                    }
                });
    }

    // 위치 권한 요청 결과 처리: 권한 허용 시 액티비티 재시작하여 지도 로딩
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
            }
        }
    }
}
