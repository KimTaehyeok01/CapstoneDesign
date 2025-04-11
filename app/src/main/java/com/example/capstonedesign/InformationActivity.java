package com.example.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class InformationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // activity_information.xml은 귀하가 제공한 계정정보(내정보) 페이지 XML입니다.
        setContentView(R.layout.activity_information);

        // 뒤로가기 버튼(btnInfoBack) 이벤트 처리
        ImageButton btnInfoBack = findViewById(R.id.btnInfoBack);
        btnInfoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                // 전환 애니메이션: 왼쪽에서 들어오고 오른쪽으로 나가기
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // 계정정보 버튼(btn_account_info) 이벤트 처리
        LinearLayout btnAccountInfo = findViewById(R.id.btn_account_info);
        btnAccountInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AccountInfoActivity는 activity_account_info.xml 레이아웃을 사용하도록 구현되어야 합니다.
                Intent intent = new Intent(InformationActivity.this, AccountInfoActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }
}
