package com.example.capstonedesign.settings_information;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.capstonedesign.R;
import com.example.capstonedesign.login_signup.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class InformationActivity extends AppCompatActivity {

    private FrameLayout bottomOverlayContainer;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 뒤로가기 버튼
        ImageButton btnInfoBack = findViewById(R.id.btnInfoBack);
        btnInfoBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 계정 정보 버튼
        LinearLayout btnAccountInfo = findViewById(R.id.btn_account_info);
        btnAccountInfo.setOnClickListener(v -> {
            Intent intent = new Intent(InformationActivity.this, AccountInfoActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 비밀번호 변경 버튼 (여기에 추가)
        LinearLayout btnPasswordChange = findViewById(R.id.btn_password_change);
        btnPasswordChange.setOnClickListener(v -> {
            Intent intent = new Intent(InformationActivity.this, PasswordChangeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 탈퇴하기 버튼
        LinearLayout btnWithdraw = findViewById(R.id.btn_withdraw);
        btnWithdraw.setOnClickListener(v -> showWithdrawLayout());

        bottomOverlayContainer = findViewById(R.id.bottomOverlayContainer);
    }

    private void showWithdrawLayout() {
        if (bottomOverlayContainer.getChildCount() == 0) {
            View withdrawView = LayoutInflater.from(this)
                    .inflate(R.layout.activity_withdraw, bottomOverlayContainer, false);

            withdrawView.findViewById(R.id.btnCancelDelete).setOnClickListener(v -> {
                bottomOverlayContainer.removeAllViews();
                bottomOverlayContainer.setVisibility(View.GONE);
            });

            withdrawView.findViewById(R.id.btnDeleteConfirm).setOnClickListener(v -> {
                performWithdraw();
            });

            bottomOverlayContainer.addView(withdrawView);
        }
        bottomOverlayContainer.setVisibility(View.VISIBLE);
    }

    private void performWithdraw() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        // Firestore 데이터 삭제
        db.collection("users").document(uid).delete()
                .addOnSuccessListener(unused -> {
                    // Auth 계정 삭제
                    user.delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();

                                    // 로그인 화면으로 이동
                                    Intent intent = new Intent(this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(this, "계정 삭제 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "데이터 삭제 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        bottomOverlayContainer.removeAllViews();
        bottomOverlayContainer.setVisibility(View.GONE);
    }
}
