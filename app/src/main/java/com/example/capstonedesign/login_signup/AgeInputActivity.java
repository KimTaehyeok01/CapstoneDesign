package com.example.capstonedesign.login_signup;

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

    private String userName;  // 전달받은 이름 저장용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_age);

        // 👇 이전 화면(SignUpActivity)에서 전달된 이름 받기
        userName = getIntent().getStringExtra("userName");

        editAge = findViewById(R.id.edit_age);
        backButton = findViewById(R.id.back_button);
        nextButton = findViewById(R.id.next_button);

        backButton.setOnClickListener(v -> finish());

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

            // 현재 로그인된 사용자 확인
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // 저장할 데이터 구성
            Map<String, Object> userData = new HashMap<>();
            userData.put("age", age);
            userData.put("name", userName); // 이름도 함께 저장

            // Firestore에 저장
            db.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "회원정보 저장 완료!", Toast.LENGTH_SHORT).show();
                        // TODO: 다음 화면으로 이동하거나 메인 화면으로 전환
                        finish(); // 일단 현재 액티비티 종료
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
