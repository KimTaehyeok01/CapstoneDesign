package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodayRecommendActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ImageButton btnBackRecommend;
    private ImageButton btnRefreshRecommend;
    private TextView tvWeatherRecommend;
    private LinearLayout itemContainer;
    private Button recommendButton;
    private TextView resultTextView;

    private final String weatherApiKey = "f5a32755e587860fe98d96a6a54af17f";

    // GPT 추천용
    private GPTRecommender recommender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_recommend);

        // View 바인딩
        btnBackRecommend    = findViewById(R.id.btnBackRecommend);
        btnRefreshRecommend = findViewById(R.id.btnRefreshRecommend);
        tvWeatherRecommend  = findViewById(R.id.tvWeatherRecommend);
        itemContainer       = findViewById(R.id.item_container);

        // RecommendActivity 에서 쓰던 뷰
        recommendButton = findViewById(R.id.recommendButton);
        resultTextView  = findViewById(R.id.resultTextView);

        // Firebase & Location 초기화
        db                  = FirebaseFirestore.getInstance();
        currentUser         = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // GPT Recommender 초기화
        recommender = new GPTRecommender();

        btnBackRecommend.setOnClickListener(v -> finish());

        // 새로고침 클릭 시 페이드 아웃→위치/추천 재조회→페이드 인
        btnRefreshRecommend.setOnClickListener(v -> {
            itemContainer.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        fetchLocationAndInit(); // 날씨+랜덤 추천
                        itemContainer.setAlpha(0f);
                        itemContainer.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .start();
                    })
                    .start();
        });

        // GPT 추천 버튼 클릭 시
        recommendButton.setOnClickListener(v -> {
            resultTextView.setText("추천 중...");
            itemContainer.removeAllViews();

            // Firebase 로그인된 사용자 ID 가져오기
            if (currentUser == null) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = currentUser.getUid();

            recommender.getRecommendations(userId)
                    .addOnSuccessListener(result -> {
                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            LayoutInflater inflater = LayoutInflater.from(this);

                            // 디버그 메시지 구성
                            StringBuilder debugInfo = new StringBuilder();
                            debugInfo.append("받은 추천 개수: ").append(jsonArray.length()).append("\n");

                            int count = Math.min(5, jsonArray.length());
                            for (int i = 0; i < count; i++) {
                                JSONObject item = jsonArray.getJSONObject(i);
                                View cardView = inflater.inflate(R.layout.item_nearby_card, itemContainer, false);

                                ImageView imgPlace    = cardView.findViewById(R.id.img_place);
                                TextView tvName       = cardView.findViewById(R.id.tv_place_name);
                                TextView tvAddress    = cardView.findViewById(R.id.tv_place_address);
                                TextView tvRegion     = cardView.findViewById(R.id.tv_place_region);
                                TextView tvPrice      = cardView.findViewById(R.id.tv_place_price);

                                tvName   .setText(item.optString("name",    "이름 없음"));
                                tvAddress.setText(item.optString("address", "주소 없음"));
                                tvRegion .setText(item.optString("region",  "지역 정보 없음"));
                                tvPrice  .setText(item.optString("price",   "가격 정보 없음"));

                                itemContainer.addView(cardView);
                                debugInfo.append(i+1).append(". ")
                                        .append(item.optString("name","")).append("\n");
                            }

                            resultTextView.setText(debugInfo.toString());

                        } catch (JSONException e) {
                            resultTextView.setText("파싱 오류: " + e.getMessage());
                        }
                    })
                    .addOnFailureListener(e -> resultTextView.setText("추천 실패: " + e.getMessage()));
        });

        // 최초 권한 요청 → 날씨 조회 & 랜덤 추천
        requestLocationPermission();
    }

    // 1) 위치 권한 요청
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            fetchLocationAndInit();
        }
    }

    // 2) 권한 승인 후: 날씨 조회 + 오늘의 추천 10개 불러오기
    private void fetchLocationAndInit() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        fetchWeather(location.getLatitude(), location.getLongitude());
                    } else {
                        tvWeatherRecommend.setText("위치를 찾을 수 없습니다.");
                    }
                    fetchRecommendations();  // 랜덤 10개
                });
    }

    // 3) 날씨 가져오기
    private void fetchWeather(double latitude, double longitude) {
        String urlStr = "https://api.openweathermap.org/data/2.5/weather?lat="
                + latitude
                + "&lon=" + longitude
                + "&appid=" + weatherApiKey
                + "&lang=kr&units=metric";

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();

                JSONObject json = new JSONObject(sb.toString());
                String description = json.getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description");
                double temp = json.getJSONObject("main").getDouble("temp");

                runOnUiThread(() ->
                        tvWeatherRecommend.setText(
                                "현재 날씨: " + description + " (" + temp + "°C)"
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        tvWeatherRecommend.setText("날씨 불러오기 실패")
                );
            }
        }).start();
    }

    // 4) 오늘의 추천 10개 장소 (랜덤)
    private void fetchRecommendations() {
        CollectionReference locationsRef = db.collection("sports_locations");
        locationsRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "추천 장소를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<QueryDocumentSnapshot> docs = new ArrayList<>();
            for (QueryDocumentSnapshot doc : task.getResult()) {
                docs.add(doc);
            }

            Collections.shuffle(docs);
            int limit = Math.min(docs.size(), 10);

            itemContainer.removeAllViews();
            for (int i = 0; i < limit; i++) {
                addRecommendationCard(docs.get(i));
            }
        });
    }

    // 5) 카드뷰 생성 및 바인딩 (오늘의 추천 / 즐겨찾기 / 상세 이동)
    private void addRecommendationCard(QueryDocumentSnapshot doc) {
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_nearby_card, itemContainer, false);

        ImageView imgPlace    = card.findViewById(R.id.img_place);
        TextView tvName       = card.findViewById(R.id.tv_place_name);
        TextView tvAddress    = card.findViewById(R.id.tv_place_address);
        TextView tvRegion     = card.findViewById(R.id.tv_place_region);
        TextView tvPrice      = card.findViewById(R.id.tv_place_price);
        ImageView imgFavorite = card.findViewById(R.id.img_favorite);

        String name     = doc.getString("name");
        String address  = doc.getString("address");
        String region   = doc.getString("topic");
        String details  = doc.getString("details");
        String imageUrl = doc.getString("image");

        tvName   .setText(name    != null ? name    : "장소명 없음");
        tvAddress.setText(address != null ? address : "주소 없음");
        tvRegion .setText(region  != null ? region  : "지역 없음");
        tvPrice  .setText(details != null ? details : "가격 정보 없음");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imgPlace);
        } else {
            imgPlace.setImageResource(R.drawable.ic_climb);
        }

        // 즐겨찾기 초기 반영
        if (currentUser != null && name != null) {
            String userId = currentUser.getUid();
            DocumentReference favRef = db
                    .collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(name);
            favRef.get().addOnSuccessListener(snap -> {
                if (snap.exists()) {
                    imgFavorite.setImageResource(R.drawable.baseline_favorite_24);
                } else {
                    imgFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                }
            });
        } else {
            imgFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
        }

        // 찜 토글
        imgFavorite.setOnClickListener(v -> toggleFavorite(doc, imgFavorite));
        // 상세 페이지 이동
        card.setOnClickListener(v -> openDetail(doc));

        itemContainer.addView(card);
    }

    // 6) 즐겨찾기 토글
    private void toggleFavorite(QueryDocumentSnapshot doc, ImageView favButton) {
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        String name   = doc.getString("name");
        String addr   = doc.getString("address");
        if (name == null) return;

        DocumentReference favRef = db
                .collection("users")
                .document(userId)
                .collection("favorites")
                .document(name);

        favRef.get().addOnSuccessListener(snap -> {
            if (snap.exists()) {
                favRef.delete().addOnSuccessListener(unused -> {
                    Toast.makeText(this, "찜 해제됨", Toast.LENGTH_SHORT).show();
                    favButton.setImageResource(R.drawable.baseline_favorite_border_24);
                });
            } else {
                Map<String,Object> data = new HashMap<>();
                data.put("name", name);
                data.put("address", addr);
                favRef.set(data).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "찜 추가됨", Toast.LENGTH_SHORT).show();
                    favButton.setImageResource(R.drawable.baseline_favorite_24);
                });
            }
        });
    }

    // 7) 상세 페이지로 이동
    private void openDetail(QueryDocumentSnapshot doc) {
        String placeName = doc.getString("name");
        if (placeName == null) return;

        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra("place_name", placeName);
        startActivity(intent);
    }

    // 권한 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndInit();
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            fetchRecommendations();
        }
    }

    // GPT 기반 추천을 호출하는 Helper 클래스
    private static class GPTRecommender {
        private final FirebaseFunctions functions;

        public GPTRecommender() {
            // asia-northeast3 (예시)
            functions = FirebaseFunctions.getInstance("asia-northeast3");
        }

        public Task<String> getRecommendations(String userId) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);

            return functions
                    .getHttpsCallable("recommendPlacesByGPT")
                    .call(data)
                    .continueWith(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        HttpsCallableResult result = task.getResult();
                        return (String) result.getData();
                    });
        }
    }
}
