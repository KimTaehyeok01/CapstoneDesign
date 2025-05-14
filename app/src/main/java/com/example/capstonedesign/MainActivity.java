package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.capstonedesign.settings_information.SettingsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // UI 컴포넌트
    private DrawerLayout drawerLayout;
    private ImageView iv_menu;
    private ImageButton btn_close_drawer;
    private ImageButton navSearch, navHome, navSetting, navMarker, navHeart;
    private TextView tvWeather, tvTodayRecommend, tvNearbyRecommend;

    // 프로필 패널 뷰 바인딩
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileAge;
    private TextView tvProfileInterest;
    private TextView tvProfileSeason;

    // 위치 & 날씨
    private FusedLocationProviderClient fusedLocationClient;
    private String apiKey = "f5a32755e587860fe98d96a6a54af17f";

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_profile);

        // 시스템 바(insets) 패딩 처리
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // Drawer & 메뉴 아이콘
        drawerLayout      = findViewById(R.id.drawer_layout);
        iv_menu           = findViewById(R.id.iv_menu);
        btn_close_drawer  = findViewById(R.id.btn_close_drawer);
        iv_menu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btn_close_drawer.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));

        // 하단 네비게이션
        navSearch  = findViewById(R.id.nav_search);
        navMarker  = findViewById(R.id.nav_marker);
        navHome    = findViewById(R.id.nav_home);
        navHeart   = findViewById(R.id.nav_heart);
        navSetting = findViewById(R.id.nav_setting);

        navSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        navMarker.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        navHome.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        });
        navHeart.setOnClickListener(v -> startActivity(new Intent(this, FavoriteListActivity.class)));
        navSetting.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        // 검색 바 클릭
        EditText searchBar = findViewById(R.id.main_search_bar);
        searchBar.setFocusable(false);
        searchBar.setClickable(true);
        searchBar.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 오늘·주변 추천
        tvTodayRecommend  = findViewById(R.id.tv_today_recommend);
        tvNearbyRecommend = findViewById(R.id.tv_nearby_recommend);
        tvTodayRecommend.setOnClickListener(v ->
                startActivity(new Intent(this, TodayRecommendActivity.class))
        );
        tvNearbyRecommend.setOnClickListener(v ->
                startActivity(new Intent(this, NearbyRecommendActivity.class))
        );

        // 날씨
        tvWeather = findViewById(R.id.tv_weather);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();

        // Firebase 초기화
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // 프로필 패널 뷰 바인딩
        tvProfileName     = findViewById(R.id.tv_profile_name);
        tvProfileEmail    = findViewById(R.id.tv_profile_email);
        tvProfileAge      = findViewById(R.id.tv_profile_age);
        tvProfileInterest = findViewById(R.id.tv_profile_interest);
        tvProfileSeason   = findViewById(R.id.tv_profile_season);

        // Firestore에서 프로필 불러오기
        loadUserProfileFromFirestore();
    }

    // 위치 권한 요청
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            getLastLocation();
        }
    }

    // 마지막 위치 가져오기
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        fetchWeather(location.getLatitude(), location.getLongitude());
                    } else {
                        tvWeather.setText("위치를 찾을 수 없습니다.");
                    }
                });
    }

    // OpenWeatherMap API 호출
    private void fetchWeather(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&appid=" + apiKey
                + "&lang=kr&units=metric";

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
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
                String desc = json.getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description");
                double temp = json.getJSONObject("main").getDouble("temp");

                runOnUiThread(() ->
                        tvWeather.setText("현재 날씨: " + desc + " (" + temp + "°C)")
                );
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> tvWeather.setText("날씨 불러오기 실패"));
            }
        }).start();
    }

    // 퍼미션 콜백
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // Firestore에서 현재 사용자 프로필 읽어오기
    @SuppressWarnings("unchecked")
    private void loadUserProfileFromFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = user.getUid();
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "프로필 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 1) displayName  2) Firestore 'name'  3) 이메일 앞부분  4) 기본값
                    String name = user.getDisplayName();
                    if (TextUtils.isEmpty(name)) {
                        String nameFromDb = doc.getString("name");
                        if (!TextUtils.isEmpty(nameFromDb)) {
                            name = nameFromDb;
                        } else if (user.getEmail() != null && user.getEmail().contains("@")) {
                            name = user.getEmail().split("@")[0];
                        } else {
                            name = "사용자";
                        }
                    }
                    tvProfileName.setText(name);

                    // 이메일
                    tvProfileEmail.setText(user.getEmail());

                    // 나이
                    Long age = doc.getLong("age");
                    if (age != null) {
                        tvProfileAge.setText(age + "세");
                    }

                    // 관심 종목
                    List<String> categories = (List<String>) doc.get("interestCategory");
                    if (categories != null && !categories.isEmpty()) {
                        tvProfileInterest.setText(TextUtils.join(", ", categories));
                    }

                    // 선호 계절
                    List<String> seasons = (List<String>) doc.get("interestSeasons");
                    if (seasons != null && !seasons.isEmpty()) {
                        tvProfileSeason.setText(TextUtils.join(", ", seasons));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "프로필 불러오기 실패", Toast.LENGTH_SHORT).show()
                );
    }
}