package com.example.capstonedesign;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TierAnalysisActivity extends AppCompatActivity {

    private RecyclerView achievementRecyclerView;
    private AchievementAdapter adapter;
    private List<Achievement> achievementList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tier_analysis);

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // RecyclerView 설정
        achievementRecyclerView = findViewById(R.id.achievementRecyclerView);
        achievementRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 샘플 데이터 생성 (실제로는 Firebase 등에서 데이터를 가져와야 합니다)
        loadSampleData();

        // 어댑터 생성 및 연결
        adapter = new AchievementAdapter(achievementList);
        achievementRecyclerView.setAdapter(adapter);
    }

    private void loadSampleData() {
        achievementList = new ArrayList<>();
        achievementList.add(new Achievement(R.drawable.silver_badge, "실버 달성!", "육상 스포츠 실버 달성했어요", "2025. 2. 1"));
        achievementList.add(new Achievement(R.drawable.silver_badge, "실버 달성!", "겨울 스포츠 실버 달성했어요", "2025. 2. 1"));
        // ... 필요한 만큼 데이터를 추가 ...
    }
}