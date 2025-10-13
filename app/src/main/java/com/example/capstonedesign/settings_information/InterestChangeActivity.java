package com.example.capstonedesign.settings_information;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterestChangeActivity extends AppCompatActivity {

    private final List<String> selectedSports = new ArrayList<>();
    private final List<String> selectedSeasons = new ArrayList<>();

    private final String[] sportNames = {"육상 스포츠", "해상 스포츠", "항공 스포츠"};
    private final int[] sportCardIds = {R.id.cardSportsLand, R.id.cardSportsSea, R.id.cardSportsAir};
    private final int[] sportBackgroundIds = {R.id.bgSportsLand, R.id.bgSportsSea, R.id.bgSportsAir};
    private final int[] sportTextIds = {R.id.textSportsLand, R.id.textSportsSea, R.id.textSportsAir};

    private final String[] seasonNames = {"봄", "여름", "가을", "겨울"};
    private final int[] seasonCardIds = {R.id.cardSeasonSpring, R.id.cardSeasonSummer, R.id.cardSeasonFall, R.id.cardSeasonWinter};
    private final int[] seasonBackgroundIds = {R.id.bgSeasonSpring, R.id.bgSeasonSummer, R.id.bgSeasonFall, R.id.bgSeasonWinter};
    private final int[] seasonTextIds = {R.id.textSeasonSpring, R.id.textSeasonSummer, R.id.textSeasonFall, R.id.textSeasonWinter};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_change);

        setupChoiceViews();
        loadCurrentUserInterests();

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnSaveChanges = findViewById(R.id.btn_save_changes);
        btnSaveChanges.setOnClickListener(v -> saveInterestChanges());
    }

    private void setupChoiceViews() {
        for (int i = 0; i < sportNames.length; i++) {
            setupCardListener(sportCardIds[i], sportNames[i], sportBackgroundIds[i], sportTextIds[i], selectedSports);
        }
        for (int i = 0; i < seasonNames.length; i++) {
            setupCardListener(seasonCardIds[i], seasonNames[i], seasonBackgroundIds[i], seasonTextIds[i], selectedSeasons);
        }
    }

    private void setupCardListener(int cardId, String text, int backgroundId, int textId, List<String> selectionList) {
        CardView cardView = findViewById(cardId);
        LinearLayout background = findViewById(backgroundId);
        TextView textView = findViewById(textId);

        cardView.setOnClickListener(v -> {
            if (selectionList.contains(text)) {
                selectionList.remove(text);
                background.setBackgroundResource(R.drawable.bg_choice_button_unselected);
                textView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            } else {
                selectionList.add(text);
                background.setBackgroundResource(R.drawable.bg_choice_button_selected);
                textView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            }
        });
    }

    private void loadCurrentUserInterests() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userSports = (List<String>) documentSnapshot.get("interestCategory");
                        if (userSports != null) {
                            selectedSports.addAll(userSports);
                            updateUiSelection(sportCardIds, sportNames, sportBackgroundIds, sportTextIds, selectedSports);
                        }
                        List<String> userSeasons = (List<String>) documentSnapshot.get("interestSeasons");
                        if (userSeasons != null) {
                            selectedSeasons.addAll(userSeasons);
                            updateUiSelection(seasonCardIds, seasonNames, seasonBackgroundIds, seasonTextIds, selectedSeasons);
                        }
                    }
                });
    }

    private void updateUiSelection(int[] cardIds, String[] names, int[] bgIds, int[] textIds, List<String> selectionList) {
        for (int i = 0; i < names.length; i++) {
            if (selectionList.contains(names[i])) {
                findViewById(bgIds[i]).setBackgroundResource(R.drawable.bg_choice_button_selected);
                ((TextView) findViewById(textIds[i])).setTextColor(ContextCompat.getColor(this, android.R.color.white));
            }
        }
    }

    private void saveInterestChanges() {
        if (selectedSeasons.isEmpty() || selectedSports.isEmpty()) {
            Toast.makeText(this, "각 항목을 하나 이상 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("interestSeasons", selectedSeasons);
        updates.put("interestCategory", selectedSports);

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "관심사가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void onBackClicked(View view) {
        finish();
    }
}