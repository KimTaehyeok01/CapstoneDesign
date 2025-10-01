package com.example.capstonedesign;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
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

public class RecommendationResultActivity extends AppCompatActivity {

    private static final String TAG = "RecommendResultActivity";

    // UI 요소 선언 (ImageView 삭제)
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

        Intent intent = getIntent();
        String ageString = intent.getStringExtra("ageString"); // UI 표시용
        int ageValue = intent.getIntExtra("ageValue", 20); // 쿼리용
        String energy = intent.getStringExtra("energy");
        String location = intent.getStringExtra("location");

        initViews();
        setupListeners();
        updateUI(ageString, energy);

        setupRecyclerView();

        loadRecommendationsFromFirestore(ageValue, energy, location);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        // ivSelectedIcon = findViewById(R.id.iv_selected_mood); <-- 이 줄 삭제
        tvRecommendationReason = findViewById(R.id.tv_recommendation_reason);
        recyclerRecommendations = findViewById(R.id.recycler_recommendations);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateUI(String age, String energy) {
        String reasonText = String.format(
                "<b><font color='#FF6347'>%s, 스릴 %s</font></b> 조건으로<br/>알맞은 레저스포츠를 추천해 드릴게요!",
                age, energy
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

    private void loadRecommendationsFromFirestore(int age, String energy, String location) {
        Query query = db.collection("sports_locations");

        if (location != null && !location.isEmpty()) {
            query = query.whereEqualTo("location", location);
        }

        if (energy != null && !energy.isEmpty()) {
            query = query.whereEqualTo("energy", energy);
        }

        if (age > 0) {
            query = query.whereArrayContains("age", age);
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
                Toast.makeText(this, "추천 목록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}