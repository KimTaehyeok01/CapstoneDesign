package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Map;

public class FavoriteListActivity extends AppCompatActivity {

    private static final String TAG = "FavoriteListActivity";

    private LinearLayout itemContainer;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FusedLocationProviderClient fusedLocationClient;

    // 하단 네비게이션 버튼
    private ImageButton navSearch, navMarker, navHome, navHeart, navSetting;

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        // 뷰 바인딩
        itemContainer = findViewById(R.id.item_container);
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 정렬 버튼 리스너
        findViewById(R.id.btn_sort_location).setOnClickListener(v -> sortByName());
        findViewById(R.id.btn_sort_distance).setOnClickListener(v -> sortByDistance());

        // 네비게이션 바 버튼 바인딩 및 클릭 처리
        navSearch  = findViewById(R.id.nav_search);
        navMarker  = findViewById(R.id.nav_marker);
        navHome    = findViewById(R.id.nav_home);
        navHeart   = findViewById(R.id.nav_heart);
        navSetting = findViewById(R.id.nav_setting);

        navSearch.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        navMarker.setOnClickListener(v -> {
            startActivity(new Intent(this, MapActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        navHome.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        navHeart.setOnClickListener(v -> {
            Intent i = new Intent(this, FavoriteListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        navSetting.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

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

    private void loadFavoriteItems() {
        String userId = currentUser.getUid();

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(favSnap -> {
                    itemContainer.removeAllViews();
                    List<DocumentSnapshot> favDocs = favSnap.getDocuments();
                    if (favDocs.isEmpty()) {
                        Toast.makeText(this, "찜한 장소가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot favDoc : favDocs) {
                        String name    = safe(favDoc.get("name"),    "장소명 없음");
                        String address = safe(favDoc.get("address"), "주소 없음");

                        // 장소 상세정보 조회
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
                                        region    = safe(locDoc.get("topic"),   region);
                                        price     = safe(locDoc.get("details"), price);
                                        imageUrl  = safe(locDoc.get("image"),   "");
                                        if (locDoc.get("latitude") instanceof Number) {
                                            latitude = ((Number) locDoc.get("latitude")).doubleValue();
                                        }
                                        if (locDoc.get("longitude") instanceof Number) {
                                            longitude = ((Number) locDoc.get("longitude")).doubleValue();
                                        }
                                    }

                                    // 카드뷰 생성 및 바인딩
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

                                    // 카드에 좌표 저장
                                    card.setTag(new double[]{ latitude, longitude });

                                    // 이미지 로드
                                    if (!imageUrl.isEmpty()) {
                                        if (imageUrl.startsWith("gs://")) {
                                            // Firebase Storage
                                            // ...
                                            img.setImageResource(R.drawable.ic_climb);
                                        } else {
                                            Glide.with(this).load(imageUrl).into(img);
                                        }
                                    } else {
                                        img.setImageResource(R.drawable.ic_climb);
                                    }

                                    // 즐겨찾기 해제
                                    btnFav.setOnClickListener(v -> {
                                        db.collection("users")
                                                .document(userId)
                                                .collection("favorites")
                                                .document(name)
                                                .delete()
                                                .addOnSuccessListener(u -> {
                                                    Toast.makeText(this, "찜 목록에서 제거되었습니다.", Toast.LENGTH_SHORT).show();
                                                    itemContainer.removeView(card);
                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show()
                                                );
                                    });

                                    // 상세 페이지 이동
                                    card.setOnClickListener(v -> {
                                        Intent i = new Intent(this, PlaceDetailActivity.class);
                                        i.putExtra("place_name", name);
                                        startActivity(i);
                                    });

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

    // 가나다순 정렬
    private void sortByName() {
        int count = itemContainer.getChildCount();
        List<View> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(itemContainer.getChildAt(i));
        }
        Collections.sort(cards, (v1, v2) -> {
            String n1 = ((TextView) v1.findViewById(R.id.tv_place_name)).getText().toString();
            String n2 = ((TextView) v2.findViewById(R.id.tv_place_name)).getText().toString();
            return n1.compareTo(n2);
        });
        itemContainer.removeAllViews();
        for (View c : cards) {
            itemContainer.addView(c);
        }
    }

    // 내 위치 기준 거리순 정렬
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void sortByDistance() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc == null) {
                        Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int count = itemContainer.getChildCount();
                    List<Pair<View, Float>> list = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        View card = itemContainer.getChildAt(i);
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
                    itemContainer.removeAllViews();
                    for (Pair<View, Float> p : list) {
                        itemContainer.addView(p.first);
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
