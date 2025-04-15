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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // 전달받은 장소 이름 (SearchActivity → intent로 전달됨)
        String placeName = getIntent().getStringExtra("place_name");
        if (placeName == null) {
            finish(); // 예외처리
            return;
        }

        // Firestore에서 데이터 불러오기
        DocumentReference docRef = firestore.collection("sports_locations").document(placeName);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String address = documentSnapshot.getString("address");
                String hours = documentSnapshot.getString("hours");
                String price = documentSnapshot.getString("price");
                String phone = documentSnapshot.getString("phone");
                String more = documentSnapshot.getString("more");
                String imageUrl = documentSnapshot.getString("imageUrl");

                if (name != null) {
                    textViewTitle.setText(name);
                    toolbarTitle.setText(name);
                }

                if (address != null) textViewAddress.setText(address);
                if (price != null) textViewPrice.setText(price);
                if (phone != null) textViewPhone.setText(phone);
                if (more != null) textViewMore.setText(more);

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

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).into(imageViewPlace);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "장소 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // 뒤로가기 버튼 클릭 시
    public void onBackClicked(View view) {
        finish(); // 현재 화면 종료 → 이전 화면으로
    }

    // 홈 버튼 클릭 시
    public void onHomeClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
