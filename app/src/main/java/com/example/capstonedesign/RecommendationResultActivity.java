package com.example.capstonedesign;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    private ImageButton btnBack;
    private ImageView ivSelectedMood;
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
        int mood = intent.getIntExtra("mood", 5);
        String energy = intent.getStringExtra("energy");
        String location = intent.getStringExtra("location");

        initViews();
        setupListeners();
        updateUI(mood, energy);

        setupRecyclerView();

        // Firestore에서 추천 장소 데이터 로드
        loadRecommendationsFromFirestore(mood, energy, location);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        ivSelectedMood = findViewById(R.id.iv_selected_mood);
        tvRecommendationReason = findViewById(R.id.tv_recommendation_reason);
        recyclerRecommendations = findViewById(R.id.recycler_recommendations);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateUI(int mood, String energy) {
        if (mood <= 2) {
            ivSelectedMood.setImageResource(R.drawable.ic_face_sad);
        } else if (mood <= 4) {
            ivSelectedMood.setImageResource(R.drawable.ic_face_anxious);
        } else if (mood <= 6) {
            ivSelectedMood.setImageResource(R.drawable.ic_face_neutral);
        } else if (mood <= 8) {
            ivSelectedMood.setImageResource(R.drawable.ic_face_good);
        } else {
            ivSelectedMood.setImageResource(R.drawable.ic_face_happy);
        }

        String moodText = getMoodText(mood);
        String reasonText = String.format(
                "<font color='#FF6347'>%s과 에너지 %s으로</font><br/>보여서 차분한 스포츠를 골라봤어요",
                moodText, energy
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvRecommendationReason.setText(Html.fromHtml(reasonText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvRecommendationReason.setText(Html.fromHtml(reasonText));
        }
    }

    private String getMoodText(int mood) {
        if (mood <= 2) return "나쁨";
        if (mood <= 4) return "불안";
        if (mood <= 6) return "보통";
        if (mood <= 8) return "괜찮음";
        return "좋음";
    }

    private void setupRecyclerView() {
        adapter = new RecommendationAdapter(this, recommendationList);
        recyclerRecommendations.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecommendations.setAdapter(adapter);
    }

    private void loadRecommendationsFromFirestore(int mood, String energy, String location) {
        // "sports_locations" 컬렉션에 대한 기본 쿼리 생성
        Query query = db.collection("sports_locations");

        // 1. 선택한 '실내/실외' 조건으로 필터링
        if (location != null && !location.isEmpty()) {
            query = query.whereEqualTo("location", location);
        }

        // 2. 선택한 '에너지' 조건으로 필터링
        if (energy != null && !energy.isEmpty()) {
            query = query.whereEqualTo("energy", energy);
        }

        // 3. '기분' 점수 범위로 필터링 (예: 3점을 선택하면 2~4점 범위의 장소 추천)
        if (mood <= 4) { // 부정적 감정일 때
            query = query.whereLessThanOrEqualTo("mood", 5); // 차분한 활동(낮은 mood 점수) 추천
        } else { // 긍정적 감정일 때
            query = query.whereGreaterThanOrEqualTo("mood", 6); // 활기찬 활동(높은 mood 점수) 추천
        }

        // 쿼리 실행
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                recommendationList.clear(); // 기존 목록 비우기
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Firestore 문서를 Map 객체로 변환하여 리스트에 추가
                    recommendationList.add(document.getData());
                }
                // 어댑터에 데이터 변경 알림
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