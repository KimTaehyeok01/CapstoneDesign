package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.MainActivity;
import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeasonSelectActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button btnSave;
    private FrameLayout[] seasonButtons;

    private final String[] seasonNames = {"봄", "여름", "가을", "겨울"};
    private boolean[] selected = new boolean[4];

    private String userName;
    private String selectedGender;
    private int height;
    private int userAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_season);

        // 이전 화면에서 전달된 값 가져오기
        userName = getIntent().getStringExtra("userName");
        selectedGender = getIntent().getStringExtra("selectedGender");
        height = getIntent().getIntExtra("height", -1);
        userAge = getIntent().getIntExtra("userAge", -1);

        // 뷰 초기화
        backButton = findViewById(R.id.back_button);
        btnSave = findViewById(R.id.btn_save);

        seasonButtons = new FrameLayout[]{
                findViewById(R.id.season1_container),
                findViewById(R.id.season2_container),
                findViewById(R.id.season3_container),
                findViewById(R.id.season4_container)
        };

        // 계절 버튼 클릭 시 선택/해제 처리
        for (int i = 0; i < seasonButtons.length; i++) {
            final int index = i;
            seasonButtons[i].setOnClickListener(v -> {
                selected[index] = !selected[index];
                seasonButtons[index].setAlpha(selected[index] ? 0.5f : 1.0f);
            });
        }

        // 뒤로가기 버튼 클릭 시 스포츠 선택 화면으로 이동
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SportsSelectActivity.class);
            intent.putExtra("userName", userName);
            intent.putExtra("selectedGender", selectedGender);
            intent.putExtra("height", height);
            intent.putExtra("userAge", userAge);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        // 저장 버튼 클릭 시
        btnSave.setOnClickListener(v -> {
            List<String> selectedSeasons = new ArrayList<>();

            // 선택된 계절 수집
            for (int i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    selectedSeasons.add(seasonNames[i]);
                }
            }

            if (selectedSeasons.isEmpty()) {
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

            // 관심 계절 저장
            Map<String, Object> update = new HashMap<>();
            update.put("interestSeasons", selectedSeasons);

            db.collection("users").document(uid)
                    .set(update, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "계절 관심사 저장 완료!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}