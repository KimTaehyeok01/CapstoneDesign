package com.example.capstonedesign;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class AccountInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // account 정보 화면용 XML 파일 (activity_account_info.xml)
        setContentView(R.layout.activity_account_info);

        // 뒤로가기 버튼(btnAccountBack) 참조 및 클릭 이벤트 등록
        ImageButton btnAccountBack = findViewById(R.id.btnAccountBack);
        btnAccountBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                // 전환 애니메이션: 왼쪽에서 들어오고 오른쪽으로 나가기
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }
}
