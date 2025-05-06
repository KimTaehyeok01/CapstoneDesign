package com.example.capstonedesign.settings_information;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterestChangeActivity extends AppCompatActivity {

    private Button[] seasonButtons;
    private boolean[] seasonSelected;
    private Button[] sportButtons;
    private boolean[] sportSelected;

    private final String[] seasonNames = {"봄", "여름", "가을", "겨울"};
    private final String[] sportNames  = {"육상 스포츠", "해상 스포츠", "항공 스포츠"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_change);

        // 1) 계절 버튼 초기화
        seasonButtons = new Button[]{
                findViewById(R.id.btnSeasonSpring),
                findViewById(R.id.btnSeasonSummer),
                findViewById(R.id.btnSeasonFall),
                findViewById(R.id.btnSeasonWinter)
        };
        seasonSelected = new boolean[seasonButtons.length];
        for (int i = 0; i < seasonButtons.length; i++) {
            final int idx = i;
            seasonButtons[idx].setOnClickListener(v -> {
                seasonSelected[idx] = !seasonSelected[idx];
                v.setAlpha(seasonSelected[idx] ? 0.5f : 1.0f);
            });
        }

        // 2) 스포츠 버튼 초기화
        sportButtons = new Button[]{
                findViewById(R.id.btnSportsLand),
                findViewById(R.id.btnSportsSea),
                findViewById(R.id.btnSportsAir)
        };
        sportSelected = new boolean[sportButtons.length];
        for (int i = 0; i < sportButtons.length; i++) {
            final int idx = i;
            sportButtons[idx].setOnClickListener(v -> {
                sportSelected[idx] = !sportSelected[idx];
                v.setAlpha(sportSelected[idx] ? 0.5f : 1.0f);
            });
        }

        // 3) 확인 버튼 클릭 시 저장 로직
        ImageButton btnConfirm = findViewById(R.id.btnSubmitPassword);
        btnConfirm.setOnClickListener(v -> saveInterestChanges());
    }

    public void onBackClicked(View view) {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void saveInterestChanges() {
        List<String> selectedSeasons = new ArrayList<>();
        for (int i = 0; i < seasonSelected.length; i++) {
            if (seasonSelected[i]) selectedSeasons.add(seasonNames[i]);
        }

        List<String> selectedSports = new ArrayList<>();
        for (int i = 0; i < sportSelected.length; i++) {
            if (sportSelected[i]) selectedSports.add(sportNames[i]);
        }

        if (selectedSeasons.isEmpty() && selectedSports.isEmpty()) {
            Toast.makeText(this, "하나 이상 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> update = new HashMap<>();
        if (!selectedSeasons.isEmpty()) update.put("interestSeasons", selectedSeasons);
        if (!selectedSports.isEmpty())  update.put("interestCategory", selectedSports);

        db.collection("users").document(uid)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "관심사 변경 완료!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
