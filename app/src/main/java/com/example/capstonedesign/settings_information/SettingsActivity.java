package com.example.capstonedesign.settings_information;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.settings_information.InformationActivity;
import com.example.capstonedesign.settings_information.PushSettingActivity;
import com.example.capstonedesign.settings_information.InterestChangeActivity;
import com.example.capstonedesign.R;
import com.example.capstonedesign.BuildConfig;

public class SettingsActivity extends AppCompatActivity {
    private boolean isVersionVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);  // settings.xml 사용

        // 뒤로가기 버튼
        ImageButton btnSettingBack = findViewById(R.id.btnSettingBack);
        btnSettingBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // 내정보 버튼 클릭 시 InformationActivity로 전환
        LinearLayout btnMyInfo = findViewById(R.id.btn_my_info);
        btnMyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, InformationActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // 관심사 변경 버튼 클릭 시 InterestChangeActivity로 전환
        LinearLayout btnChangeInterest = findViewById(R.id.btn_change_interest);
        btnChangeInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, InterestChangeActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // 푸시알림설정 버튼 클릭 시 PushSettingActivity로 이동
        LinearLayout btnPushSetting = findViewById(R.id.btn_push_setting);
        btnPushSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, PushSettingActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // 앱 버전 토글 텍스트
        TextView versionText = findViewById(R.id.text_app_version);
        TextView versionValue = findViewById(R.id.text_version_value);

        // BuildConfig.VERSION_NAME을 통해 자동 버전 텍스트 설정
        versionValue.setText("v" + BuildConfig.VERSION_NAME);
        versionValue.setVisibility(View.GONE);

        versionText.setOnClickListener(v -> {
            isVersionVisible = !isVersionVisible;
            versionValue.setVisibility(isVersionVisible ? View.VISIBLE : View.GONE);
        });
    }
}
