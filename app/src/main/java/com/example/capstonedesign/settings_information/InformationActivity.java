package com.example.capstonedesign.settings_information;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.capstonedesign.R;

public class InformationActivity extends AppCompatActivity {

    private FrameLayout bottomOverlayContainer; // 하단 오버레이 영역

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);  // 위 XML 사용

        // 뒤로가기 버튼 처리
        ImageButton btnInfoBack = findViewById(R.id.btnInfoBack);
        btnInfoBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 계정정보 버튼 처리 (예: 별도 액티비티 전환 등 필요 시 구현)
        LinearLayout btnAccountInfo = findViewById(R.id.btn_account_info);
        btnAccountInfo.setOnClickListener(v -> {
            // 예시: AccountInfoActivity로 전환
            Intent intent = new Intent(InformationActivity.this, AccountInfoActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 탈퇴하기 버튼 처리: 하단 오버레이에 탈퇴 화면 표시
        LinearLayout btnWithdraw = findViewById(R.id.btn_withdraw);
        btnWithdraw.setOnClickListener(v -> {
            showWithdrawLayout();
        });

        // 하단 오버레이 컨테이너 참조 (activity_information.xml에 추가한 FrameLayout)
        bottomOverlayContainer = findViewById(R.id.bottomOverlayContainer);
    }

    private void showWithdrawLayout() {
        if (bottomOverlayContainer.getChildCount() == 0) {
            View withdrawView = LayoutInflater.from(this)
                    .inflate(R.layout.activity_withdraw, bottomOverlayContainer, false);

            // 취소 버튼 처리
            withdrawView.findViewById(R.id.btnCancelDelete).setOnClickListener(v -> {
                bottomOverlayContainer.removeAllViews();
                bottomOverlayContainer.setVisibility(View.GONE);
            });

            // 탈퇴하기 버튼 처리
            withdrawView.findViewById(R.id.btnDeleteConfirm).setOnClickListener(v -> {
                // 실제 탈퇴 로직 구현 (예: Firebase 회원 탈퇴)
                // 필요 시 Toast나 로그 등을 출력
                bottomOverlayContainer.removeAllViews();
                bottomOverlayContainer.setVisibility(View.GONE);
            });

            bottomOverlayContainer.addView(withdrawView);
        }
        // 오버레이 영역 보이도록 설정
        bottomOverlayContainer.setVisibility(View.VISIBLE);
    }
}
