package com.example.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {

    private EditText editSearch;
    private ImageButton btnSearchGlass, btnCancel;
    private TextView tvNearbySearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);  // Search 화면 XML

        // 뒤로가기 버튼: 클릭 시 SearchActivity 종료 및 애니메이션 적용
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // 검색 관련 뷰 초기화
        editSearch = findViewById(R.id.editSearch);
        btnSearchGlass = findViewById(R.id.btnSearchGlass);
        btnCancel = findViewById(R.id.btnCancel);

        // 검색어 입력 시 (키보드의 검색 버튼 또는 엔터)
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        // 돋보기 아이콘 클릭 시 검색 기능 호출
        btnSearchGlass.setOnClickListener(view -> performSearch());

        // 취소 버튼 클릭 시 EditText 내용 지우기
        btnCancel.setOnClickListener(view -> editSearch.setText(""));

        // "현재 내 주변에서 검색" 텍스트 클릭 시 MapActivity로 전환
        tvNearbySearch = findViewById(R.id.tvNearbySearch);
        tvNearbySearch.setOnClickListener(view -> {
            Intent intent = new Intent(SearchActivity.this, MapActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    /**
     * 검색어를 가져와서 검색 기능(예: Firebase 연동 등)을 수행합니다.
     * 여기서는 간단히 Toast로 결과를 보여주는 예제입니다.
     */
    private void performSearch() {
        String query = editSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
        } else {
            // 실제 검색 로직(예: Firebase 쿼리 등)을 여기에 추가
            Toast.makeText(this, "검색 중: " + query, Toast.LENGTH_SHORT).show();
        }
    }
}
