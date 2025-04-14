package com.example.capstonedesign;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
    // 내 위치 버튼 컨테이너 (애니메이션 적용)
    private FrameLayout btnMyLocationContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map); // res/layout/map.xml 사용

        // Firestore 인스턴스 초기화
        db = FirebaseFirestore.getInstance();
        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 구글 맵 초기화 (최신 렌더러 사용)
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, renderer -> {});

        // MapFragment 참조 및 지도 초기화
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 검색창 연결 및 검색 이벤트 처리
        searchBar = findViewById(R.id.search_bar);
        searchBar.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String query = searchBar.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchLeisureSports(query.toLowerCase());
                }
                return true;
            }
            return false;
        });

        ImageButton btnMapSearchGlass = findViewById(R.id.btnMapSearchGlass);
        btnMapSearchGlass.setOnClickListener(view -> {
            String query = searchBar.getText().toString().trim();
            if (!query.isEmpty()) {
                searchLeisureSports(query.toLowerCase());
            }
        });

        // 내 위치 버튼 컨테이너 연결 및 애니메이션 효과 추가 (눌리는 느낌)
        btnMyLocationContainer = findViewById(R.id.btnMyLocationContainer);
        btnMyLocationContainer.setOnClickListener(view -> {
            // 눌리는 애니메이션: 약간 축소된 후 복귀
            ViewPropertyAnimator animator = view.animate();
            animator.scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }).start();

            // 내 위치로 카메라 이동 (애니메이션 효과)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.clear();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17), 1000, null);
                        mMap.addMarker(new MarkerOptions().position(myLocation).title("내 위치"));
                    } else {
                        Toast.makeText(this, "현재 위치를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        });
    }

    // Firestore에서 'sports_locations' 장소를 검색하여 마커를 지도에 표시하는 메서드
    private void searchLeisureSports(String searchQuery) {
        db.collection("sports_locations")
                .orderBy("name")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        mMap.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Double lat = doc.getDouble("latitude");
                            Double lng = doc.getDouble("longitude");
                            String placeName = doc.getString("name");
                            if (lat != null && lng != null && placeName != null) {
                                LatLng placeLatLng = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeName));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng, 15), 1000, null);
                            } else {
                                Log.d("Firestore", "누락된 필드: " + doc.getId());
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 17), 1000, null);
                mMap.addMarker(new MarkerOptions().position(myLocation).title("내 위치"));
            } else {
                LatLng defaultLocation = new LatLng(37.5665, 126.9780);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                recreate();
            }
        }
    }
}
