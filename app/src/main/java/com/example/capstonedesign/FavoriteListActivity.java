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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class FavoriteListActivity extends AppCompatActivity {

    private static final String TAG = "FavoriteListActivity";

    private LinearLayout itemContainer;  // 찜한 장소 카드들을 담을 컨테이너
    private FirebaseFirestore db;        // Firestore 데이터베이스
    private FirebaseUser currentUser;    // 현재 로그인한 사용자

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        // 레이아웃에서 뷰 연결
        itemContainer = findViewById(R.id.item_container);
        ImageButton backButton = findViewById(R.id.back_button);

        // 뒤로가기 버튼 클릭 시 현재 액티비티 종료
        backButton.setOnClickListener(v -> finish());

        // Firebase 연결
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // 현재 로그인한 사용자가 있으면 찜 목록 불러오기
        if (currentUser != null) {
            loadFavoriteItems();
        } else {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    // 찜한 장소들 불러오는 함수
    private void loadFavoriteItems() {
        String userId = currentUser.getUid();  // 현재 유저 ID

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    itemContainer.removeAllViews();  // 기존 카드뷰들 모두 제거

                    for (var doc : querySnapshot.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data == null) continue;

                        String name = safe(data.get("name"), "장소명 없음");
                        String address = safe(data.get("address"), "주소 없음");
                        String region = safe(data.get("region"), "지역 없음");
                        String price = safe(data.get("price"), "가격 정보 없음");
                        String imageUrl = safe(data.get("image"), "");

                        // 카드 뷰 생성
                        View card = LayoutInflater.from(this).inflate(R.layout.item_favorite_card, itemContainer, false);

                        ImageView img = card.findViewById(R.id.img_place);
                        TextView tvName = card.findViewById(R.id.tv_place_name);
                        TextView tvAddress = card.findViewById(R.id.tv_place_address);
                        TextView tvRegion = card.findViewById(R.id.tv_place_region);
                        TextView tvPrice = card.findViewById(R.id.tv_place_price);
                        ImageView btnFavorite = card.findViewById(R.id.img_favorite);

                        // 데이터 세팅
                        tvName.setText(name);
                        tvAddress.setText(address);
                        tvRegion.setText(region);
                        tvPrice.setText(price);

                        // 이미지 표시
                        if (!imageUrl.isEmpty()) {
                            if (imageUrl.startsWith("gs://")) {
                                // Firebase Storage 경로 처리
                                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                        Glide.with(this).load(uri.toString()).into(img)
                                ).addOnFailureListener(e -> {
                                    Log.e(TAG, "이미지 로드 실패", e);
                                    img.setImageResource(R.drawable.ic_climb); // 기본 이미지
                                });
                            } else {
                                // 일반 URL
                                Glide.with(this).load(imageUrl).into(img);
                            }
                        } else {
                            img.setImageResource(R.drawable.ic_climb);  // 이미지 없을 때 기본 이미지
                        }

                        // 찜 해제 버튼
                        btnFavorite.setOnClickListener(v -> {
                            db.collection("users")
                                    .document(userId)
                                    .collection("favorites")
                                    .document(name)
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "찜 목록에서 제거되었습니다.", Toast.LENGTH_SHORT).show();
                                        itemContainer.removeView(card);  // 카드뷰 제거
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show()
                                    );
                        });

                        // 카드 클릭 시 상세페이지 이동
                        card.setOnClickListener(v -> {
                            Intent intent = new Intent(this, PlaceDetailActivity.class);
                            intent.putExtra("place_name", name);
                            startActivity(intent);
                        });

                        // 카드 추가
                        itemContainer.addView(card);
                    }

                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "찜한 장소가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "찜 목록 로딩 실패", e);
                    Toast.makeText(this, "찜 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    // 값이 null일 경우 기본값 리턴하는 함수
    private String safe(Object value, String fallback) {
        return value != null ? value.toString() : fallback;
    }
}
