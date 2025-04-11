package com.example.capstonedesign;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);  // settings.xml을 사용

        // ic_setting_left_arrow를 사용하는 뒤로가기 버튼
        ImageButton btnSettingBack = findViewById(R.id.btnSettingBack);
        btnSettingBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                // 전환 애니메이션: 왼쪽에서 들어오고 오른쪽으로 나가기
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }
}
