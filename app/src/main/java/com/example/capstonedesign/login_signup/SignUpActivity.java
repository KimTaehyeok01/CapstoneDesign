package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.OnboardingActivity;
import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText editEmailId, editEmailDomain, editPassword, editConfirmPassword, editName;
    private Button btnBack, btnSignUpSubmit, btnTogglePassword, btnToggleConfirmPassword;

    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // FirebaseAuth 초기화
        mAuth = FirebaseAuth.getInstance();

        // XML 뷰 연결
        editEmailId = findViewById(R.id.editEmailId);
        editEmailDomain = findViewById(R.id.editEmailDomain);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        editName = findViewById(R.id.editName);

        btnBack = findViewById(R.id.btnBack);
        btnSignUpSubmit = findViewById(R.id.btnSignUpSubmit);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);

        // 뒤로가기 → 로그인 화면
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); // 애니메이션
            finish();
        });

        // 비밀번호 보기 토글
        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(editPassword, isPasswordVisible);
        });

        btnToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(editConfirmPassword, isConfirmPasswordVisible);
        });

        // 회원가입 버튼 클릭
        btnSignUpSubmit.setOnClickListener(v -> {
            String emailId = editEmailId.getText().toString().trim();
            String emailDomain = editEmailDomain.getText().toString().trim();
            String email = emailId + "@" + emailDomain;

            String password = editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();
            String name = editName.getText().toString().trim();

            if (emailId.isEmpty() || emailDomain.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase 회원가입 요청
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                            // ✅ 로그인 화면으로 이동
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "알 수 없는 오류";
                            Toast.makeText(this, "회원가입 실패: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    // 비밀번호 토글 함수
    private void togglePasswordVisibility(EditText editText, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editText.setSelection(editText.getText().length()); // 커서 마지막으로 이동

    }
}
