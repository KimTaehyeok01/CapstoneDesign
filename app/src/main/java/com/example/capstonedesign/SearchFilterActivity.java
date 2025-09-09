package com.example.capstonedesign;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SearchFilterActivity extends AppCompatActivity {

    private static final String TAG = "SearchFilter";

    // 상단
    private ImageButton btnBack;
    private TextView btnReset;
    private MaterialButton btnApply;

    // 그룹
    private ChipGroup groupSeason, groupType, groupRegion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter); // ✅ XML 이름 맞춤

        // findViewById
        btnBack  = findViewById(R.id.btn_back);
        btnReset = findViewById(R.id.btn_reset);
        btnApply = findViewById(R.id.btnApply);

        groupSeason = findViewById(R.id.group_season);
        groupType   = findViewById(R.id.group_type);
        groupRegion = findViewById(R.id.group_region);

        // 이전 선택 복원
        applySelection(groupSeason, FilterPrefs.loadSeasons(this));
        applySelection(groupType,   FilterPrefs.loadTypes(this));
        applySelection(groupRegion, FilterPrefs.loadRegions(this));

        // 상단 버튼
        btnBack.setOnClickListener(v -> finish());

        btnReset.setOnClickListener(v -> {
            clearGroup(groupSeason);
            clearGroup(groupType);
            clearGroup(groupRegion);
            FilterPrefs.clear(this);
            Log.d(TAG, "[Reset] clear all");
        });

        // 적용 버튼
        btnApply.setOnClickListener(v -> {
            ArrayList<String> selSeasons = getCheckedLabels(groupSeason, true);
            ArrayList<String> selTypes   = getCheckedLabels(groupType, true);
            ArrayList<String> selRegions = getCheckedLabels(groupRegion, false);

            Log.d(TAG, "Apply → types=" + selTypes + ", seasons=" + selSeasons + ", regions=" + selRegions);

            Intent data = new Intent();
            data.putStringArrayListExtra("selectedTypes",   selTypes);
            data.putStringArrayListExtra("selectedSeasons", selSeasons);
            data.putStringArrayListExtra("selectedRegions", selRegions);
            setResult(Activity.RESULT_OK, data);
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 떠날 때 저장
        FilterPrefs.save(this,
                getCheckedLabels(groupSeason, true),
                getCheckedLabels(groupType, true),
                new ArrayList<>(), // 난이도는 현재 미사용
                getCheckedLabels(groupRegion, false));
    }

    /* ---------------- Helpers ---------------- */

    private void applySelection(ChipGroup group, ArrayList<String> selectedLabels) {
        if (group == null || selectedLabels == null) return;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof Chip) {
                Chip chip = (Chip) group.getChildAt(i);
                String label = normalizeLabel(chip.getText().toString().trim());
                chip.setChecked(containsIgnoreCase(selectedLabels, label));
            }
        }
    }

    private ArrayList<String> getCheckedLabels(ChipGroup group, boolean normalizeForDb) {
        ArrayList<String> res = new ArrayList<>();
        if (group == null) return res;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof Chip) {
                Chip chip = (Chip) group.getChildAt(i);
                if (chip.isChecked()) {
                    String t = chip.getText().toString().trim();
                    res.add(normalizeForDb ? normalizeLabel(t) : t);
                }
            }
        }
        return res;
    }

    private void clearGroup(ChipGroup group) {
        if (group == null) return;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof Chip) {
                ((Chip) group.getChildAt(i)).setChecked(false);
            }
        }
    }

    private String normalizeLabel(String s) {
        // XML이 “수상”일 때 DB는 “해상”으로 통일
        if ("수상".equals(s)) return "해상";
        return s;
    }

    private boolean containsIgnoreCase(ArrayList<String> list, String value) {
        if (value == null || list == null) return false;
        String target = value.trim();
        for (String s : list) {
            if (s != null && s.trim().equalsIgnoreCase(target)) return true;
        }
        return false;
    }

    /* ---------------- SharedPreferences ---------------- */

    public static class FilterPrefs {
        private static final String PREFS = "filters_prefs";
        private static final String KEY_SEASONS = "selected_seasons";
        private static final String KEY_TYPES   = "selected_types";
        private static final String KEY_DIFFS   = "selected_diffs";
        private static final String KEY_REGIONS = "selected_regions";

        private static void putList(SharedPreferences.Editor editor, String key, ArrayList<String> list) {
            JSONArray arr = new JSONArray();
            if (list != null) for (String s : list) arr.put(s);
            editor.putString(key, arr.toString());
        }

        private static ArrayList<String> getList(SharedPreferences prefs, String key) {
            String json = prefs.getString(key, "[]");
            ArrayList<String> result = new ArrayList<>();
            try {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) result.add(arr.optString(i));
            } catch (JSONException ignored) {}
            return result;
        }

        public static void save(Context ctx,
                                ArrayList<String> seasons,
                                ArrayList<String> types,
                                ArrayList<String> diffs,
                                ArrayList<String> regions) {
            SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = prefs.edit();
            putList(ed, KEY_SEASONS, seasons);
            putList(ed, KEY_TYPES,   types);
            putList(ed, KEY_DIFFS,   diffs);
            putList(ed, KEY_REGIONS, regions);
            ed.apply();
        }

        public static ArrayList<String> loadSeasons(Context ctx) { return getList(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE), KEY_SEASONS); }
        public static ArrayList<String> loadTypes(Context ctx)   { return getList(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE), KEY_TYPES); }
        public static ArrayList<String> loadDiffs(Context ctx)   { return getList(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE), KEY_DIFFS); }
        public static ArrayList<String> loadRegions(Context ctx) { return getList(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE), KEY_REGIONS); }

        public static void clear(Context ctx) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
        }
    }
}
