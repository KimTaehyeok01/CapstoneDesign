package com.example.capstonedesign.settings_information;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.capstonedesign.R;

public class WithdrawActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // activity_withdraw.xml 파일 사용 (탈퇴 확인 화면 XML)
        setContentView(R.layout.activity_withdraw);

        // 취소 버튼 (btnCancelDelete) 이벤트 처리:
        // 이 버튼을 누르면 탈퇴 화면을 닫음.
        Button btnCancelDelete = findViewById(R.id.btnCancelDelete);
        btnCancelDelete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // 탈퇴하기 버튼 (btnDeleteConfirm) 이벤트 처리:
        // 회원 탈퇴 로직을 구현가능.
        Button btnDeleteConfirm = findViewById(R.id.btnDeleteConfirm);
        btnDeleteConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // 예시로 토스트 메시지 출력 (실제 회원 탈퇴 로직 구현 필요)
                Toast.makeText(WithdrawActivity.this, "탈퇴 기능 실행", Toast.LENGTH_SHORT).show();
                // 탈퇴 후 종료 등 필요한 작업 수행 후 화면 종료
                finish();
            }
        });
    }
}
