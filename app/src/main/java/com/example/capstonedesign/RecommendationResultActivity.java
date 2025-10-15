package com.example.capstonedesign;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecommendationResultActivity extends AppCompatActivity {

    private static final String TAG = "RecommendResultActivity";

    // UI 요소 선언
    private ImageButton btnBack;
    private TextView tvRecommendationReason;
    private RecyclerView recyclerRecommendations;

    private RecommendationAdapter adapter;
    private final List<Map<String, Object>> recommendationList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation_result);

        db = FirebaseFirestore.getInstance();

        // Intent에서 데이터 가져오기
        Intent intent = getIntent();
        int personCount = intent.getIntExtra("personCount", 1);
        boolean isChildFriendly = intent.getBooleanExtra("isChildFriendly", false);
        ArrayList<Integer> ageValues = intent.getIntegerArrayListExtra("ageValues");
        ArrayList<String> energyLevels = intent.getStringArrayListExtra("energyLevels");
        ArrayList<String> locations = intent.getStringArrayListExtra("locations");

        initViews();
        setupListeners();
        updateUI(personCount, isChildFriendly, ageValues, energyLevels, locations);
        setupRecyclerView();

        // Firestore에서 추천 데이터 로드
        loadRecommendationsFromFirestore(ageValues, energyLevels, locations, isChildFriendly);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvRecommendationReason = findViewById(R.id.tv_recommendation_reason);
        recyclerRecommendations = findViewById(R.id.recycler_recommendations);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    // 상단 추천 조건 텍스트 UI 업데이트
    private void updateUI(int personCount, boolean isChildFriendly, List<Integer> ages, List<String> energies, List<String> locations) {
        List<String> conditions = new ArrayList<>();
        conditions.add("인원 " + personCount + "명");

        if (ages != null && !ages.isEmpty()) {
            // SDK 26 이상
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String ageText = ages.stream()
                        .map(age -> age + "대")
                        .distinct()
                        .collect(Collectors.joining(", "));
                conditions.add(ageText);
            } else { // SDK 26 미만
                List<String> ageStrings = new ArrayList<>();
                for(Integer age : ages) {
                    if (!ageStrings.contains(age + "대")) {
                        ageStrings.add(age + "대");
                    }
                }
                conditions.add(TextUtils.join(", ", ageStrings));
            }
        }

        if (energies != null && !energies.isEmpty()) {
            conditions.add("스릴 " + TextUtils.join(", ", energies));
        }

        if (locations != null && !locations.isEmpty()) {
            conditions.add(TextUtils.join(", ", locations));
        }

        if (isChildFriendly) {
            conditions.add("어린이 동반");
        }

        String conditionsText = TextUtils.join(", ", conditions);
        String reasonText = String.format(
                "<b><font color='#FF6347'>%s</font></b> 조건으로<br/>알맞은 레저스포츠를 추천해 드릴게요!",
                conditionsText
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvRecommendationReason.setText(Html.fromHtml(reasonText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvRecommendationReason.setText(Html.fromHtml(reasonText));
        }
    }

    private void setupRecyclerView() {
        adapter = new RecommendationAdapter(this, recommendationList);
        recyclerRecommendations.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecommendations.setAdapter(adapter);
    }

    // Firestore 쿼리를 생성하고 데이터를 로드
    private void loadRecommendationsFromFirestore(List<Integer> ageValues, List<String> energyLevels, List<String> locations, boolean isChildFriendly) {
        Query query = db.collection("sports_locations");

        // 어린이 동반 필터링
        if (isChildFriendly) {
            query = query.whereEqualTo("childFriendly", true);
        }

        // 실내/실외 필터링 (whereIn)
        if (locations != null && !locations.isEmpty()) {
            query = query.whereIn("location", locations);
        }

        // 스릴 난이도 필터링 (whereIn)
        if (energyLevels != null && !energyLevels.isEmpty()) {
            query = query.whereIn("energy", energyLevels);
        }

        // 나이 필터링 (whereArrayContainsAny)
        if (ageValues != null && !ageValues.isEmpty()) {
            query = query.whereArrayContainsAny("age", ageValues);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                recommendationList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    recommendationList.add(document.getData());
                }
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Successfully loaded " + recommendationList.size() + " places.");

                if (recommendationList.isEmpty()) {
                    Toast.makeText(this, "조건에 맞는 추천 장소가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
                Toast.makeText(this, "추천 목록을 불러오는 데 실패했습니다. Firestore 쿼리 제약을 확인하세요.", Toast.LENGTH_LONG).show();
            }
        });
    }
}