package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceDetailActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private ImageView imageViewPlace, imageViewFavorite, imageViewCall;
    private TextView textViewTitle, textViewAddress, textViewPrice, textViewPhone, textViewMore, toolbarTitle;
    private LinearLayout textViewHours;
    private ImageButton buttonStamp;

    private boolean isFavorite = false;
    private String placeName = "";
    private static final int CALL_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_details);

        final View rootView = findViewById(android.R.id.content);
        final RelativeLayout topAppBar = findViewById(R.id.top_app_bar);
        final ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            if (topAppBar != null && topAppBar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) topAppBar.getLayoutParams();
                params.topMargin = topInset;
                topAppBar.setLayoutParams(params);
            }

            if (scrollView != null) {
                scrollView.setPadding(
                        scrollView.getPaddingLeft(),
                        scrollView.getPaddingTop(),
                        scrollView.getPaddingRight(),
                        bottomInset
                );
            }

            return WindowInsetsCompat.CONSUMED;
        });

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
        buttonStamp = findViewById(R.id.buttonStamp);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        placeName = getIntent().getStringExtra("place_name");
        if (placeName == null) {
            finish();
            return;
        }

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

        View.OnClickListener callListener = v -> requestCallPermission();
        textViewPhone.setOnClickListener(callListener);
        imageViewCall.setOnClickListener(callListener);

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
                            String category = documentSnapshot.getString("category");

                            if (name != null) {
                                textViewTitle.setText(name);
                                toolbarTitle.setText(name);
                            }
                            if (address != null) textViewAddress.setText(address);
                            if (price != null) textViewPrice.setText(price);
                            if (phone != null && !phone.isEmpty()) textViewPhone.setText(phone);
                            else textViewPhone.setText("전화번호 없음");
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
                                    storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                            Glide.with(this).load(uri.toString()).into(imageViewPlace)
                                    ).addOnFailureListener(e ->
                                            Toast.makeText(this, "이미지 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                                } else {
                                    Glide.with(this).load(imageUrl).into(imageViewPlace);
                                }
                            }

                            // 스탬프 버튼 리스너 설정
                            buttonStamp.setOnClickListener(v -> {
                                checkAndApplyStamp(placeName, category);
                            });

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

    private void checkAndApplyStamp(String placeIdentifier, String category) {
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (placeIdentifier == null || category == null) {
            Toast.makeText(this, "장소 정보가 없어 스탬프를 찍을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> stampedPlaces = (List<String>) documentSnapshot.get("stampedPlaces");

                if (stampedPlaces != null && stampedPlaces.contains(placeIdentifier)) {
                    Toast.makeText(this, "이미 스탬프를 찍은 장소입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    applyStamp(userRef, placeIdentifier, category);
                }
            } else {
                applyStamp(userRef, placeIdentifier, category);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "사용자 정보를 확인하는 데 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void applyStamp(DocumentReference userRef, String placeIdentifier, String category) {
        String fieldToIncrement;
        switch (category) {
            case "육상": fieldToIncrement = "stampCounts.land"; break;
            case "해상": fieldToIncrement = "stampCounts.sea"; break;
            case "항공": fieldToIncrement = "stampCounts.air"; break;
            default:
                Toast.makeText(this, "스탬프를 찍을 수 없는 카테고리입니다.", Toast.LENGTH_SHORT).show();
                return;
        }

        // 1. 스탬프 횟수와 방문 기록을 먼저 업데이트합니다.
        WriteBatch batch = firestore.batch();
        batch.update(userRef, fieldToIncrement, FieldValue.increment(1));
        batch.update(userRef, "stampedPlaces", FieldValue.arrayUnion(placeIdentifier));

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "스탬프를 찍었습니다!", Toast.LENGTH_SHORT).show();
                    // 2. 성공 시, 티어 달성 여부를 확인하고 기록합니다.
                    checkAndRecordAchievement(userRef, category);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "스탬프 저장에 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    // ✅ 티어 달성 여부를 확인하고 Firestore에 기록하는 새 메소드
    private void checkAndRecordAchievement(DocumentReference userRef, String category) {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) return;

            Map<String, Long> stampCounts = (Map<String, Long>) documentSnapshot.get("stampCounts");
            if (stampCounts == null) return;

            long currentCount = 0;
            String categoryFieldName = "";

            switch (category) {
                case "육상":
                    currentCount = stampCounts.getOrDefault("land", 0L);
                    categoryFieldName = "land";
                    break;
                case "해상":
                    currentCount = stampCounts.getOrDefault("sea", 0L);
                    categoryFieldName = "sea";
                    break;
                case "항공":
                    currentCount = stampCounts.getOrDefault("air", 0L);
                    categoryFieldName = "air";
                    break;
            }

            // 티어 달성 횟수 조건 확인 (3, 6, 9, 12, 15...)
            if (currentCount == 3 || currentCount == 6 || currentCount == 9 || currentCount == 12 || currentCount == 15) {
                String tier = getTierForCount(currentCount); // 횟수에 맞는 티어 이름 가져오기

                // achievements 컬렉션 참조
                CollectionReference achievementsRef = userRef.collection("achievements");

                // 중복 기록 방지: 이미 해당 티어/카테고리 기록이 있는지 확인
                achievementsRef.whereEqualTo("tier", tier).whereEqualTo("category", category)
                        .limit(1).get().addOnSuccessListener(querySnapshot -> {
                            if (querySnapshot.isEmpty()) {
                                // 기록이 없으면 새로 생성
                                Map<String, Object> achievementData = new HashMap<>();
                                achievementData.put("tier", tier);
                                achievementData.put("category", category);
                                achievementData.put("timestamp", FieldValue.serverTimestamp()); // 서버 시간으로 기록

                                achievementsRef.add(achievementData);
                            }
                        });
            }
        });
    }

    // ✅ 횟수로 티어 이름을 반환하는 헬퍼 메소드 (기존에 없다면 추가)
    private String getTierForCount(long count) {
        if (count >= 15) return "Master";
        if (count >= 12) return "Platinum";
        if (count >= 9) return "Gold";
        if (count >= 6) return "Silver";
        if (count >= 3) return "Bronze";
        return "Unranked";
    }

    private void requestCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PERMISSION_REQUEST_CODE);
        } else {
            startCall();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCall();
            } else {
                Toast.makeText(this, "전화 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCall() {
        String number = textViewPhone.getText().toString();
        if (number.equals("전화번호 없음") || number.isEmpty()) {
            Toast.makeText(this, "전화번호가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    public void onBackClicked(View view) {
        finish();
    }

    public void onHomeClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}