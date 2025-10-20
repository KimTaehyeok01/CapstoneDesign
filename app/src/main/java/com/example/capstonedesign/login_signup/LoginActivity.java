package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button; // 수정: Button import 추가
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.MainActivity;
import com.example.capstonedesign.R;
import com.example.capstonedesign.settings_information.MyFirebaseMessagingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button btnLogin;
    private ImageButton btnBack;
    private TextView btnRegister, btnFindPw;
    private CheckBox checkboxAutoLogin;

    private FirebaseAuth mAuth;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        preferences = getSharedPreferences("autoLogin", MODE_PRIVATE);
        editor = preferences.edit();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnFindPw = findViewById(R.id.btnFindPw);
        btnBack = findViewById(R.id.btnBack);
        checkboxAutoLogin = findViewById(R.id.checkboxAutoLogin);

        if (preferences.getBoolean("autoLoginEnabled", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String pw = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                            // 로그인 성공 시 FCM 토큰을 가져와 서버에 저장
                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
                                MyFirebaseMessagingService.sendRegistrationToServer(token);
                            });

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
                            String error = task.getException() != null ? task.getException().getMessage() : "로그인 실패";
                            Toast.makeText(this, "로그인 실패: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        editTextPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnLogin.performClick();
                return true;
            }
            return false;
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnFindPw.setOnClickListener(v -> {
            Intent intent = new Intent(this, FindPwActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });
    }
}