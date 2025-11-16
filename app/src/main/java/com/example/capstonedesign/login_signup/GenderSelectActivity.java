package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class GenderSelectActivity extends AppCompatActivity {

    private FrameLayout maleButton, femaleButton;
    private ImageButton backButton;
    private Button nextButton;

    private String selectedGender = null;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_gender);

        // 이전 화면에서 전달된 사용자 이름 가져오기
        userName = getIntent().getStringExtra("userName");

        // 뷰 초기화
        maleButton = findViewById(R.id.male_button);
        femaleButton = findViewById(R.id.female_button);
        backButton = findViewById(R.id.back_button);
        nextButton = findViewById(R.id.next_button);

        // 남성 버튼 클릭 시
        maleButton.setOnClickListener(v -> {
            selectedGender = "남성";
            maleButton.setAlpha(0.5f);
            femaleButton.setAlpha(1.0f);
        });

        // 여성 버튼 클릭 시
        femaleButton.setOnClickListener(v -> {
            selectedGender = "여성";
            femaleButton.setAlpha(0.5f);
            maleButton.setAlpha(1.0f);
        });

        // 뒤로가기 버튼 클릭 시 회원가입 화면으로 이동
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        // 다음 버튼 클릭 시
        nextButton.setOnClickListener(v -> {
            if (selectedGender == null) {
                Toast.makeText(this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // 사용자 데이터 생성
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", userName);
            userData.put("gender", selectedGender);

            // Firestore에 사용자 데이터 저장
            db.collection("users").document(uid)
                    .set(userData, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "성별 저장 완료!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, InputHeightActivity.class);
                        intent.putExtra("userName", userName);
                        intent.putExtra("selectedGender", selectedGender);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}