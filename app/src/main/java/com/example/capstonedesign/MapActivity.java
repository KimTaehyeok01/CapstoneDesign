package com.example.capstonedesign;

import android.Manifest;
import android.content.SharedPreferences;
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
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private EditText searchBar;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private FrameLayout btnMyLocationContainer;

    private View placeInfoContainer;
    private TextView placeNameTextView, placeAddressTextView, placePhoneTextView;
    private ImageButton btnFavorite, btnHidePlaceInfo;

    private boolean isFavorite = false;
    private String currentPlaceName = "";
    private String currentPlaceAddress = "";
    private String currentPlacePhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, renderer -> {});

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

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

        btnMyLocationContainer = findViewById(R.id.btnMyLocationContainer);
        btnMyLocationContainer.setOnClickListener(view -> {
            // ✅ 위치 설정 꺼져있으면 기능 차단
            SharedPreferences prefs = getSharedPreferences("PushSettingsPrefs", MODE_PRIVATE);
            boolean isLocationOn = prefs.getBoolean("location_on", true);
            if (!isLocationOn) {
                Toast.makeText(this, "위치 서비스가 꺼져 있어 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            ViewPropertyAnimator animator = view.animate();
            animator.scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(() -> {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }).start();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

        placeInfoContainer = findViewById(R.id.placeInfoContainer);
        placeNameTextView = findViewById(R.id.placeNameTextView);
        placeAddressTextView = findViewById(R.id.placeAddressTextView);
        placePhoneTextView = findViewById(R.id.placePhoneTextView);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnHidePlaceInfo = findViewById(R.id.btnHidePlaceInfo);

        btnHidePlaceInfo.setOnClickListener(v -> placeInfoContainer.setVisibility(View.GONE));

        btnFavorite.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();
            DocumentReference favRef = db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(currentPlaceName);

            if (!isFavorite) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", currentPlaceName);
                data.put("address", currentPlaceAddress);
                data.put("phone", currentPlacePhone);

                favRef.set(data).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "찜 추가됨", Toast.LENGTH_SHORT).show();
                    btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
                    isFavorite = true;
                });
            } else {
                favRef.delete().addOnSuccessListener(unused -> {
                    Toast.makeText(this, "찜 해제됨", Toast.LENGTH_SHORT).show();
                    btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                    isFavorite = false;
                });
            }
        });
    }

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

        // 지도 초기 로딩 시 위치 설정 확인
        SharedPreferences prefs = getSharedPreferences("PushSettingsPrefs", MODE_PRIVATE);
        boolean isLocationOn = prefs.getBoolean("location_on", true);
        if (!isLocationOn) {
            LatLng defaultLocation = new LatLng(37.5665, 126.9780);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
            Toast.makeText(this, "위치 서비스가 꺼져 있어 기본 위치로 이동합니다.", Toast.LENGTH_SHORT).show();
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

        mMap.setOnMarkerClickListener(marker -> {
            String clickedPlaceName = marker.getTitle();

            db.collection("sports_locations")
                    .whereEqualTo("name", clickedPlaceName)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            var doc = queryDocumentSnapshots.getDocuments().get(0);
                            currentPlaceName = doc.getString("name");
                            currentPlaceAddress = doc.getString("address");
                            currentPlacePhone = doc.getString("phone");

                            placeNameTextView.setText(currentPlaceName != null ? currentPlaceName : "이름 없음");
                            placeAddressTextView.setText(currentPlaceAddress != null ? currentPlaceAddress : "주소 정보 없음");
                            placePhoneTextView.setText(currentPlacePhone != null ? currentPlacePhone : "전화번호 없음");

                            placeInfoContainer.setVisibility(View.VISIBLE);

                            if (currentUser != null) {
                                String userId = currentUser.getUid();
                                db.collection("users")
                                        .document(userId)
                                        .collection("favorites")
                                        .document(currentPlaceName)
                                        .get()
                                        .addOnSuccessListener(snapshot -> {
                                            if (snapshot.exists()) {
                                                isFavorite = true;
                                                btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
                                            } else {
                                                isFavorite = false;
                                                btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                                            }
                                        });
                            }
                        }
                    });

            return false;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
            }
        }
    }
}
