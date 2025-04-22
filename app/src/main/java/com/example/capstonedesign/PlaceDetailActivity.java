package com.example.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PlaceDetailActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private ImageView imageViewPlace;
    private TextView textViewTitle, textViewAddress, textViewPrice, textViewPhone, textViewMore, toolbarTitle;
    private LinearLayout textViewHours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // 뷰 연결
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageViewPlace = findViewById(R.id.imageViewPlace);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewHours = findViewById(R.id.textViewHours);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewMore = findViewById(R.id.textViewMore);

        firestore = FirebaseFirestore.getInstance();

        // 전달받은 장소 이름
        String placeName = getIntent().getStringExtra("place_name");
        if (placeName == null) {
            finish(); // 예외처리
            return;
        }

        // Firestore에서 장소 정보 불러오기
        firestore.collection("sports_locations")
                .whereEqualTo("name", placeName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                            String name = documentSnapshot.getString("name");
                            String address = documentSnapshot.getString("address");
                            String hours = documentSnapshot.getString("hours");
                            String price = documentSnapshot.getString("price");
                            String phone = documentSnapshot.getString("phone");
                            String more = documentSnapshot.getString("summary"); // Firestore의 'summary' 필드
                            String imageUrl = documentSnapshot.getString("image");

                            // 이름
                            if (name != null) {
                                textViewTitle.setText(name);
                                toolbarTitle.setText(name);
                            }

                            // 주소
                            if (address != null) textViewAddress.setText(address);

                            // 가격
                            if (price != null) textViewPrice.setText(price);

                            // 전화번호
                            if (phone != null && !phone.isEmpty()) {
                                textViewPhone.setText(phone);
                            } else {
                                textViewPhone.setText("전화번호 없음");
                            }

                            // 상세 설명
                            if (more != null) textViewMore.setText(more);

                            // 운영 시간
                            if (hours != null) {
                                textViewHours.removeAllViews();
                                for (String line : hours.split("\n")) {
                                    TextView hourLine = new TextView(this);
                                    hourLine.setText(line);
                                    hourLine.setTextSize(14f);
                                    hourLine.setTextColor(getResources().getColor(android.R.color.black));
                                    textViewHours.addView(hourLine);
                                }
                            }

                            // 이미지 URL 처리 (gs:// → download URL 변환)
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                if (imageUrl.startsWith("gs://")) {
                                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        Glide.with(this).load(uri.toString()).into(imageViewPlace);
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(this, "이미지 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    Glide.with(this).load(imageUrl).into(imageViewPlace);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "장소 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "장소 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    // 뒤로가기 버튼 클릭 시
    public void onBackClicked(View view) {
        finish(); // 현재 화면 종료
    }

    // 홈 버튼 클릭 시
    public void onHomeClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
