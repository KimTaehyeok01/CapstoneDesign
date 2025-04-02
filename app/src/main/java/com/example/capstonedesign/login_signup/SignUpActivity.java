package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;

public class SignUpActivity extends AppCompatActivity {

    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up); // sign_up.xml 연결

        // ← 꺾새 버튼 연결
        btnBack = findViewById(R.id.btnBack);

        // 꺾새 버튼 클릭 시 LoginActivity로 이동
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // 현재 액티비티 종료
        });
    }
}
