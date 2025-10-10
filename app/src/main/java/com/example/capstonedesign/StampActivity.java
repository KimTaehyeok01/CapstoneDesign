package com.example.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class StampActivity extends AppCompatActivity {

    private Button btnSportsTab, btnSeasonalTab;

    // [수정] 버튼 기능이 사라졌으므로 동적 UI 변경 관련 변수들 대부분 삭제
    // private TextView tvCurrentTierTitle, tvDynamicTierTag;
    // private ImageView ivTierBadge;
    // ... (종목별 TextView 변수들도 삭제)

    // [수정] 데이터 저장을 위한 Map 삭제
    // private Map<String, String> tierData = new HashMap<>();
    // private Map<String, Integer> countData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stamps);

        // 뒤로가기 버튼
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 탭 버튼 리스너 설정
        btnSportsTab = findViewById(R.id.btn_sports_tab);
        btnSeasonalTab = findViewById(R.id.btn_seasonal_tab);
        btnSportsTab.setOnClickListener(v -> setActiveTab(btnSportsTab));
        btnSeasonalTab.setOnClickListener(v -> setActiveTab(btnSeasonalTab));
        setActiveTab(btnSportsTab);

        // '다음 티어달성 분석표' 클릭 이벤트
        LinearLayout btnTierAnalysis = findViewById(R.id.btn_tier_analysis);
        btnTierAnalysis.setOnClickListener(v -> {
            startActivity(new Intent(StampActivity.this, TierAnalysisActivity.class));
        });

        // [수정] 종목별 클릭 이벤트 및 관련 함수 호출 모두 삭제
        // LinearLayout btnAthletics = findViewById(R.id.btn_athletics);
        // ... (관련 코드 모두 삭제)
    }

    // [수정] 불필요해진 함수들 삭제
    // private void initializeData() { ... }
    // private void initializeViews() { ... }
    // private void updateTierView(String sportType) { ... }

    private void setActiveTab(Button activeButton) {
        // 1. 모든 탭 버튼을 '비활성' 상태의 디자인으로 초기화합니다.
        btnSportsTab.setBackgroundResource(R.drawable.rounded_left_button_inactive);
        btnSportsTab.setTextColor(ContextCompat.getColor(this, R.color.black));

        btnSeasonalTab.setBackgroundResource(R.drawable.rounded_right_button_inactive);
        btnSeasonalTab.setTextColor(ContextCompat.getColor(this, R.color.black));

        // 2. 현재 클릭된 버튼만 '활성' 상태의 디자인으로 변경합니다.
        if (activeButton.getId() == R.id.btn_sports_tab) {
            btnSportsTab.setBackgroundResource(R.drawable.rounded_left_button_active);
            btnSportsTab.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else if (activeButton.getId() == R.id.btn_seasonal_tab) {
            btnSeasonalTab.setBackgroundResource(R.drawable.rounded_right_button_active);
            btnSeasonalTab.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }
}