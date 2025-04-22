package com.example.capstonedesign.settings_information;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;

public class PushSettingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PushSettingsPrefs";
    private static final String KEY_LOCATION_ON = "location_on";
    private static final String KEY_NOTICE_ON = "notice_on";

    private boolean isLocationOn = true;
    private boolean isNoticeOn = true;

    private ImageButton btnLocationToggle;
    private ImageButton btnNoticeToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_notification);

        // 저장된 설정 불러오기
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isLocationOn = prefs.getBoolean(KEY_LOCATION_ON, true);
        isNoticeOn = prefs.getBoolean(KEY_NOTICE_ON, true);

        // 뒤로가기 버튼
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 토글 버튼 연결
        btnLocationToggle = findViewById(R.id.btnLocationToggle);
        btnNoticeToggle = findViewById(R.id.btnNoticeToggle);

        // 초기 상태 설정
        updateToggleState(btnLocationToggle, isLocationOn);
        updateToggleState(btnNoticeToggle, isNoticeOn);

        // 위치 서비스 토글
        btnLocationToggle.setOnClickListener(v -> {
            isLocationOn = !isLocationOn;
            updateToggleState(btnLocationToggle, isLocationOn);
        });

        // 공지사항 토글
        btnNoticeToggle.setOnClickListener(v -> {
            isNoticeOn = !isNoticeOn;
            updateToggleState(btnNoticeToggle, isNoticeOn);
        });

        // 확인 버튼 눌렀을 때 SharedPreferences 저장
        ImageButton btnSubmitConfirm = findViewById(R.id.btnSubmitConfirm);
        btnSubmitConfirm.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putBoolean(KEY_LOCATION_ON, isLocationOn);
            editor.putBoolean(KEY_NOTICE_ON, isNoticeOn);
            editor.apply(); // 비동기 저장

            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    // 버튼 이미지 업데이트
    private void updateToggleState(ImageButton button, boolean isOn) {
        button.setImageResource(isOn ? R.drawable.ic_on_button : R.drawable.ic_off_button);
    }
}
