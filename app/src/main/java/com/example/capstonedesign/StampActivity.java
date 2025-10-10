package com.example.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class StampActivity extends AppCompatActivity {

    private Button btnSportsTab, btnSeasonalTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stamps); // XML 레이아웃 로드

        // 뒤로가기 버튼
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 탭 버튼 ID 확인 및 연결
        btnSportsTab = findViewById(R.id.btn_sports_tab);
        btnSeasonalTab = findViewById(R.id.btn_seasonal_tab);

        // 탭 버튼 클릭 리스너 설정
        btnSportsTab.setOnClickListener(v -> setActiveTab(btnSportsTab));
        btnSeasonalTab.setOnClickListener(v -> setActiveTab(btnSeasonalTab));

        // 앱 시작 시 '스포츠' 탭을 기본으로 활성화
        setActiveTab(btnSportsTab);

        // ▼▼▼ [수정] 이 부분이 누락되었습니다. 아래 코드를 추가하세요. ▼▼▼
        // '다음 티어달성 분석표' 레이아웃 클릭 이벤트 처리
        LinearLayout btnTierAnalysis = findViewById(R.id.btn_tier_analysis);
        btnTierAnalysis.setOnClickListener(v -> {
            // TierAnalysisActivity로 이동하기 위한 Intent 생성
            Intent intent = new Intent(StampActivity.this, TierAnalysisActivity.class);
            // 새로운 Activity 시작
            startActivity(intent);
        });
        // ▲▲▲ 여기까지 추가 ▲▲▲
    }

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