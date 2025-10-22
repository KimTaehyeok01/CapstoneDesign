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
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FeelingInputActivity extends AppCompatActivity {

    private ImageButton btnBack, btnMinus, btnPlus;
    private TextView tvPersonCount;
    private LinearLayout llAgeBarsContainer;

    private TextView btnThrillLow, btnThrillMid, btnThrillHigh;
    private TextView btnLocationIn, btnLocationOut;
    private TextView btnChild;
    private MaterialButton btnGetRecommendation;

    private int personCount = 0;
    private boolean isChildFriendly = false;

    private final List<String> selectedThrills = new ArrayList<>();
    private final List<String> selectedLocations = new ArrayList<>();
    private final List<View> ageBarViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeling_input);

        initViews();
        setupListeners();

        // 초기 상태 설정
        changePersonCount(1);

        updateButtonStyle(btnThrillMid, true);
        selectedThrills.add("보통");

        updateButtonStyle(btnLocationIn, true);
        selectedLocations.add("실내");
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
        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());

        // 인원 수 조절
        btnPlus.setOnClickListener(v -> changePersonCount(1));
        btnMinus.setOnClickListener(v -> changePersonCount(-1));

        // 스릴 난이도 다중 선택
        btnThrillLow.setOnClickListener(v -> toggleSelection((TextView) v, selectedThrills, "낮음"));
        btnThrillMid.setOnClickListener(v -> toggleSelection((TextView) v, selectedThrills, "보통"));
        btnThrillHigh.setOnClickListener(v -> toggleSelection((TextView) v, selectedThrills, "높음"));

        // 장소 다중 선택
        btnLocationIn.setOnClickListener(v -> toggleSelection((TextView) v, selectedLocations, "실내"));
        btnLocationOut.setOnClickListener(v -> toggleSelection((TextView) v, selectedLocations, "실외"));

        // 어린이 동반 단일 토글
        btnChild.setOnClickListener(v -> {
            isChildFriendly = !isChildFriendly;
            updateButtonStyle(btnChild, isChildFriendly);
        });

        // 추천 결과 화면 이동
        btnGetRecommendation.setOnClickListener(v -> navigateToRecommendation());
    }

    private void changePersonCount(int delta) {
        int newCount = personCount + delta;
        if (newCount < 1) {
            Toast.makeText(this, "최소 1명 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newCount > 10) {
            Toast.makeText(this, "최대 10명까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        personCount = newCount;
        tvPersonCount.setText(String.valueOf(personCount));

        if (delta > 0) {
            addAgeSeekBar(personCount);
        } else {
            removeLastAgeBar();
        }
    }

    private void addAgeSeekBar(int personIndex) {
        View ageView = LayoutInflater.from(this)
                .inflate(R.layout.age_bar_item, llAgeBarsContainer, false);

        TextView tvLabel = ageView.findViewById(R.id.tv_person_label);
        TextView tvAge = ageView.findViewById(R.id.tv_age_value_item);
        SeekBar seekBar = ageView.findViewById(R.id.seekbar_age_item);

        if (personIndex == 1) {
            tvLabel.setText("본인");
        } else {
            tvLabel.setText("인원 " + personIndex);
        }

        tvAge.setText("20대");
        seekBar.setProgress(1);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                tvAge.setText(getAgeString(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        llAgeBarsContainer.addView(ageView);
        ageBarViews.add(ageView);
    }

    private void removeLastAgeBar() {
        if (!ageBarViews.isEmpty()) {
            View last = ageBarViews.remove(ageBarViews.size() - 1);
            llAgeBarsContainer.removeView(last);
        }
    }

    private void toggleSelection(TextView button, List<String> list, String value) {
        boolean selected = list.contains(value);
        if (selected) {
            if (list.size() == 1) {
                Toast.makeText(this, "최소 1개는 선택해야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            list.remove(value);
        } else {
            list.add(value);
        }
        updateButtonStyle(button, !selected);
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

        Intent intent = new Intent(this, RecommendationResultActivity.class);
        intent.putExtra("personCount", personCount);
        intent.putExtra("isChildFriendly", isChildFriendly);
        intent.putIntegerArrayListExtra("ageValues", getAgesFromSeekBars());
        intent.putStringArrayListExtra("energyLevels", new ArrayList<>(selectedThrills)); // 스릴을 energyLevels로 전달
        intent.putStringArrayListExtra("locations", new ArrayList<>(selectedLocations));
        startActivity(intent);
    }

    private ArrayList<Integer> getAgesFromSeekBars() {
        ArrayList<Integer> ages = new ArrayList<>();
        for (View view : ageBarViews) {
            SeekBar sb = view.findViewById(R.id.seekbar_age_item);
            ages.add(getAgeValue(sb.getProgress()));
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