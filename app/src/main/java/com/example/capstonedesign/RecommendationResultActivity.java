package com.example.capstonedesign;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
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

public class RecommendationResultActivity extends AppCompatActivity {

    private static final String TAG = "RecommendResultActivity";

    // UI 요소
    private ImageButton btnBack;
    private TextView tvRecommendationReason;
    private RecyclerView recyclerRecommendations;

    // RecyclerView Adapter
    private RecommendationAdapter adapter;
    private final List<Map<String, Object>> recommendationList = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation_result);

        db = FirebaseFirestore.getInstance();

        // Intent에서 전달받은 추천 조건
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
        loadRecommendationsFromFirestore(ageValues, energyLevels, locations, isChildFriendly);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvRecommendationReason = findViewById(R.id.tv_recommendation_reason);
        // 'tvNoRecommendation' findViewById 호출 삭제
        recyclerRecommendations = findViewById(R.id.recycler_recommendations);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateUI(int personCount, boolean isChildFriendly, List<Integer> ages, List<String> energies, List<String> locations) {
        List<String> conditions = new ArrayList<>();
        conditions.add("인원 " + personCount + "명");

        if (ages != null && !ages.isEmpty()) {
            List<String> ageStrings = new ArrayList<>();
            for (Integer age : ages) {
                String ageStr = age + "대";
                if (!ageStrings.contains(ageStr)) ageStrings.add(ageStr);
            }
            conditions.add(String.join(", ", ageStrings));
        }

        if (energies != null && !energies.isEmpty()) {
            conditions.add("스릴 " + String.join(", ", energies));
        }

        if (locations != null && !locations.isEmpty()) {
            conditions.add(String.join(", ", locations));
        }

        if (isChildFriendly) {
            conditions.add("어린이 동반");
        }

        String conditionsText = String.join(", ", conditions);
        String fullText = conditionsText + " 조건으로 알맞은 레저스포츠를 추천해 드릴게요!";
        SpannableString spannable = new SpannableString(fullText);

        // 강조 스타일 적용
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF6347")),
                0, conditionsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD),
                0, conditionsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvRecommendationReason.setText(spannable);
    }

    private void setupRecyclerView() {
        adapter = new RecommendationAdapter(this, recommendationList);
        recyclerRecommendations.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecommendations.setAdapter(adapter);
    }

    private void loadRecommendationsFromFirestore(List<Integer> ageValues, List<String> energyLevels,
                                                  List<String> locations, boolean isChildFriendly) {
        Query query = db.collection("sports_locations");

        // Firestore 제약으로 whereIn, array-contains-any는 최소화
        if (energyLevels != null && !energyLevels.isEmpty()) {
            query = query.whereIn("energy", energyLevels);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                recommendationList.clear();

                // 앱 내 필터링
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Map<String, Object> data = doc.getData();

                    // 어린이 동반 필터
                    if (isChildFriendly && (!data.containsKey("childFriendly") || !(Boolean) data.get("childFriendly"))) {
                        continue;
                    }

                    // 장소 필터
                    if (locations != null && !locations.isEmpty()) {
                        String placeLocation = (String) data.get("location");
                        if (placeLocation == null || !locations.contains(placeLocation)) continue;
                    }

                    // 나이 필터
                    if (ageValues != null && !ageValues.isEmpty()) {
                        List<Long> placeAgesLong = (List<Long>) data.get("age");
                        if (placeAgesLong == null) continue;

                        boolean ageMatch = false;
                        for (Integer userAge : ageValues) {
                            if (placeAgesLong.contains(userAge.longValue())) {
                                ageMatch = true;
                                break;
                            }
                        }
                        if (!ageMatch) continue;
                    }

                    // 모든 조건 통과 시 리스트 추가
                    recommendationList.add(data);
                }

                // 결과가 없을 때 TextView를 보여주는 대신 Toast 메시지를 띄웁니다.
                if (recommendationList.isEmpty()) {
                    Toast.makeText(this, "조건에 맞는 추천 장소가 없습니다.", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
                Log.d(TAG, "추천 장소 로드 완료: " + recommendationList.size() + "개");

            } else {
                Log.e(TAG, "Firestore 쿼리 실패", task.getException());
                Toast.makeText(this, "추천 목록을 불러오는 데 실패했습니다.", Toast.LENGTH_LONG).show();
            }
        });
    }
}