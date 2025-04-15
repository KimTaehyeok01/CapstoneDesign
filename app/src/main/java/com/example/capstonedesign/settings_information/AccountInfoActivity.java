package com.example.capstonedesign.settings_information;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;
import com.example.capstonedesign.login_signup.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountInfoActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editName, editAge;
    private FrameLayout btnModifyContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        // 🔹 Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔹 View 연결
        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        btnModifyContainer = findViewById(R.id.btnModifyContainer);
        ImageButton btnAccountBack = findViewById(R.id.btnAccountBack);
        LinearLayout btnLogout = findViewById(R.id.btnLogout);

        // 🔹 공통 버튼 애니메이션
        View.OnTouchListener scaleTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setScaleX(0.96f);
                        v.setScaleY(0.96f);
                        v.setAlpha(0.7f);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                        v.setAlpha(1f);
                        break;
                }
                return false; // false여야 onClick도 작동함
            }
        };

        // 🔹 뒤로가기 버튼
        btnAccountBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 🔹 로그아웃 버튼
        btnLogout.setOnTouchListener(scaleTouchListener);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AccountInfoActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        // 🔹 수정 버튼 애니메이션 + 이벤트
        btnModifyContainer.setOnTouchListener(scaleTouchListener);
        btnModifyContainer.setOnClickListener(v -> {
            String inputName = editName.getText().toString().trim();
            String inputAge = editAge.getText().toString().trim();

            if (inputName.isEmpty() || inputAge.isEmpty()) {
                Toast.makeText(AccountInfoActivity.this, "이름과 나이를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(AccountInfoActivity.this, "로그인 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = mAuth.getCurrentUser().getUid();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String currentName = document.getString("name");
                            String currentAge = document.getString("age");

                            if (inputName.equals(currentName) || inputAge.equals(currentAge)) {
                                Toast.makeText(AccountInfoActivity.this,
                                        "입력한 이름이나 나이가 기존 정보와 동일합니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                db.collection("users").document(uid)
                                        .update("name", inputName, "age", inputAge)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(AccountInfoActivity.this,
                                                    "정보가 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(AccountInfoActivity.this,
                                                    "수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(AccountInfoActivity.this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AccountInfoActivity.this,
                                "정보 조회 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
