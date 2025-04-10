package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.OnboardingActivity;
import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;

public class FindPwActivity extends AppCompatActivity {

    private EditText editEmail;
    private Button btnSendEmail, btnBack;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_pw);

        mAuth = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.editEmail);
        btnSendEmail = findViewById(R.id.btnSendEmail);
        btnBack = findViewById(R.id.btnBack);

        // 인증메일 전송
        btnSendEmail.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "이메일이 전송되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String error = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "오류 발생";
                            Toast.makeText(this, "이메일 전송 실패: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });


        // 뒤로가기 → 로그인
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });
    }
}
