package com.example.capstonedesign;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StampActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ListenerRegistration userListener;

    // UI 요소 변수
    private ScrollView scrollView;
    private Button btnSportsTab, btnSeasonalTab;
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

        initializeViews();

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        btnSportsTab.setOnClickListener(v -> setActiveTab(btnSportsTab));
        btnSeasonalTab.setOnClickListener(v -> setActiveTab(btnSeasonalTab));

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
        btnSportsTab = findViewById(R.id.btn_sports_tab);
        btnSeasonalTab = findViewById(R.id.btn_seasonal_tab);
        tvCurrentTierTitle = findViewById(R.id.tv_current_tier_title);
        tierBadge = findViewById(R.id.tier_badge);
        tvAthleticsCount = findViewById(R.id.tv_athletics_count);
        tvAthleticsTier = findViewById(R.id.tv_athletics_tier);
        tvWaterSportsCount = findViewById(R.id.tv_water_sports_count);
        tvWaterSportsTier = findViewById(R.id.tv_water_sports_tier);
        tvAirSportsCount = findViewById(R.id.tv_air_sports_count);
        tvAirSportsTier = findViewById(R.id.tv_air_sports_tier);

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


            if (scrollView != null) {
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
            }
        });
    }

    private void updateFirstCardUI(long land, long sea, long air) {
        tvAthleticsCount.setText(String.valueOf(land));
        tvAthleticsTier.setText(getTierForCount(land).toLowerCase());
        tvWaterSportsCount.setText(String.valueOf(sea));
        tvWaterSportsTier.setText(getTierForCount(sea).toLowerCase());
        tvAirSportsCount.setText(String.valueOf(air));
        tvAirSportsTier.setText(getTierForCount(air).toLowerCase());

        long maxCount = Collections.max(java.util.Arrays.asList(land, sea, air));
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
        switch (tier) {
            case "Master": return R.drawable.master_badge;
            case "Platinum": return R.drawable.platinum_badge;
            case "Gold": return R.drawable.gold_badge;
            case "Silver": return R.drawable.silver_badge;
            case "Bronze": return R.drawable.bronze_badge;
            default: return R.drawable.bronze_badge;
        }
    }

    private void setActiveTab(Button activeButton) {
        btnSportsTab.setBackgroundResource(R.drawable.rounded_left_button_inactive);
        btnSportsTab.setTextColor(ContextCompat.getColor(this, R.color.black));
        btnSeasonalTab.setBackgroundResource(R.drawable.rounded_right_button_inactive);
        btnSeasonalTab.setTextColor(ContextCompat.getColor(this, R.color.black));

        if (activeButton.getId() == R.id.btn_sports_tab) {
            btnSportsTab.setBackgroundResource(R.drawable.rounded_left_button_active);
            btnSportsTab.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else if (activeButton.getId() == R.id.btn_seasonal_tab) {
            btnSeasonalTab.setBackgroundResource(R.drawable.rounded_right_button_active);
            btnSeasonalTab.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }
}