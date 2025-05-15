package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.capstonedesign.settings_information.SettingsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoriteListActivity extends AppCompatActivity {

    private static final String TAG = "FavoriteListActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private LinearLayout itemContainer;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FusedLocationProviderClient fusedLocationClient;

    private EditText editSearch;
    private ImageButton btnSearchGlass, btnCancel;

    // 하단 네비게이션 버튼
    private ImageButton navSearch, navMarker, navHome, navHeart, navSetting;

    // 모든 카드 뷰를 보관할 리스트
    private final List<View> allCards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        // 뷰 바인딩
        itemContainer   = findViewById(R.id.item_container);
        editSearch      = findViewById(R.id.editSearch);
        btnSearchGlass  = findViewById(R.id.btnSearchGlass);
        btnCancel       = findViewById(R.id.btnCancel);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 검색 버튼 클릭
        btnSearchGlass.setOnClickListener(v -> performSearch());

        // 키보드 검색 버튼 처리
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        // 취소 버튼 클릭: 검색어 클리어 후 원래 목록 복원
        btnCancel.setOnClickListener(v -> clearSearch());

        // 정렬 버튼 리스너
        findViewById(R.id.btn_sort_location).setOnClickListener(v -> sortByName());
        findViewById(R.id.btn_sort_distance).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        REQUEST_LOCATION_PERMISSION);
            } else {
                sortByDistance();
            }
        });

        // 네비게이션 바 버튼 바인딩 및 클릭 처리
        navSearch  = findViewById(R.id.nav_search);
        navMarker  = findViewById(R.id.nav_marker);
        navHome    = findViewById(R.id.nav_home);
        navHeart   = findViewById(R.id.nav_heart);
        navSetting = findViewById(R.id.nav_setting);

        navSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        navMarker.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        navHome.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
        navHeart.setOnClickListener(v -> {
            Intent i = new Intent(this, FavoriteListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
        navSetting.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        // Firebase 및 위치 서비스 초기화
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (currentUser != null) {
            loadFavoriteItems();
        } else {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            boolean granted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                sortByDistance();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadFavoriteItems() {
        String userId = currentUser.getUid();
        allCards.clear();
        itemContainer.removeAllViews();

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(favSnap -> {
                    List<DocumentSnapshot> favDocs = favSnap.getDocuments();
                    if (favDocs.isEmpty()) {
                        Toast.makeText(this, "찜한 장소가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot favDoc : favDocs) {
                        String name    = safe(favDoc.get("name"),    "장소명 없음");
                        String address = safe(favDoc.get("address"), "주소 없음");

                        CollectionReference locRef = db.collection("sports_locations");
                        locRef.whereEqualTo("name", name)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(locSnap -> {
                                    String region   = "지역 없음";
                                    String price    = "가격 정보 없음";
                                    String imageUrl = "";
                                    double latitude = 0.0, longitude = 0.0;

                                    if (!locSnap.isEmpty()) {
                                        DocumentSnapshot locDoc = locSnap.getDocuments().get(0);
                                        region   = safe(locDoc.get("topic"),   region);
                                        price    = safe(locDoc.get("details"), price);
                                        imageUrl = safe(locDoc.get("image"),   "");
                                        if (locDoc.get("latitude") instanceof Number) {
                                            latitude = ((Number) locDoc.get("latitude")).doubleValue();
                                        }
                                        if (locDoc.get("longitude") instanceof Number) {
                                            longitude = ((Number) locDoc.get("longitude")).doubleValue();
                                        }
                                    }

                                    View card = LayoutInflater.from(this)
                                            .inflate(R.layout.item_favorite_card, itemContainer, false);

                                    ImageView img      = card.findViewById(R.id.img_place);
                                    TextView  tvName   = card.findViewById(R.id.tv_place_name);
                                    TextView  tvAddr   = card.findViewById(R.id.tv_place_address);
                                    TextView  tvRegion = card.findViewById(R.id.tv_place_region);
                                    TextView  tvPrice  = card.findViewById(R.id.tv_place_price);
                                    ImageView btnFav   = card.findViewById(R.id.img_favorite);

                                    tvName.setText(name);
                                    tvAddr.setText(address);
                                    tvRegion.setText(region);
                                    tvPrice.setText(price);

                                    card.setTag(new double[]{ latitude, longitude });

                                    if (!imageUrl.isEmpty()) {
                                        if (imageUrl.startsWith("gs://")) {
                                            img.setImageResource(R.drawable.ic_climb);
                                        } else {
                                            Glide.with(this).load(imageUrl).into(img);
                                        }
                                    } else {
                                        img.setImageResource(R.drawable.ic_climb);
                                    }

                                    btnFav.setOnClickListener(v -> {
                                        db.collection("users")
                                                .document(userId)
                                                .collection("favorites")
                                                .document(name)
                                                .delete()
                                                .addOnSuccessListener(u -> {
                                                    Toast.makeText(this, "찜 목록에서 제거되었습니다.", Toast.LENGTH_SHORT).show();
                                                    allCards.remove(card);
                                                    itemContainer.removeView(card);
                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show()
                                                );
                                    });

                                    card.setOnClickListener(v -> {
                                        Intent i = new Intent(this, PlaceDetailActivity.class);
                                        i.putExtra("place_name", name);
                                        startActivity(i);
                                    });

                                    allCards.add(card);
                                    itemContainer.addView(card);
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "장소 상세 조회 실패", e));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "찜 목록 로딩 실패", e);
                    Toast.makeText(this, "찜 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void performSearch() {
        String query = editSearch.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<View> matched = new ArrayList<>();
        for (View card : allCards) {
            String name = ((TextView) card.findViewById(R.id.tv_place_name))
                    .getText().toString().toLowerCase();
            if (name.contains(query)) {
                matched.add(card);
            }
        }

        if (matched.isEmpty()) {
            Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        itemContainer.removeAllViews();
        for (View c : matched) {
            itemContainer.addView(c);
        }
    }

    private void clearSearch() {
        editSearch.setText("");
        itemContainer.removeAllViews();
        for (View c : allCards) {
            itemContainer.addView(c);
        }
    }

    private void sortByName() {
        Collections.sort(allCards, (v1, v2) -> {
            String n1 = ((TextView) v1.findViewById(R.id.tv_place_name)).getText().toString();
            String n2 = ((TextView) v2.findViewById(R.id.tv_place_name)).getText().toString();
            return n1.compareTo(n2);
        });
        itemContainer.removeAllViews();
        for (View c : allCards) {
            itemContainer.addView(c);
        }
    }

    private void sortByDistance() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc == null) {
                        Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<Pair<View, Float>> list = new ArrayList<>();
                    for (View card : allCards) {
                        Object tag = card.getTag();
                        if (tag instanceof double[]) {
                            double[] latlon = (double[]) tag;
                            float[] results = new float[1];
                            Location.distanceBetween(
                                    loc.getLatitude(), loc.getLongitude(),
                                    latlon[0], latlon[1],
                                    results
                            );
                            list.add(new Pair<>(card, results[0]));
                        }
                    }
                    Collections.sort(list, (p1, p2) -> Float.compare(p1.second, p2.second));
                    allCards.clear();
                    for (Pair<View, Float> p : list) {
                        allCards.add(p.first);
                    }
                    itemContainer.removeAllViews();
                    for (View c : allCards) {
                        itemContainer.addView(c);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "위치 정보 조회 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private String safe(Object val, String def) {
        return val != null ? val.toString() : def;
    }
}