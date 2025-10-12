package com.example.capstonedesign;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TierAnalysisActivity extends AppCompatActivity {

    private RecyclerView achievementRecyclerView;
    private AchievementAdapter adapter;
    private List<Achievement> achievementList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tier_analysis);

        // Firebase 인스턴스 초기화
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 뒤로가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // RecyclerView 설정
        achievementRecyclerView = findViewById(R.id.achievementRecyclerView);
        achievementRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 리스트 초기화 및 어댑터 연결
        achievementList = new ArrayList<>();
        adapter = new AchievementAdapter(achievementList);
        achievementRecyclerView.setAdapter(adapter);

        // Firestore에서 실제 업적 데이터 불러오기
        loadAchievementsFromFirestore();
    }

    private void loadAchievementsFromFirestore() {
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId).collection("achievements")
                .orderBy("timestamp", Query.Direction.DESCENDING) // 최신순으로 정렬
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // 데이터가 없을 때 처리 (예: 안내 메시지 표시)
                    } else {
                        achievementList.clear(); // 기존 목록 초기화
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String tier = document.getString("tier");
                            String category = document.getString("category");
                            Timestamp timestamp = document.getTimestamp("timestamp");

                            // 날짜 포맷 변경
                            String dateString = "날짜 정보 없음";
                            if (timestamp != null) {
                                Date date = timestamp.toDate();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy. M. d", Locale.KOREA);
                                dateString = sdf.format(date);
                            }

                            // 이미지 리소스 ID 설정
                            int badgeResId = getTierBadgeResource(tier);

                            // 리스트에 추가
                            String title = tier + " 달성!";
                            String description = category + " 스포츠 " + tier + " 달성했어요";
                            achievementList.add(new Achievement(badgeResId, title, description, dateString));
                        }
                        adapter.notifyDataSetChanged(); // 리스트가 변경되었음을 어댑터에 알림
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "업적을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private int getTierBadgeResource(String tier) {
        if (tier == null) return R.drawable.bronze_badge; // 기본값
        switch (tier) {
            case "Master": return R.drawable.master_badge;
            case "Platinum": return R.drawable.platinum_badge;
            case "Gold": return R.drawable.gold_badge;
            case "Silver": return R.drawable.silver_badge;
            default: return R.drawable.bronze_badge;
        }
    }
}