package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private EditText editEmail, editPassword, editConfirmPassword, editName;
    private ImageButton btnBack;
    private Button btnSignUpSubmit;

    private FirebaseAuth mAuth;
    private static final String TAG = "SignUpActivity";

    // 비밀번호 정규식: 6자리 이상, 영문과 숫자 1개 이상 포함
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        mAuth = FirebaseAuth.getInstance();

        // 뷰 초기화 (수정된 XML ID에 맞게 변경)
        editEmail = findViewById(R.id.editEmailId);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        editName = findViewById(R.id.editName);
        btnBack = findViewById(R.id.btnBack);
        btnSignUpSubmit = findViewById(R.id.btnSignUpSubmit);

        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        // 회원가입 완료 버튼
        btnSignUpSubmit.setOnClickListener(v -> attemptSignUp());
    }

    // 회원가입 시도 로직
    private void attemptSignUp() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        // 순차적으로 유효성 검사 수행
        if (!validateName(name) || !validateEmail(email) || !validatePassword(password) || !validateConfirmPassword(password, confirmPassword)) {
            return; // 하나라도 실패하면 중단
        }

        // Firebase를 통한 회원가입 시도
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();
                            user.updateProfile(profileUpdates);
                        }

                        Intent intent = new Intent(SignUpActivity.this, GenderSelectActivity.class);
                        intent.putExtra("userName", name); // 이름 전달
                        startActivity(intent);
                        finish();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "알 수 없는 오류";
                        Toast.makeText(this, "회원가입 실패: " + error, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "회원가입 실패", task.getException());
                    }
                });
    }

    // 1. 이름 유효성 검사
    private boolean validateName(String name) {
        if (name.isEmpty()) {
            editName.setError("이름을 입력해주세요.");
            editName.requestFocus();
            return false;
        }
        if (name.length() < 2) {
            editName.setError("이름은 2글자 이상이어야 합니다.");
            editName.requestFocus();
            return false;
        }
        editName.setError(null);
        return true;
    }

    // 2. 이메일 유효성 검사
    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            editEmail.setError("이메일을 입력해주세요.");
            editEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("올바른 이메일 형식이 아닙니다.");
            editEmail.requestFocus();
            return false;
        }
        editEmail.setError(null);
        return true;
    }

    // 3. 비밀번호 유효성 검사
    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            editPassword.setError("비밀번호를 입력해주세요.");
            editPassword.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            editPassword.setError("비밀번호는 6자 이상이어야 합니다.");
            editPassword.requestFocus();
            return false;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            editPassword.setError("영문과 숫자를 모두 포함해야 합니다.");
            editPassword.requestFocus();
            return false;
        }
        editPassword.setError(null);
        return true;
    }

    // 4. 비밀번호 확인 유효성 검사
    private boolean validateConfirmPassword(String password, String confirmPassword) {
        if (confirmPassword.isEmpty()) {
            editConfirmPassword.setError("비밀번호를 다시 입력해주세요.");
            editConfirmPassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("비밀번호가 일치하지 않습니다.");
            editConfirmPassword.requestFocus();
            return false;
        }
        editConfirmPassword.setError(null);
        return true;
    }
}