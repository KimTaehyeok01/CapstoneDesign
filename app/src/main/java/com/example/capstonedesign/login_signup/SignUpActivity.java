package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText editEmailId, editEmailDomain, editPassword, editConfirmPassword, editName;
    private ImageButton btnBack, btnSignUpSubmit;

    private FirebaseAuth mAuth;
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();

        // 뷰 초기화
        editEmailId = findViewById(R.id.editEmailId);
        editEmailDomain = findViewById(R.id.editEmailDomain);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        editName = findViewById(R.id.editName);

        btnBack = findViewById(R.id.btnBack);
        btnSignUpSubmit = findViewById(R.id.btnSignUpSubmit);

        // 뒤로가기 버튼 클릭 시 로그인 화면으로 이동
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        // 회원가입 완료 버튼 클릭 이벤트
        btnSignUpSubmit.setOnClickListener(v -> {
            try {
                // 입력값 가져오기
                String emailId = editEmailId.getText().toString().trim();
                String emailDomain = editEmailDomain.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                String confirmPassword = editConfirmPassword.getText().toString().trim();
                String name = editName.getText().toString().trim();

                // 이메일 도메인이 비어있으면 힌트 사용
                if (emailDomain.isEmpty()) {
                    CharSequence hint = editEmailDomain.getHint();
                    if (hint != null) {
                        emailDomain = hint.toString().trim();
                    } else {
                        Toast.makeText(this, "이메일 도메인을 입력해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                String email = emailId + "@" + emailDomain;

                // 입력값 유효성 검사
                if (emailId.isEmpty() || emailDomain.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
                    Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "이메일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
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

                // Firebase를 통한 회원가입 시도
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
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

            } catch (Exception e) {
                Toast.makeText(this, "예기치 못한 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "회원가입 처리 중 예외", e);
            }
        });
    }
}
