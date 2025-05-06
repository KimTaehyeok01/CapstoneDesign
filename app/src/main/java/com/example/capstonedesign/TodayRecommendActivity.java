package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private TextView tvWeatherRecommend;
    private EditText editSearchRecommend;

    private CardView cardRec1, cardRec2;
    private ImageView imgRec1, imgRec2, btnFav1, btnFav2;
    private TextView tvRecTitle1, tvRecAddress1, tvRecRegion1, tvRecPrice1;
    private TextView tvRecTitle2, tvRecAddress2, tvRecRegion2, tvRecPrice2;

    private boolean isFav1 = false;
    private boolean isFav2 = false;

    private QueryDocumentSnapshot doc1;
    private QueryDocumentSnapshot doc2;

    private final String weatherApiKey = "f5a32755e587860fe98d96a6a54af17f"; // OpenWeatherMap API 키

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_recommend);

        btnBackRecommend = findViewById(R.id.btnBackRecommend);
        tvWeatherRecommend = findViewById(R.id.tvWeatherRecommend);
        editSearchRecommend = findViewById(R.id.editSearchRecommend);

        cardRec1 = findViewById(R.id.cardRec1);
        cardRec2 = findViewById(R.id.cardRec2);
        imgRec1 = findViewById(R.id.imgRec1);
        imgRec2 = findViewById(R.id.imgRec2);
        btnFav1 = findViewById(R.id.btnFav1);
        btnFav2 = findViewById(R.id.btnFav2);

        tvRecTitle1 = findViewById(R.id.tvRecTitle1);
        tvRecAddress1 = findViewById(R.id.tvRecAddress1);
        tvRecRegion1 = findViewById(R.id.tvRecRegion1);
        tvRecPrice1 = findViewById(R.id.tvRecPrice1);

        tvRecTitle2 = findViewById(R.id.tvRecTitle2);
        tvRecAddress2 = findViewById(R.id.tvRecAddress2);
        tvRecRegion2 = findViewById(R.id.tvRecRegion2);
        tvRecPrice2 = findViewById(R.id.tvRecPrice2);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnBackRecommend.setOnClickListener(v -> finish());

        requestLocationPermission();
        fetchRecommendations();

        btnFav1.setOnClickListener(v -> toggleFavorite(doc1, btnFav1));
        btnFav2.setOnClickListener(v -> toggleFavorite(doc2, btnFav2));

        cardRec1.setOnClickListener(v -> openDetail(doc1));
        cardRec2.setOnClickListener(v -> openDetail(doc2));
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
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
                });
    }

    private void fetchWeather(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude +
                "&lon=" + longitude +
                "&appid=" + weatherApiKey +
                "&lang=kr&units=metric";

        new Thread(() -> {
            try {
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                JSONObject json = new JSONObject(response.toString());
                String description = json.getJSONArray("weather").getJSONObject(0).getString("description");
                double temp = json.getJSONObject("main").getDouble("temp");

                runOnUiThread(() -> tvWeatherRecommend.setText("현재 날씨: " + description + " (" + temp + "°C)"));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> tvWeatherRecommend.setText("날씨 불러오기 실패"));
            }
        }).start();
    }

    private void fetchRecommendations() {
        CollectionReference locationsRef = db.collection("sports_locations");

        locationsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<QueryDocumentSnapshot> documents = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    documents.add(doc);
                }

                if (documents.size() >= 2) {
                    Collections.shuffle(documents);
                    doc1 = documents.get(0);
                    doc2 = documents.get(1);

                    updateCardView(doc1, imgRec1, tvRecTitle1, tvRecAddress1, tvRecRegion1, tvRecPrice1);
                    updateCardView(doc2, imgRec2, tvRecTitle2, tvRecAddress2, tvRecRegion2, tvRecPrice2);
                } else {
                    Toast.makeText(this, "추천할 장소가 부족합니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "장소를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCardView(QueryDocumentSnapshot doc, ImageView img, TextView title, TextView address, TextView region, TextView price) {
        String name = doc.getString("name");
        String addr = doc.getString("address");
        String topic = doc.getString("topic");
        String imageUrl = doc.getString("image");
        String details = doc.getString("details");

        title.setText(name != null ? name : "장소명 없음");
        address.setText(addr != null ? addr : "주소 없음");
        region.setText(topic != null ? topic : "지역 없음");
        price.setText(details != null ? "정보 제공 중" : "가격 정보 없음");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("gs://")) {
                // gs:// Storage 경로 처리
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                        Glide.with(this).load(uri.toString()).into(img)
                ).addOnFailureListener(e ->
                        Toast.makeText(this, "이미지 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } else {
                Glide.with(this).load(imageUrl).into(img);
            }
        } else {
            img.setImageResource(R.drawable.ic_climb); // 데이터베이스에 사진이 없을 경우 대체 이미지
        }
    }

    private void toggleFavorite(QueryDocumentSnapshot doc, ImageView favButton) {
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (doc == null) {
            Toast.makeText(this, "장소 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String name = doc.getString("name");
        String address = doc.getString("address");

        DocumentReference favRef = db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(name);

        favRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 이미 찜 -> 해제
                favRef.delete().addOnSuccessListener(unused -> {
                    Toast.makeText(this, "찜 해제됨", Toast.LENGTH_SHORT).show();
                    favButton.setImageResource(R.drawable.baseline_favorite_border_24);
                });
            } else {
                // 찜 추가
                Map<String, Object> data = new HashMap<>();
                data.put("name", name);
                data.put("address", address);

                favRef.set(data).addOnSuccessListener(unused -> {
                    Toast.makeText(this, "찜 추가됨", Toast.LENGTH_SHORT).show();
                    favButton.setImageResource(R.drawable.baseline_favorite_24);
                });
            }
        });
    }

    private void openDetail(QueryDocumentSnapshot doc) {
        if (doc == null) return;

        String placeName = doc.getString("name");
        if (placeName == null) return;

        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra("place_name", placeName);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
