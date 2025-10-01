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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeelingInputActivity extends AppCompatActivity {

    // UI 요소 선언
    private ImageButton btnBack;
    private SeekBar seekbarAge;
    private TextView tvAgeValue;
    private TextView btnThrillLow, btnThrillMid, btnThrillHigh;
    private TextView btnLocationIn, btnLocationOut;
    private AppCompatButton btnGetRecommendation;

    // 선택된 값을 저장할 변수
    private String selectedAgeString = "20대"; // UI 표시용 문자열 (초기값)
    private int selectedAgeValue = 20; // 쿼리용 숫자 (초기값)
    private String selectedThrill = "보통";
    private String selectedLocation = "실내";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeling_input);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        seekbarAge = findViewById(R.id.seekbar_age);
        tvAgeValue = findViewById(R.id.tv_age_value);
        btnThrillLow = findViewById(R.id.btnThrillLow);
        btnThrillMid = findViewById(R.id.btnThrillMid);
        btnThrillHigh = findViewById(R.id.btnThrillHigh);
        btnLocationIn = findViewById(R.id.btnLocationIn);
        btnLocationOut = findViewById(R.id.btnLocationOut);
        btnGetRecommendation = findViewById(R.id.btn_get_recommendation);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        seekbarAge.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedAgeString = getAgeString(progress);
                selectedAgeValue = getAgeValue(progress); // 숫자 값도 함께 업데이트
                tvAgeValue.setText(selectedAgeString);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Map<TextView, String> thrillMap = new HashMap<>();
        thrillMap.put(btnThrillLow, "낮음");
        thrillMap.put(btnThrillMid, "보통");
        thrillMap.put(btnThrillHigh, "높음");

        for (Map.Entry<TextView, String> entry : thrillMap.entrySet()) {
            TextView button = entry.getKey();
            String thrillValue = entry.getValue();
            button.setOnClickListener(v -> {
                selectedThrill = thrillValue;
                updateButtonStyles(Arrays.asList(btnThrillLow, btnThrillMid, btnThrillHigh), button);
            });
        }

        List<TextView> locationButtons = Arrays.asList(btnLocationIn, btnLocationOut);
        for (TextView button : locationButtons) {
            button.setOnClickListener(v -> {
                selectedLocation = button.getText().toString();
                updateButtonStyles(locationButtons, button);
            });
        }

        btnGetRecommendation.setOnClickListener(v -> navigateToRecommendation());
    }

    // progress를 나이대 문자열로 변환
    private String getAgeString(int progress) {
        switch (progress) {
            case 0: return "10대";
            case 1: return "20대";
            case 2: return "30대";
            case 3: return "40대";
            case 4: return "50대 이상";
            default: return "20대";
        }
    }

    // progress를 나이대 대표 숫자로 변환
    private int getAgeValue(int progress) {
        switch (progress) {
            case 0: return 10;
            case 1: return 20;
            case 2: return 30;
            case 3: return 40;
            case 4: return 50; // '50대 이상'은 50으로 처리
            default: return 20;
        }
    }

    private void updateButtonStyles(List<TextView> buttonGroup, TextView selectedButton) {
        for (TextView button : buttonGroup) {
            if (button == selectedButton) {
                button.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_choice_button_selected));
                button.setTypeface(null, Typeface.BOLD);
            } else {
                button.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_choice_button_unselected));
                button.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    private void navigateToRecommendation() {
        Intent intent = new Intent(FeelingInputActivity.this, RecommendationResultActivity.class);
        intent.putExtra("ageString", selectedAgeString); // UI 표시용
        intent.putExtra("ageValue", selectedAgeValue);   // 쿼리용
        intent.putExtra("energy", selectedThrill);
        intent.putExtra("location", selectedLocation);
        startActivity(intent);
    }
}