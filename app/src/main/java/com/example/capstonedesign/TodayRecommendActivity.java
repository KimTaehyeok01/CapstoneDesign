package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
    private EditText editSearchRecommend;
    private LinearLayout itemContainer;

    private final String weatherApiKey = "f5a32755e587860fe98d96a6a54af17f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_recommend);

        // View 바인딩
        btnBackRecommend    = findViewById(R.id.btnBackRecommend);
        btnRefreshRecommend = findViewById(R.id.btnRefreshRecommend); // ★
        tvWeatherRecommend  = findViewById(R.id.tvWeatherRecommend);
        editSearchRecommend = findViewById(R.id.editSearchRecommend);
        itemContainer       = findViewById(R.id.item_container);

        // Firebase & Location 초기화
        db                  = FirebaseFirestore.getInstance();
        currentUser         = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnBackRecommend.setOnClickListener(v -> finish());

        // 새로고침 클릭 시 애니메이션 후 추천 갱신
        btnRefreshRecommend.setOnClickListener(v -> animateAndRefresh()); // ★

        // 위치 권한 요청 → 승인되면 날씨 조회 & 추천 불러오기
        requestLocationPermission();
    }

    // 애니메이션 후 추천 갱신 메서드
    private void animateAndRefresh() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                itemContainer.removeAllViews();
                fetchRecommendationsWithAnimation();
            }
        });
        itemContainer.startAnimation(fadeOut);
    }

    private void fetchRecommendationsWithAnimation() {
        fetchRecommendations();
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(300);
        itemContainer.startAnimation(fadeIn);
    }

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
                    fetchRecommendationsWithAnimation(); // ★ 여기서도 애니메이션 포함 호출
                });
    }

    private void fetchWeather(double latitude, double longitude) {
        String urlStr = "https://api.openweathermap.org/data/2.5/weather?lat="
                + latitude
                + "&lon=" + longitude
                + "&appid=" + weatherApiKey
                + "&lang=kr&units=metric";

        new Thread(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
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

            for (int i = 0; i < limit; i++) {
                addRecommendationCard(docs.get(i));
            }
        });
    }

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

        itemContainer.addView(card);
    }

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
                Map<String,Object> data = new java.util.HashMap<>();
                data.put("name", name);
                data.put("address", addr);
                favRef.set(data).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "찜 추가됨", Toast.LENGTH_SHORT).show();
                    favButton.setImageResource(R.drawable.baseline_favorite_24);
                });
            }
        });
    }

    private void openDetail(QueryDocumentSnapshot doc) {
        String placeName = doc.getString("name");
        if (placeName == null) return;

        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra("place_name", placeName);
        startActivity(intent);
    }

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
            fetchRecommendationsWithAnimation(); // 권한 없어도 애니메이션 포함
        }
    }
}