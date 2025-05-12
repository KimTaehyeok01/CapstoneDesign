package com.example.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.capstonedesign.settings_information.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class FavoriteListActivity extends AppCompatActivity {

    private static final String TAG = "FavoriteListActivity";

    private LinearLayout itemContainer;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // 하단 네비게이션 버튼
    private ImageButton navSearch, navMarker, navHome, navHeart, navSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        itemContainer = findViewById(R.id.item_container);
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 네비게이션 바 버튼 바인딩
        navSearch  = findViewById(R.id.nav_search);
        navMarker  = findViewById(R.id.nav_marker);
        navHome    = findViewById(R.id.nav_home);
        navHeart   = findViewById(R.id.nav_heart);
        navSetting = findViewById(R.id.nav_setting);

        navSearch.setOnClickListener(v -> {
            startActivity(new Intent(FavoriteListActivity.this, SearchActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        navMarker.setOnClickListener(v -> {
            startActivity(new Intent(FavoriteListActivity.this, MapActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        navHome.setOnClickListener(v -> {
            Intent i = new Intent(FavoriteListActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        navHeart.setOnClickListener(v -> {
            Intent i = new Intent(FavoriteListActivity.this, FavoriteListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        navSetting.setOnClickListener(v -> {
            startActivity(new Intent(FavoriteListActivity.this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Firebase 초기화
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            loadFavoriteItems();
        } else {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFavoriteItems() {
        String userId = currentUser.getUid();

        // 1) users/{userId}/favorites 컬렉션 조회
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

                        // 2) sports_locations 에서 상세정보 조회
                        CollectionReference locRef = db.collection("sports_locations");
                        locRef.whereEqualTo("name", name)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(locSnap -> {
                                    String region   = "지역 없음";
                                    String price    = "가격 정보 없음";
                                    String imageUrl = "";

                                    if (!locSnap.isEmpty()) {
                                        // DocumentSnapshot 그대로 사용
                                        DocumentSnapshot locDoc = locSnap.getDocuments().get(0);
                                        region   = safe(locDoc.get("topic"),   region);
                                        price    = safe(locDoc.get("details"), price);
                                        imageUrl = safe(locDoc.get("image"),   "");
                                    }

                                    // 3) 카드뷰 생성 및 바인딩
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

                                    // 이미지 로드
                                    if (!imageUrl.isEmpty()) {
                                        if (imageUrl.startsWith("gs://")) {
                                            StorageReference storageRef = FirebaseStorage
                                                    .getInstance()
                                                    .getReferenceFromUrl(imageUrl);
                                            storageRef.getDownloadUrl()
                                                    .addOnSuccessListener(uri ->
                                                            Glide.with(this).load(uri.toString()).into(img)
                                                    )
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "이미지 로드 실패", e);
                                                        img.setImageResource(R.drawable.ic_climb);
                                                    });
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

    private String safe(Object val, String def) {
        return val != null ? val.toString() : def;
    }
}
