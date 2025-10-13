package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StampActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration userListener;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1005;

    // UI 요소 변수
    private ScrollView scrollView;
    private Button btnNearbyStamps;
    private TextView tvCurrentTierTitle, tvAthleticsCount, tvAthleticsTier, tvWaterSportsCount, tvWaterSportsTier, tvAirSportsCount, tvAirSportsTier;
    private ImageView tierBadge;
    private TextView btnCategoryAthletics, btnCategoryWater, btnCategoryAir;
    private TextView tvDynamicTierTag, tvNextTierProgressHeader, tvNextTierProgressFooter;

    private long currentLandCount = 0, currentSeaCount = 0, currentAirCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stamps);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        LinearLayout btnTierAnalysis = findViewById(R.id.btn_tier_analysis);
        btnTierAnalysis.setOnClickListener(v -> {
            startActivity(new Intent(StampActivity.this, TierAnalysisActivity.class));
        });
        LinearLayout btnVisitedList = findViewById(R.id.btn_visited_list);
        btnVisitedList.setOnClickListener(v -> {
            startActivity(new Intent(StampActivity.this, VisitedListActivity.class));
        });

        btnCategoryAthletics.setOnClickListener(v -> {
            updateSecondCardUI("land");
            updateCategoryButtonUI(btnCategoryAthletics);
        });
        btnCategoryWater.setOnClickListener(v -> {
            updateSecondCardUI("sea");
            updateCategoryButtonUI(btnCategoryWater);
        });
        btnCategoryAir.setOnClickListener(v -> {
            updateSecondCardUI("air");
            updateCategoryButtonUI(btnCategoryAir);
        });

        btnNearbyStamps.setOnClickListener(v -> requestLocationPermission());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStampData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (userListener != null) {
            userListener.remove();
        }
    }

    private void initializeViews() {
        scrollView = findViewById(R.id.scrollView);
        tvCurrentTierTitle = findViewById(R.id.tv_current_tier_title);
        tierBadge = findViewById(R.id.tier_badge);
        tvAthleticsCount = findViewById(R.id.tv_athletics_count);
        tvAthleticsTier = findViewById(R.id.tv_athletics_tier);
        tvWaterSportsCount = findViewById(R.id.tv_water_sports_count);
        tvWaterSportsTier = findViewById(R.id.tv_water_sports_tier);
        tvAirSportsCount = findViewById(R.id.tv_air_sports_count);
        tvAirSportsTier = findViewById(R.id.tv_air_sports_tier);
        btnNearbyStamps = findViewById(R.id.btn_nearby_stamps);

        btnCategoryAthletics = findViewById(R.id.btn_category_athletics);
        btnCategoryWater = findViewById(R.id.btn_category_water);
        btnCategoryAir = findViewById(R.id.btn_category_air);
        tvDynamicTierTag = findViewById(R.id.tv_dynamic_tier_tag);
        tvNextTierProgressHeader = findViewById(R.id.tv_next_tier_progress_header);
        tvNextTierProgressFooter = findViewById(R.id.tv_next_tier_progress_footer);
    }

    private void loadStampData() {
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userListener = userRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.w("StampActivity", "Listen failed.", e);
                return;
            }

            if (scrollView == null) return;

            scrollView.post(() -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Long> stampCounts = (Map<String, Long>) documentSnapshot.get("stampCounts");
                    if (stampCounts == null) stampCounts = new HashMap<>();

                    this.currentLandCount = stampCounts.getOrDefault("land", 0L);
                    this.currentSeaCount = stampCounts.getOrDefault("sea", 0L);
                    this.currentAirCount = stampCounts.getOrDefault("air", 0L);

                    updateFirstCardUI(currentLandCount, currentSeaCount, currentAirCount);
                    updateCategoryButtonUI(btnCategoryAthletics);
                    updateSecondCardUI("land");

                } else {
                    updateFirstCardUI(0, 0, 0);
                    updateCategoryButtonUI(btnCategoryAthletics);
                    updateSecondCardUI("land");
                }
            });
        });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            findAndShowNearestPlace();
        }
    }

    private void findAndShowNearestPlace() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("sports_locations").get().addOnSuccessListener(queryDocumentSnapshots -> {
                DocumentSnapshot nearestPlace = null;
                float minDistance = Float.MAX_VALUE;

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Double lat = doc.getDouble("latitude");
                    Double lon = doc.getDouble("longitude");
                    if (lat != null && lon != null) {
                        float[] results = new float[1];
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat, lon, results);
                        float distance = results[0];

                        if (distance < minDistance) {
                            minDistance = distance;
                            nearestPlace = doc;
                        }
                    }
                }

                if (nearestPlace != null) {
                    Double lat = nearestPlace.getDouble("latitude");
                    Double lon = nearestPlace.getDouble("longitude");
                    String name = nearestPlace.getString("name");

                    if(lat != null && lon != null && name != null) {
                        Intent intent = new Intent(StampActivity.this, MapActivity.class);
                        intent.putExtra("latitude", lat);
                        intent.putExtra("longitude", lon);
                        intent.putExtra("place_name", name);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "주변에 등록된 장소가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findAndShowNearestPlace();
            } else {
                Toast.makeText(this, "기능을 사용하려면 위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateFirstCardUI(long land, long sea, long air) {
        tvAthleticsCount.setText(String.valueOf(land));
        tvAthleticsTier.setText(getTierForCount(land).toLowerCase());
        tvWaterSportsCount.setText(String.valueOf(sea));
        tvWaterSportsTier.setText(getTierForCount(sea).toLowerCase());
        tvAirSportsCount.setText(String.valueOf(air));
        tvAirSportsTier.setText(getTierForCount(air).toLowerCase());

        long maxCount = Math.max(land, Math.max(sea, air));
        String highestTier = getTierForCount(maxCount);

        tvCurrentTierTitle.setText("현재 티어: " + highestTier);
        tierBadge.setImageResource(getTierBadgeResource(highestTier));
    }

    private void updateSecondCardUI(String category) {
        long currentCount;
        switch (category) {
            case "sea": currentCount = this.currentSeaCount; break;
            case "air": currentCount = this.currentAirCount; break;
            default: currentCount = this.currentLandCount; break;
        }

        String currentTier = getTierForCount(currentCount);

        if (tvDynamicTierTag != null) {
            tvDynamicTierTag.setText(currentTier);
            tvDynamicTierTag.setBackgroundResource(getTierBackgroundResource(currentTier));
        }

        long nextTierRequirement = getNextTierRequirement(currentCount);
        if (nextTierRequirement == -1) {
            tvNextTierProgressHeader.setText("최고 티어 달성!");
            tvNextTierProgressFooter.setText("모든 스탬프를 모았습니다!");
        } else {
            long remaining = nextTierRequirement - currentCount;
            tvNextTierProgressHeader.setText("다음 티어까지 " + remaining);
            tvNextTierProgressFooter.setText("다음 티어까지 " + remaining + "개 남았어요");
        }
    }

    private void updateCategoryButtonUI(TextView selectedButton) {
        btnCategoryAthletics.setTypeface(null, Typeface.NORMAL);
        btnCategoryAthletics.setTextColor(ContextCompat.getColor(this, R.color.inactive_button_text));

        btnCategoryWater.setTypeface(null, Typeface.NORMAL);
        btnCategoryWater.setTextColor(ContextCompat.getColor(this, R.color.inactive_button_text));

        btnCategoryAir.setTypeface(null, Typeface.NORMAL);
        btnCategoryAir.setTextColor(ContextCompat.getColor(this, R.color.inactive_button_text));

        selectedButton.setTypeface(null, Typeface.BOLD);
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.black));
    }

    private long getNextTierRequirement(long count) {
        if (count < 3) return 3;
        if (count < 6) return 6;
        if (count < 9) return 9;
        if (count < 12) return 12;
        if (count < 15) return 15;
        return -1;
    }

    private int getTierBackgroundResource(String tier) {
        if(tier == null) return R.drawable.rounded_bronze_background;
        switch (tier) {
            case "Master": return R.drawable.rounded_master_background;
            case "Platinum": return R.drawable.rounded_platinum_background;
            case "Gold": return R.drawable.rounded_gold_background;
            case "Silver": return R.drawable.rounded_silver_background;
            default: return R.drawable.rounded_bronze_background;
        }
    }

    private String getTierForCount(long count) {
        if (count >= 15) return "Master";
        if (count >= 12) return "Platinum";
        if (count >= 9) return "Gold";
        if (count >= 6) return "Silver";
        if (count >= 3) return "Bronze";
        return "Unranked";
    }

    private int getTierBadgeResource(String tier) {
        if(tier == null) return R.drawable.bronze_badge;
        switch (tier) {
            case "Master": return R.drawable.master_badge;
            case "Platinum": return R.drawable.platinum_badge;
            case "Gold": return R.drawable.gold_badge;
            case "Silver": return R.drawable.silver_badge;
            case "Bronze": return R.drawable.bronze_badge;
            default: return R.drawable.bronze_badge;
        }
    }
}