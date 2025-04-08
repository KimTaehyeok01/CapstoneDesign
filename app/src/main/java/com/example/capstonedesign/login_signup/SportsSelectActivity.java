package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.MainActivity;
import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SportsSelectActivity extends AppCompatActivity {

    private ImageButton backButton, btnSave;
    private ImageButton[] sportButtons;

    private final String[] sportNames = {
            "클라이밍", "서핑", "패러글라이딩", "카약", "행글라이딩",
            "스키", "아이스 스케이트", "카트레이싱", "제트스키"
    };

    private boolean[] selected = new boolean[9]; // 각 버튼 선택 상태 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_sports);

        backButton = findViewById(R.id.back_button);
        btnSave = findViewById(R.id.btn_save);

        // 9개 버튼 배열에 등록
        sportButtons = new ImageButton[]{
                findViewById(R.id.group1),
                findViewById(R.id.group2),
                findViewById(R.id.group3),
                findViewById(R.id.group4),
                findViewById(R.id.group5),
                findViewById(R.id.group6),
                findViewById(R.id.group7),
                findViewById(R.id.group8),
                findViewById(R.id.group9)
        };

        // 각 버튼에 클릭 이벤트 부여
        for (int i = 0; i < sportButtons.length; i++) {
            final int index = i;

            sportButtons[i].setOnClickListener(v -> {
                selected[index] = !selected[index];
                // 배경 강조: 선택시 알파값 낮추기
                sportButtons[index].setAlpha(selected[index] ? 0.5f : 1.0f);
            });
        }

        // 뒤로가기 → 나이입력화면으로 이동
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AgeInputActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        // 저장 버튼
        btnSave.setOnClickListener(v -> {
            ArrayList<String> selectedSports = new ArrayList<>();
            for (int i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    selectedSports.add(sportNames[i]);
                }
            }

            if (selectedSports.size() < 3) {
                Toast.makeText(this, "최소 3개 선택해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> update = new HashMap<>();
            update.put("interests", selectedSports); // 관심사 배열로 저장

            db.collection("users").document(uid)
                    .update(update)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "관심사 저장 완료!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
    return;
}