package com.example.capstonedesign.settings_information;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;
import com.example.capstonedesign.login_signup.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AccountInfoActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView tvEmail, tvName, tvAge, tvHeight, tvGender;
    private ImageButton btnAccountBack;
    private TextView textLogout;
    private ImageView arrowLogout;
    private LinearLayout containerSeason, containerLeisure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // View 연결
        tvEmail = findViewById(R.id.tvEmail);
        tvName = findViewById(R.id.tvName);
        tvAge = findViewById(R.id.tvAge);
        tvHeight = findViewById(R.id.tvHeight);
        tvGender = findViewById(R.id.tvGender);
        containerSeason = findViewById(R.id.containerSeason);
        containerLeisure = findViewById(R.id.containerLeisure);

        btnAccountBack = findViewById(R.id.btnAccountBack);
        textLogout = findViewById(R.id.textLogout);
        arrowLogout = findViewById(R.id.arrowLogout);

        // 뒤로가기 버튼
        btnAccountBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 로그아웃 처리
        View.OnClickListener logoutClickListener = v -> {
            mAuth.signOut();
            SharedPreferences preferences = getSharedPreferences("autoLogin", MODE_PRIVATE);
            preferences.edit().remove("autoLoginEnabled").apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        };
        textLogout.setOnClickListener(logoutClickListener);
        arrowLogout.setOnClickListener(logoutClickListener);

        // 사용자 정보 불러오기
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "로그인된 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        tvEmail.setText(currentUser.getEmail());

        String uid = currentUser.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvName.setText(doc.getString("name"));
                        tvAge.setText(String.valueOf(doc.get("age")));
                        tvHeight.setText(String.valueOf(doc.get("height")) + "cm");
                        tvGender.setText(doc.getString("gender"));

                        List<String> seasonList = (List<String>) doc.get("interestSeasons");
                        List<String> leisureList = (List<String>) doc.get("interestCategory");

                        containerSeason.removeAllViews();
                        containerLeisure.removeAllViews();

                        if (seasonList != null) {
                            for (String season : seasonList) {
                                addCardTo(containerSeason, season, getSeasonImage(season));
                            }
                        }

                        if (leisureList != null) {
                            for (String leisure : leisureList) {
                                addCardTo(containerLeisure, leisure, getLeisureImage(leisure));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("AccountInfo", "Firestore 오류", e);
                });
    }

    private void addCardTo(LinearLayout container, String label, int imageResId) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 0, 16, 0);
        card.setLayoutParams(params);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(imageResId);

        TextView textView = new TextView(this);
        textView.setText(label);
        textView.setTextSize(14);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        card.addView(imageView);
        card.addView(textView);
        container.addView(card);
    }

    private int getSeasonImage(String season) {
        switch (season.trim()) {
            case "봄": return R.drawable.season1;
            case "여름": return R.drawable.season2;
            case "가을": return R.drawable.season3;
            case "겨울": return R.drawable.season4;
            default: return R.drawable.ic_question;
        }
    }

    private int getLeisureImage(String leisure) {
        switch (leisure.trim()) {
            case "육상 스포츠": return R.drawable.group1;
            case "해상 스포츠": return R.drawable.group2;
            case "항공 스포츠": return R.drawable.group3;
            default: return R.drawable.ic_question;
        }
    }
}
