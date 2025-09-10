package com.example.capstonedesign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class FilterActivity extends AppCompatActivity {

    private MaterialButton btnApply;

    public static final String EXTRA_CATEGORIES = "selectedTypes";   // category
    public static final String EXTRA_SEASONS    = "selectedSeasons"; // season
    public static final String EXTRA_TOPICS     = "selectedRegions"; // topic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        btnApply = findViewById(R.id.btnApply);

        // 버튼들을 checkable 로 보장 (XML에 app:checkable="true"가 있으면 이 부분은 생략 가능)
        ensureCheckable(
                R.id.btn_spring, R.id.btn_summer, R.id.btn_autumn, R.id.btn_winter,
                R.id.btn_land, R.id.btn_water, R.id.btn_air,
                R.id.btn_gyeonggi, R.id.btn_chungcheong, R.id.btn_jeolla, R.id.btn_gyeongsang,
                R.id.btn_gangwon, R.id.btn_hwanghae, R.id.btn_pyeongan, R.id.btn_hamgyeong
        );

        btnApply.setOnClickListener(v -> {
            ArrayList<String> categories = new ArrayList<>();
            ArrayList<String> seasons    = new ArrayList<>();
            ArrayList<String> topics     = new ArrayList<>();

            // 계절
            collectIfChecked(seasons, R.id.btn_spring);
            collectIfChecked(seasons, R.id.btn_summer);
            collectIfChecked(seasons, R.id.btn_autumn);
            collectIfChecked(seasons, R.id.btn_winter);

            // 카테고리(육상/수상/항공) → DB 규격으로 정규화
            collectIfCheckedNormalized(categories, R.id.btn_land);
            collectIfCheckedNormalized(categories, R.id.btn_water);
            collectIfCheckedNormalized(categories, R.id.btn_air);

            // 지역
            collectIfChecked(topics, R.id.btn_gyeonggi);
            collectIfChecked(topics, R.id.btn_chungcheong);
            collectIfChecked(topics, R.id.btn_jeolla);
            collectIfChecked(topics, R.id.btn_gyeongsang);
            collectIfChecked(topics, R.id.btn_gangwon);
            collectIfChecked(topics, R.id.btn_hwanghae);
            collectIfChecked(topics, R.id.btn_pyeongan);
            collectIfChecked(topics, R.id.btn_hamgyeong);

            Intent out = new Intent();
            out.putStringArrayListExtra(EXTRA_CATEGORIES, categories);
            out.putStringArrayListExtra(EXTRA_SEASONS, seasons);
            out.putStringArrayListExtra(EXTRA_TOPICS, topics);

            setResult(Activity.RESULT_OK, out);
            finish();
        });
    }

    // ---- helpers ----
    private void ensureCheckable(int... ids) {
        for (int id : ids) {
            MaterialButton b = findViewById(id);
            if (b != null) {
                b.setCheckable(true);
            }
        }
    }

    private void collectIfChecked(ArrayList<String> list, int buttonId) {
        MaterialButton btn = findViewById(buttonId);
        if (btn != null && btn.isChecked()) {
            list.add(btn.getText().toString().trim());
        }
    }

    // “수상” → “해상” 등 DB 규격으로 맞춰서 넣기
    private void collectIfCheckedNormalized(ArrayList<String> list, int buttonId) {
        MaterialButton btn = findViewById(buttonId);
        if (btn != null && btn.isChecked()) {
            list.add(normalizeLabel(btn.getText().toString().trim()));
        }
    }

    private String normalizeLabel(String s) {
        if ("수상".equals(s)) return "해상";
        return s;
    }
}
