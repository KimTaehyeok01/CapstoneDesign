package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.MapActivity; // ✅ MapActivity import 추가
import com.example.capstonedesign.MainActivity;
import com.example.capstonedesign.OnboardingActivity;
import com.example.capstonedesign.R;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button btnLogin, btnRegister, btnFindPw, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI 연결
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnFindPw = findViewById(R.id.btnFindPw);
        btnBack = findViewById(R.id.btnBack);

        // 로그인 버튼 클릭 시
        btnLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String pw = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 엔터 누르면 로그인
        editTextPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnLogin.performClick();
                return true;
            }
            return false;
        });

        // 회원가입 이동
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });

        //  비밀번호 찾기 버튼 → 아직 구현 안됨
        btnFindPw.setOnClickListener(v -> {
            Toast.makeText(this, "비밀번호 찾기 기능은 아직 구현되지 않았습니다.", Toast.LENGTH_SHORT).show();
            // 나중에 비밀번호 찾기 액티비티 연결
        });

        // ← 뒤로가기 → 온보딩
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
//awwe
