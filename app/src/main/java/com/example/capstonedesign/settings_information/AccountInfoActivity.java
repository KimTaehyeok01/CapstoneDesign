package com.example.capstonedesign.settings_information;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.capstonedesign.R;
import com.example.capstonedesign.login_signup.LoginActivity;

public class AccountInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // activity_account_info.xml 레이아웃을 사용합니다.
        setContentView(R.layout.activity_account_info);

        // 뒤로가기 버튼(btnAccountBack) 이벤트 처리
        ImageButton btnAccountBack = findViewById(R.id.btnAccountBack);
        btnAccountBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                // 애니메이션: 왼쪽에서 들어오고 오른쪽으로 나가기
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // 로그아웃 버튼(btnLogout) 이벤트 처리
        LinearLayout btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 실제 로그아웃 로직 (예: FirebaseAuth.getInstance().signOut();) 추가 가능
                // 로그인 화면(LoginActivity)으로 전환
                Intent intent = new Intent(AccountInfoActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                // 현재 액티비티 종료
                finish();
            }
        });
    }
}
