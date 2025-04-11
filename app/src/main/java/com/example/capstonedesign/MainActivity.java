package com.example.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class MainActivity extends AppCompatActivity {

    private ImageButton navHome, navSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // activity_main.xml (위의 전체 레이아웃)

        // 시스템 인셋 적용 (노치 등)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 검색바 클릭 이벤트 (이미 구현되어 있음)
        View searchBar = findViewById(R.id.main_search_bar);
        searchBar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 하단 네비게이션 버튼 참조
        navHome = findViewById(R.id.nav_home);
        navSetting = findViewById(R.id.nav_setting);

        // 홈 버튼: 클릭 시 MainActivity를 새로고침 (재실행)합니다.
        navHome.setOnClickListener(v -> {
            // 현재 액티비티를 종료한 뒤, 동일한 MainActivity를 다시 시작합니다.
            finish();
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 설정 버튼: 클릭 시 SettingsActivity로 전환
        navSetting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
}
