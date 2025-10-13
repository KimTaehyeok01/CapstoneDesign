package com.example.capstonedesign.settings_information;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.capstonedesign.R;

public class PushSettingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PushSettingsPrefs";
    private static final String KEY_LOCATION_ON = "location_on";
    private static final String KEY_NOTICE_ON = "notice_on";

    private boolean isLocationOn;
    private boolean isNoticeOn;

    private SwitchCompat switchLocation;
    private SwitchCompat switchNotice;
    private Button btnSubmitConfirm;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_notification);

        // UI 요소 연결
        switchLocation = findViewById(R.id.switch_location);
        switchNotice = findViewById(R.id.switch_notice);
        btnSubmitConfirm = findViewById(R.id.btnSubmitConfirm);
        btnBack = findViewById(R.id.btnBack);

        // 저장된 설정 불러오기
        loadSettings();

        // 리스너 설정
        setupListeners();
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isLocationOn = prefs.getBoolean(KEY_LOCATION_ON, true);
        isNoticeOn = prefs.getBoolean(KEY_NOTICE_ON, true);

        // 스위치 초기 상태 설정
        switchLocation.setChecked(isLocationOn);
        switchNotice.setChecked(isNoticeOn);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // 스위치 상태 변경 시 변수 값 업데이트
        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isLocationOn = isChecked;
        });

        switchNotice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isNoticeOn = isChecked;
        });

        // 확인 버튼 클릭 시 설정 저장
        btnSubmitConfirm.setOnClickListener(v -> {
            saveSettings();
            Toast.makeText(this, "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_LOCATION_ON, isLocationOn);
        editor.putBoolean(KEY_NOTICE_ON, isNoticeOn);
        editor.apply();
    }
}