package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AgeInputActivity extends AppCompatActivity {

    private EditText editAge;
    private ImageButton backButton, nextButton;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_age);

        userName = getIntent().getStringExtra("userName");

        editAge = findViewById(R.id.edit_age);
        backButton = findViewById(R.id.back_button);
        nextButton = findViewById(R.id.next_button);

        // 뒤로가기 버튼 → 회원가입(SignUpActivity)로 이동
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        // 다음 버튼 → 나이 저장 후 레저 선택으로 이동
        nextButton.setOnClickListener(v -> {
            String ageStr = editAge.getText().toString().trim();

            if (ageStr.isEmpty()) {
                Toast.makeText(this, "나이를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "올바른 나이를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> userData = new HashMap<>();
            userData.put("age", age);
            userData.put("name", userName); // null 주의

            db.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "회원정보 저장 완료!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, SportsSelectActivity.class);
                        intent.putExtra("userName", userName);
                        intent.putExtra("userAge", age);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
