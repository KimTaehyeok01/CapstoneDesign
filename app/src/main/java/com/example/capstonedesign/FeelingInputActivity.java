package com.example.capstonedesign;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeelingInputActivity extends AppCompatActivity {

    // UI 요소 선언
    private ImageButton btnBack;
    private SeekBar seekbarMood;
    private TextView tvMoodValue;
    private TextView btnEnergyLow, btnEnergyMid, btnEnergyHigh;
    private TextView btnLocationIn, btnLocationOut;
    private AppCompatButton btnGetRecommendation;

    // 선택된 값을 저장할 변수 (초기값은 XML에 선택된 상태로 설정)
    private String selectedEnergy = "보통";
    private String selectedLocation = "실내";

    // Firebase 인증 및 Firestore 인스턴스 선언
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeling_input); // 이전에 만든 XML 파일 이름

        // Firebase 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI 요소들을 ID로 찾아와서 변수에 할당
        initViews();

        // 각종 버튼과 SeekBar에 대한 리스너(이벤트 처리) 설정
        setupListeners();
    }

    // 레이아웃의 View들을 findViewById로 찾아와 초기화하는 메소드

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        seekbarMood = findViewById(R.id.seekbar_mood);
        tvMoodValue = findViewById(R.id.tv_mood_value);

        // 에너지 버튼
        btnEnergyLow = findViewById(R.id.btnEnergyLow); // XML에 ID 추가 필요
        btnEnergyMid = findViewById(R.id.btnEnergyMid); // XML에 ID 추가 필요
        btnEnergyHigh = findViewById(R.id.btnEnergyHigh); // XML에 ID 추가 필요

        // 실내/실외 버튼
        btnLocationIn = findViewById(R.id.btnLocationIn); // XML에 ID 추가 필요
        btnLocationOut = findViewById(R.id.btnLocationOut); // XML에 ID 추가 필요

        btnGetRecommendation = findViewById(R.id.btn_get_recommendation);
    }

    private void setupListeners() {
        // 1. 뒤로가기 버튼
        btnBack.setOnClickListener(v -> {
            finish(); // 현재 액티비티 종료
        });

        // 2. 감정 상태 SeekBar
        seekbarMood.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // SeekBar를 움직일 때마다 tvMoodValue 텍스트를 현재 값(progress)으로 변경
                // XML에서 max를 10으로 설정했으므로 0~10까지의 값이 표시됩니다.
                // 만약 1~10으로 표시하고 싶다면 `String.valueOf(progress + 1)`로 수정하고, max를 9로 변경하면 됩니다.
                tvMoodValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 3. 에너지 버튼 그룹
        List<TextView> energyButtons = Arrays.asList(btnEnergyLow, btnEnergyMid, btnEnergyHigh);
        for (TextView button : energyButtons) {
            button.setOnClickListener(v -> {
                selectedEnergy = button.getText().toString();
                updateButtonStyles(energyButtons, button);
            });
        }

        // 4. 실내/실외 버튼 그룹
        List<TextView> locationButtons = Arrays.asList(btnLocationIn, btnLocationOut);
        for (TextView button : locationButtons) {
            button.setOnClickListener(v -> {
                selectedLocation = button.getText().toString();
                updateButtonStyles(locationButtons, button);
            });
        }

        // 5. 추천 받기 버튼
        btnGetRecommendation.setOnClickListener(v -> {
            saveDataToFirestore();
        });
    }

    private void updateButtonStyles(List<TextView> buttonGroup, TextView selectedButton) {
        for (TextView button : buttonGroup) {
            if (button == selectedButton) {
                // 선택된 버튼 스타일 적용
                button.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_choice_button_selected));
                button.setTypeface(null, Typeface.BOLD);
            } else {
                // 선택되지 않은 버튼 스타일 적용
                button.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_choice_button_unselected));
                button.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    private void saveDataToFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            // 필요하다면 로그인 화면으로 이동하는 코드 추가
            // Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);
            return;
        }

        // 현재 사용자의 고유 ID(UID) 가져오기
        String uid = currentUser.getUid();

        // 저장할 데이터 생성 (Map 형태)
        Map<String, Object> feelingData = new HashMap<>();
        feelingData.put("mood", seekbarMood.getProgress());
        feelingData.put("energy", selectedEnergy);
        feelingData.put("location", selectedLocation);
        feelingData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp()); // 저장 시간 기록

        // Firestore에 데이터 저장
        // 'user_feelings' 라는 컬렉션에 현재 사용자의 UID를 문서 이름으로 하여 데이터를 저장합니다.
        db.collection("user_feelings").document(uid).set(feelingData)
                .addOnSuccessListener(aVoid -> {
                    // 저장 성공 시
                    Toast.makeText(FeelingInputActivity.this, "데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show();

                    // 다음 화면(추천 화면)으로 이동
                    // 아직 RecommendationActivity가 없으므로 주석 처리. 만들고 나서 주석을 푸세요.
                    // Intent intent = new Intent(FeelingInputActivity.this, RecommendationActivity.class);
                    // startActivity(intent);
                    // finish(); // 현재 화면은 종료
                })
                .addOnFailureListener(e -> {
                    // 저장 실패 시
                    Toast.makeText(FeelingInputActivity.this, "저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}