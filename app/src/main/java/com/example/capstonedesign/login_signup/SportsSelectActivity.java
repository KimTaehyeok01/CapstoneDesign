package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SportsSelectActivity extends AppCompatActivity {

    private ImageButton backButton, btnSave;
    private ImageButton[] sportButtons;

    private final String[] sportCategories = {"육상 스포츠", "해상 스포츠", "항공 스포츠"};
    private boolean[] selected = new boolean[3];

    private String userName;
    private String selectedGender;
    private int height;
    private int userAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_sports);

        // 이전 화면에서 전달된 값 가져오기
        userName = getIntent().getStringExtra("userName");
        selectedGender = getIntent().getStringExtra("selectedGender");
        height = getIntent().getIntExtra("height", -1);
        userAge = getIntent().getIntExtra("userAge", -1);

        // 뷰 초기화
        backButton = findViewById(R.id.back_button);
        btnSave = findViewById(R.id.btn_save);

        sportButtons = new ImageButton[]{
                findViewById(R.id.group1),
                findViewById(R.id.group2),
                findViewById(R.id.group3)
        };

        // 스포츠 카테고리 버튼 클릭 시 선택/해제 처리
        for (int i = 0; i < sportButtons.length; i++) {
            final int index = i;
            sportButtons[i].setOnClickListener(v -> {
                selected[index] = !selected[index];
                sportButtons[index].setAlpha(selected[index] ? 0.5f : 1.0f);
            });
        }

        // 뒤로가기 버튼 클릭 시 나이 입력 화면으로 이동
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AgeInputActivity.class);
            intent.putExtra("userName", userName);
            intent.putExtra("selectedGender", selectedGender);
            intent.putExtra("height", height);
            intent.putExtra("userAge", userAge);
            startActivity(intent);
            finish();
        });

        // 저장 버튼 클릭 시
        btnSave.setOnClickListener(v -> {
            List<String> selectedCategories = new ArrayList<>();

            // 선택된 카테고리 수집
            for (int i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    selectedCategories.add(sportCategories[i]);
                }
            }

            if (selectedCategories.isEmpty()) {
                Toast.makeText(this, "하나 이상 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // 선택한 관심 스포츠 저장
            Map<String, Object> update = new HashMap<>();
            update.put("interestCategory", selectedCategories);

            db.collection("users").document(uid)
                    .set(update, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "관심 스포츠 저장 완료!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, SeasonSelectActivity.class);
                        intent.putExtra("userName", userName);
                        intent.putExtra("selectedGender", selectedGender);
                        intent.putExtra("height", height);
                        intent.putExtra("userAge", userAge);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
