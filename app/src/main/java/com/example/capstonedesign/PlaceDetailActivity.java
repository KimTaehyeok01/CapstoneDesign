package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class PlaceDetailActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private ImageView imageViewPlace;
    private TextView textViewTitle, textViewAddress, textViewPrice, textViewPhone, textViewMore, toolbarTitle;
    private LinearLayout textViewHours;

    private ImageView imageViewFavorite;
    private ImageView imageViewCall;
    private boolean isFavorite = false;
    private FirebaseUser currentUser;
    private String placeName = "";

    private static final int REQUEST_CALL_PHONE = 1;

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
        imageViewFavorite = findViewById(R.id.imageViewFavorite);
        imageViewCall = findViewById(R.id.imageViewCall);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 전달받은 장소 이름
        placeName = getIntent().getStringExtra("place_name");
        if (placeName == null) {
            finish();
            return;
        }

        // 전화 기능 클릭 리스너 (아이콘 + 텍스트)
        View.OnClickListener callClickListener = v -> {
            String phoneNumber = textViewPhone.getText().toString().trim();
            if (!phoneNumber.isEmpty() && !phoneNumber.equals("전화번호 없음")) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));

                // 권한 확인
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
                } else {
                    startActivity(callIntent);
                }
            } else {
                Toast.makeText(this, "전화번호가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        imageViewCall.setOnClickListener(callClickListener);
        textViewPhone.setOnClickListener(callClickListener);

        // 찜 버튼 클릭
        imageViewFavorite.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();
            DocumentReference favRef = firestore.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(placeName);

            if (!isFavorite) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", placeName);
                data.put("address", textViewAddress.getText().toString());
                data.put("phone", textViewPhone.getText().toString());

                favRef.set(data).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "찜 추가됨", Toast.LENGTH_SHORT).show();
                    imageViewFavorite.setImageResource(R.drawable.baseline_favorite_24);
                    isFavorite = true;
                });
            } else {
                favRef.delete().addOnSuccessListener(unused -> {
                    Toast.makeText(this, "찜 해제됨", Toast.LENGTH_SHORT).show();
                    imageViewFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                    isFavorite = false;
                });
            }
        });

        // 장소 정보 가져오기
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
                            String more = documentSnapshot.getString("summary");
                            String imageUrl = documentSnapshot.getString("image");

                            if (name != null) {
                                textViewTitle.setText(name);
                                toolbarTitle.setText(name);
                            }
                            if (address != null) textViewAddress.setText(address);
                            if (price != null) textViewPrice.setText(price);
                            if (phone != null && !phone.isEmpty()) {
                                textViewPhone.setText(phone);
                            } else {
                                textViewPhone.setText("전화번호 없음");
                            }
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

                            if (currentUser != null) {
                                String userId = currentUser.getUid();
                                firestore.collection("users")
                                        .document(userId)
                                        .collection("favorites")
                                        .document(placeName)
                                        .get()
                                        .addOnSuccessListener(doc -> {
                                            if (doc.exists()) {
                                                isFavorite = true;
                                                imageViewFavorite.setImageResource(R.drawable.baseline_favorite_24);
                                            } else {
                                                isFavorite = false;
                                                imageViewFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                                            }
                                        });
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

    // 뒤로가기
    public void onBackClicked(View view) {
        finish();
    }

    // 홈으로
    public void onHomeClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // 전화 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                textViewPhone.performClick(); // 권한 승인되면 전화 실행
            } else {
                Toast.makeText(this, "전화 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
