package com.example.capstonedesign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class FilterActivity extends AppCompatActivity {

    private MaterialButton btnApply;
    private ImageButton btnBack;
    private TextView btnReset;
    private ChipGroup groupSeason;
    private ChipGroup groupType;
    private ChipGroup groupRegion;

    public static final String EXTRA_CATEGORIES = "selectedTypes";   // category
    public static final String EXTRA_SEASONS = "selectedSeasons";    // season
    public static final String EXTRA_TOPICS = "selectedRegions";     // topic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge 적용 (노치, 상태바 대응)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_filter);

        // 상태바, 내비게이션 영역 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.filter_root), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    sys.top + v.getPaddingTop(),
                    v.getPaddingRight(),
                    sys.bottom + v.getPaddingBottom()
            );
            return WindowInsetsCompat.CONSUMED;
        });

        // View 연결
        btnApply = findViewById(R.id.btnApply);
        btnBack = findViewById(R.id.btn_back);
        btnReset = findViewById(R.id.btn_reset);
        groupSeason = findViewById(R.id.group_season);
        groupType = findViewById(R.id.group_type);
        groupRegion = findViewById(R.id.group_region);

        //  뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());

        //  필터 전체 초기화
        btnReset.setOnClickListener(v -> {
            groupSeason.clearCheck();
            groupType.clearCheck();
            groupRegion.clearCheck();
        });

        //  적용 버튼
        btnApply.setOnClickListener(v -> {
            ArrayList<String> seasons = getSelectedChipTexts(groupSeason);
            ArrayList<String> types = getSelectedChipTextsNormalized(groupType); // “수상 → 해상” 변환 포함
            ArrayList<String> regions = getSelectedChipTexts(groupRegion);

            Intent out = new Intent();
            out.putStringArrayListExtra(EXTRA_CATEGORIES, types);
            out.putStringArrayListExtra(EXTRA_SEASONS, seasons);
            out.putStringArrayListExtra(EXTRA_TOPICS, regions);

            setResult(Activity.RESULT_OK, out);
            finish();
        });
    }

    private ArrayList<String> getSelectedChipTexts(ChipGroup chipGroup) {
        ArrayList<String> selectedTexts = new ArrayList<>();
        for (int id : chipGroup.getCheckedChipIds()) {
            Chip chip = chipGroup.findViewById(id);
            if (chip != null) {
                selectedTexts.add(chip.getText().toString());
            }
        }
        return selectedTexts;
    }

    private ArrayList<String> getSelectedChipTextsNormalized(ChipGroup chipGroup) {
        ArrayList<String> selectedTexts = new ArrayList<>();
        for (int id : chipGroup.getCheckedChipIds()) {
            Chip chip = chipGroup.findViewById(id);
            if (chip != null) {
                String label = normalizeLabel(chip.getText().toString().trim());
                selectedTexts.add(label);
            }
        }
        return selectedTexts;
    }

    private String normalizeLabel(String s) {
        if ("수상".equals(s)) return "해상";
        return s;
    }
}
