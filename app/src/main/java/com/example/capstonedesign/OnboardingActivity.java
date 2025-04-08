package com.example.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.login_signup.LoginActivity;

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
                Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
