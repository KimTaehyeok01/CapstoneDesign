package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;

public class SportsSelectActivity extends AppCompatActivity {

    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_sports); // 현재 레이아웃

        backButton = findViewById(R.id.back_button);

        // 뒤로가기 버튼 클릭 시 → AgeInputActivity로 이동
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AgeInputActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish(); // 현재 화면 종료
        });
    }
}
