package com.example.capstonedesign;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeelingInputActivity extends AppCompatActivity {

    // UI 요소 선언
    private ImageButton btnBack;
    private TextView tvPersonCount;
    private ImageButton btnMinus, btnPlus;
    private LinearLayout llAgeBarsContainer;

    private TextView btnThrillLow, btnThrillMid, btnThrillHigh;
    private TextView btnLocationIn, btnLocationOut;
    private TextView btnChild;
    private AppCompatButton btnGetRecommendation;

    // 데이터 저장 변수
    private int personCount = 1;
    private boolean isChildFriendly = false;

    // XML의 기본 선택 상태('중급', '실내')를 코드에 반영하여 초기화
    private final List<String> selectedThrills = new ArrayList<>(Arrays.asList("보통"));
    private final List<String> selectedLocations = new ArrayList<>(Arrays.asList("실내"));

    private final List<View> ageBarViews = new ArrayList<>(); // 동적으로 추가된 나이 SeekBar 뷰를 관리

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeling_input);

        initViews();
        setupListeners();
        updatePersonCountUI(); // 초기 인원(1명)에 대한 UI 설정
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvPersonCount = findViewById(R.id.tv_person_count);
        btnMinus = findViewById(R.id.btn_minus);
        btnPlus = findViewById(R.id.btn_plus);
        llAgeBarsContainer = findViewById(R.id.ll_age_bars_container);

        btnThrillLow = findViewById(R.id.btnThrillLow);
        btnThrillMid = findViewById(R.id.btnThrillMid);
        btnThrillHigh = findViewById(R.id.btnThrillHigh);
        btnLocationIn = findViewById(R.id.btnLocationIn);
        btnLocationOut = findViewById(R.id.btnLocationOut);
        btnChild = findViewById(R.id.btnChild);
        btnGetRecommendation = findViewById(R.id.btn_get_recommendation);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // 인원수 조절 버튼
        btnPlus.setOnClickListener(v -> {
            if (personCount < 10) { // 최대 10명까지
                personCount++;
                updatePersonCountUI();
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (personCount > 1) { // 최소 1명
                personCount--;
                updatePersonCountUI();
            }
        });

        // 스릴 난이도 버튼 (다중 선택)
        btnThrillLow.setOnClickListener(v -> toggleButtonSelection((TextView) v, selectedThrills, "낮음"));
        btnThrillMid.setOnClickListener(v -> toggleButtonSelection((TextView) v, selectedThrills, "보통"));
        btnThrillHigh.setOnClickListener(v -> toggleButtonSelection((TextView) v, selectedThrills, "높음"));

        // 실내/실외 버튼 (다중 선택)
        btnLocationIn.setOnClickListener(v -> toggleButtonSelection((TextView) v, selectedLocations, "실내"));
        btnLocationOut.setOnClickListener(v -> toggleButtonSelection((TextView) v, selectedLocations, "실외"));

        // 어린이 동반 버튼 (단일 토글)
        btnChild.setOnClickListener(v -> {
            isChildFriendly = !isChildFriendly;
            updateSingleToggleStyle((TextView) v, isChildFriendly);
        });

        btnGetRecommendation.setOnClickListener(v -> navigateToRecommendation());
    }

    // 인원 수 변경에 따라 UI (나이 SeekBar)
    private void updatePersonCountUI() {
        tvPersonCount.setText(String.valueOf(personCount));

        int currentSeekBars = llAgeBarsContainer.getChildCount();
        if (personCount > currentSeekBars) {
            for (int i = currentSeekBars; i < personCount; i++) {
                addAgeSeekBar(i + 1);
            }
        } else if (personCount < currentSeekBars) {
            for (int i = currentSeekBars; i > personCount; i--) {
                removeAgeSeekBar();
            }
        }
    }

    private void addAgeSeekBar(int personIndex) {
        LayoutInflater inflater = LayoutInflater.from(this);

        View ageSeekBarView = inflater.inflate(R.layout.age_bar_item, llAgeBarsContainer, false);

        TextView tvPersonLabel = ageSeekBarView.findViewById(R.id.tv_person_label);
        TextView tvAgeValueItem = ageSeekBarView.findViewById(R.id.tv_age_value_item);
        SeekBar seekBarItem = ageSeekBarView.findViewById(R.id.seekbar_age_item);

        tvPersonLabel.setText("인원 " + personIndex);
        tvAgeValueItem.setText("20대");
        seekBarItem.setProgress(1);

        seekBarItem.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvAgeValueItem.setText(getAgeString(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        llAgeBarsContainer.addView(ageSeekBarView);
        ageBarViews.add(ageSeekBarView);
    }

    private void removeAgeSeekBar() {
        if (!ageBarViews.isEmpty()) {
            View lastView = ageBarViews.remove(ageBarViews.size() - 1);
            llAgeBarsContainer.removeView(lastView);
        }
    }

    private void toggleButtonSelection(TextView button, List<String> selectionList, String value) {
        if (selectionList.contains(value)) {
            selectionList.remove(value);
            updateButtonStyle(button, false);
        } else {
            selectionList.add(value);
            updateButtonStyle(button, true);
        }
    }

    private void updateSingleToggleStyle(TextView button, boolean isSelected) {
        updateButtonStyle(button, isSelected);
    }

    private void updateButtonStyle(TextView button, boolean isSelected) {
        if (isSelected) {
            button.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_choice_button_selected));
            button.setTypeface(null, Typeface.BOLD);
        } else {
            button.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_choice_button_unselected));
            button.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void navigateToRecommendation() {
        if (selectedThrills.isEmpty() || selectedLocations.isEmpty()) {
            Toast.makeText(this, "스릴 난이도와 실내/실외를 각각 하나 이상 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(FeelingInputActivity.this, RecommendationResultActivity.class);
        intent.putExtra("personCount", personCount);
        intent.putExtra("isChildFriendly", isChildFriendly);
        intent.putIntegerArrayListExtra("ageValues", getAgesFromSeekBars());
        intent.putStringArrayListExtra("energyLevels", new ArrayList<>(selectedThrills));
        intent.putStringArrayListExtra("locations", new ArrayList<>(selectedLocations));
        startActivity(intent);
    }

    private ArrayList<Integer> getAgesFromSeekBars() {
        ArrayList<Integer> ages = new ArrayList<>();
        for (View view : ageBarViews) {
            SeekBar seekBar = view.findViewById(R.id.seekbar_age_item);
            ages.add(getAgeValue(seekBar.getProgress()));
        }
        return ages;
    }

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

    private int getAgeValue(int progress) {
        switch (progress) {
            case 0: return 10;
            case 1: return 20;
            case 2: return 30;
            case 3: return 40;
            case 4: return 50;
            default: return 20;
        }
    }
}