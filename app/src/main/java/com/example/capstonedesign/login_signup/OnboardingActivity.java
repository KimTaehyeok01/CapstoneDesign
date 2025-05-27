package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.MainActivity;
import com.example.capstonedesign.R;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_page);

        View startHotspot = findViewById(R.id.button1);
        startHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("OnboardingActivity", "시작하기 버튼 클릭됨!");
                SharedPreferences prefs = getSharedPreferences("autoLogin", MODE_PRIVATE);
                boolean autoLogin = prefs.getBoolean("autoLoginEnabled", false);
                Intent intent;
                if (autoLogin) {
                    // 자동 로그인 설정이 되어 있으면 바로 메인으로
                    intent = new Intent(OnboardingActivity.this, MainActivity.class);
                } else {
                    // 자동 로그인 설정이 없으면 로그인 화면으로
                    intent = new Intent(OnboardingActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        });
    }
}
