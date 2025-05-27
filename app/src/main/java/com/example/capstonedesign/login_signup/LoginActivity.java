package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.MainActivity;
import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private ImageButton btnLogin, btnBack;
    private TextView btnRegister, btnFindPw;
    private CheckBox checkboxAutoLogin;

    private FirebaseAuth mAuth;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Auth 초기화
        mAuth = FirebaseAuth.getInstance();

        // SharedPreferences 초기화 (자동 로그인용)
        preferences = getSharedPreferences("autoLogin", MODE_PRIVATE);
        editor = preferences.edit();

        // UI 연결
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnFindPw = findViewById(R.id.btnFindPw);
        btnBack = findViewById(R.id.btnBack);
        checkboxAutoLogin = findViewById(R.id.checkboxAutoLogin);

        // 로그인 버튼 클릭 시
        btnLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String pw = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase 로그인 처리
            mAuth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                            // 자동 로그인 체크되어 있으면 저장
                            if (checkboxAutoLogin.isChecked()) {
                                editor.putBoolean("autoLoginEnabled", true);
                                editor.apply();
                            } else {
                                editor.remove("autoLoginEnabled");
                                editor.apply();
                            }
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String error = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "로그인 실패";
                            Toast.makeText(this, "로그인 실패: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
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
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 비밀번호 찾기 이동
        btnFindPw.setOnClickListener(v -> {
            Intent intent = new Intent(this, FindPwActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 뒤로가기 → 온보딩 이동
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });
    }
}
