package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.example.capstonedesign.settings_information.SettingsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // UI 컴포넌트
    private DrawerLayout drawerLayout;
    private ImageView iv_menu;
    private ImageButton btn_close_drawer;
    private ImageButton navSearch, navHome, navSetting, navMarker, navHeart;
    private TextView tvWeather, tvTodayRecommend, tvNearbyRecommend;

    // 오늘·주변 추천용 뷰
    private ImageView imgToday1, imgToday2, imgNearby1, imgNearby2;
    private TextView tvToday1, tvToday2, tvNearby1, tvNearby2;

    // 랜덤 뽑기 UI
    private ImageView imgRandomPlace, btnDiceRefresh;
    private TextView tvRandomPlaceName;

    // 프로필 패널 뷰
    private TextView tvProfileName, tvProfileEmail, tvProfileAge, tvProfileInterest, tvProfileSeason;

    // 위치 & 날씨
    private FusedLocationProviderClient fusedLocationClient;
    private String apiKey = "f5a32755e587860fe98d96a6a54af17f";

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // 슬롯 애니메이션
    private Handler slotHandler = new Handler();
    private boolean isSlotRunning = false;
    private List<QueryDocumentSnapshot> placeDocs;
    private long slotDelay = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_profile);

        // 시스템 바(insets) 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // Drawer & 메뉴 초기화
        drawerLayout     = findViewById(R.id.drawer_layout);
        iv_menu          = findViewById(R.id.iv_menu);
        btn_close_drawer = findViewById(R.id.btn_close_drawer);
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

        // 검색 바 설정
        EditText searchBar = findViewById(R.id.main_search_bar);
        searchBar.setFocusable(false);
        searchBar.setClickable(true);
        searchBar.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 오늘·주변 추천 버튼
        tvTodayRecommend  = findViewById(R.id.tv_today_recommend);
        tvNearbyRecommend = findViewById(R.id.tv_nearby_recommend);
        tvTodayRecommend.setOnClickListener(v ->
                startActivity(new Intent(this, TodayRecommendActivity.class))
        );
        tvNearbyRecommend.setOnClickListener(v ->
                startActivity(new Intent(this, NearbyRecommendActivity.class))
        );

        // 오늘·주변 추천 뷰 바인딩
        imgToday1 = findViewById(R.id.img_today_item1);
        imgToday2 = findViewById(R.id.img_today_item2);
        tvToday1  = findViewById(R.id.tv_today_item1);
        tvToday2  = findViewById(R.id.tv_today_item2);
        imgNearby1 = findViewById(R.id.img_nearby_item1);
        imgNearby2 = findViewById(R.id.img_nearby_item2);
        tvNearby1  = findViewById(R.id.tv_nearby_item1);
        tvNearby2  = findViewById(R.id.tv_nearby_item2);

        View.OnClickListener todayClick = v -> {
            String place = tvToday1.getText().toString();
            if (v == imgToday2 || v == tvToday2) {
                place = tvToday2.getText().toString();
            }
            startPlaceDetailActivity(place);
        };
        imgToday1.setOnClickListener(todayClick);
        tvToday1.setOnClickListener(todayClick);
        imgToday2.setOnClickListener(todayClick);
        tvToday2.setOnClickListener(todayClick);

        View.OnClickListener nearbyClick = v -> {
            String place = tvNearby1.getText().toString();
            if (v == imgNearby2 || v == tvNearby2) {
                place = tvNearby2.getText().toString();
            }
            startPlaceDetailActivity(place);
        };
        imgNearby1.setOnClickListener(nearbyClick);
        tvNearby1.setOnClickListener(nearbyClick);
        imgNearby2.setOnClickListener(nearbyClick);
        tvNearby2.setOnClickListener(nearbyClick);

        // 랜덤 뽑기 UI 연결
        imgRandomPlace    = findViewById(R.id.img_random_place);
        tvRandomPlaceName = findViewById(R.id.tv_random_place_name);
        btnDiceRefresh    = findViewById(R.id.btn_dice_refresh);

        // 주사위 GIF 초기 로드
        Glide.with(this).asGif().load(R.drawable.ic_dice).into(btnDiceRefresh);

        // 주사위 클릭 시 랜덤 추천 애니메이션 시작
        btnDiceRefresh.setOnClickListener(v -> {
            Glide.with(this).asGif().load(R.drawable.ic_dice).into(btnDiceRefresh);
            db.collection("sports_locations").get().addOnSuccessListener(snapshot -> {
                List<QueryDocumentSnapshot> docs = new ArrayList<>();
                for (QueryDocumentSnapshot d : snapshot) docs.add(d);
                if (!docs.isEmpty()) {
                    startSlotAnimation(docs);
                } else {
                    Toast.makeText(this, "추천할 장소가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 날씨
        tvWeather = findViewById(R.id.tv_weather);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        // 프로필 패널
        tvProfileName     = findViewById(R.id.tv_profile_name);
        tvProfileEmail    = findViewById(R.id.tv_profile_email);
        tvProfileAge      = findViewById(R.id.tv_profile_age);
        tvProfileInterest = findViewById(R.id.tv_profile_interest);
        tvProfileSeason   = findViewById(R.id.tv_profile_season);
        loadUserProfileFromFirestore();

        // 위치 권한 요청 → 날씨, 추천 로드
        requestLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetRandomRecommendation();
    }

    private void resetRandomRecommendation() {
        // 슬롯 애니메이션 중지
        isSlotRunning = false;
        slotHandler.removeCallbacks(slotRunnable);

        // 기본 아이콘을 Glide 로 로드하여 배경 깨짐 방지
        Glide.with(this)
                .load(R.drawable.ic_random)
                .into(imgRandomPlace);

        // 초기 안내 문구
        tvRandomPlaceName.setText("주사위를 클릭하세요");

        // 상세 이동 리스너 해제
        tvRandomPlaceName.setOnClickListener(null);
        imgRandomPlace.setOnClickListener(null);
    }

    private void startSlotAnimation(List<QueryDocumentSnapshot> docs) {
        if (docs == null || docs.isEmpty()) return;
        placeDocs = docs;
        slotDelay = 50;
        isSlotRunning = true;
        slotHandler.post(slotRunnable);
    }

    private final Runnable slotRunnable = new Runnable() {
        @Override
        public void run() {
            // 애니메이션 중지 조건
            if (!isSlotRunning || placeDocs == null || placeDocs.isEmpty()) return;
            if (isFinishing() || isDestroyed()) return;

            int randomIndex = (int) (Math.random() * placeDocs.size());
            QueryDocumentSnapshot doc = placeDocs.get(randomIndex);
            String name = doc.getString("name");
            String imageUrl = doc.getString("image");

            tvRandomPlaceName.setText(name != null ? name : "이름 없음");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(MainActivity.this).load(imageUrl).into(imgRandomPlace);
            }

            slotDelay += 30;
            if (slotDelay < 500) {
                slotHandler.postDelayed(this, slotDelay);
            } else {
                isSlotRunning = false;
                tvRandomPlaceName.setOnClickListener(v -> startPlaceDetailActivity(name));
                imgRandomPlace.setOnClickListener(v -> startPlaceDetailActivity(name));
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        // 슬롯 애니메이션 중지
        isSlotRunning = false;
        slotHandler.removeCallbacks(slotRunnable);
    }

    private void startPlaceDetailActivity(String place) {
        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra("place_name", place);
        startActivity(intent);
    }

    // 오늘 추천 불러오기
    private void loadTodayRecommendations() {
        db.collection("sports_locations")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<QueryDocumentSnapshot> docs = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snapshot) docs.add(d);
                    Collections.shuffle(docs);
                    for (int i = 0; i < Math.min(2, docs.size()); i++) {
                        QueryDocumentSnapshot d = docs.get(i);
                        String name = d.getString("name");
                        String img  = d.getString("image");
                        ImageView iv = (i == 0 ? imgToday1 : imgToday2);
                        TextView tv  = (i == 0 ? tvToday1  : tvToday2);
                        tv.setText(name != null ? name : "이름 없음");
                        if (img != null && !img.isEmpty()) {
                            Glide.with(this).load(img).into(iv);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "오늘 추천 불러오기 실패", e));
    }

    // 주변 추천 불러오기
    private void loadNearbyRecommendations(Location loc) {
        db.collection("sports_locations")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<QueryDocumentSnapshot> candidates = new ArrayList<>();
                    float[] out = new float[1];
                    for (QueryDocumentSnapshot d : snapshot) {
                        Double la = d.getDouble("latitude");
                        Double lo = d.getDouble("longitude");
                        if (la != null && lo != null) {
                            android.location.Location.distanceBetween(
                                    loc.getLatitude(), loc.getLongitude(), la, lo, out
                            );
                            if (out[0] <= 50000) candidates.add(d);
                        }
                    }
                    Collections.sort(candidates, (a, b) -> {
                        float[] a1 = new float[1], b1 = new float[1];
                        android.location.Location.distanceBetween(
                                loc.getLatitude(), loc.getLongitude(),
                                a.getDouble("latitude"), a.getDouble("longitude"), a1
                        );
                        android.location.Location.distanceBetween(
                                loc.getLatitude(), loc.getLongitude(),
                                b.getDouble("latitude"), b.getDouble("longitude"), b1
                        );
                        return Float.compare(a1[0], b1[0]);
                    });
                    for (int i = 0; i < Math.min(2, candidates.size()); i++) {
                        QueryDocumentSnapshot d = candidates.get(i);
                        String name = d.getString("name");
                        String img  = d.getString("image");
                        ImageView iv = (i == 0 ? imgNearby1 : imgNearby2);
                        TextView tv  = (i == 0 ? tvNearby1  : tvNearby2);
                        tv.setText(name != null ? name : "이름 없음");
                        if (img != null && !img.isEmpty()) {
                            Glide.with(this).load(img).into(iv);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "주변 추천 불러오기 실패", e));
    }

    // 날씨 불러오기
    private void fetchWeather(double lat, double lon) {
        new Thread(() -> {
            try {
                String url = "https://api.openweathermap.org/data/2.5/weather"
                        + "?lat=" + lat
                        + "&lon=" + lon
                        + "&appid=" + apiKey
                        + "&lang=kr&units=metric";
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

    // 위치 권한 요청
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
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
                    // 오늘 추천은 항상 로드
                    loadTodayRecommendations();

                    if (location != null) {
                        fetchWeather(location.getLatitude(), location.getLongitude());
                        loadNearbyRecommendations(location);
                    } else {
                        tvWeather.setText("위치를 찾을 수 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "위치 가져오기 실패", e);
                    loadTodayRecommendations();
                });
    }

    // 퍼미션 결과 처리
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

    // 사용자 프로필 불러오기
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
                    String name = user.getDisplayName();
                    if (TextUtils.isEmpty(name)) {
                        String ndb = doc.getString("name");
                        if (!TextUtils.isEmpty(ndb)) name = ndb;
                        else if (user.getEmail() != null && user.getEmail().contains("@"))
                            name = user.getEmail().split("@")[0];
                        else name = "사용자";
                    }
                    tvProfileName.setText(name);
                    tvProfileEmail.setText(user.getEmail());

                    Long age = doc.getLong("age");
                    if (age != null) tvProfileAge.setText(age + "세");

                    List<String> cats = (List<String>) doc.get("interestCategory");
                    if (cats != null && !cats.isEmpty())
                        tvProfileInterest.setText(TextUtils.join(", ", cats));

                    List<String> seas = (List<String>) doc.get("interestSeasons");
                    if (seas != null && !seas.isEmpty())
                        tvProfileSeason.setText(TextUtils.join(", ", seas));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "프로필 불러오기 실패", Toast.LENGTH_SHORT).show()
                );
    }
}