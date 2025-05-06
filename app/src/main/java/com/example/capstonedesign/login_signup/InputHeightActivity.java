package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonedesign.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class InputHeightActivity extends AppCompatActivity {

    private EditText editHeight;
    private ImageButton backButton, nextButton;

    private String userName;
    private String selectedGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_height);

        // 이전 화면에서 전달된 값 가져오기
        userName = getIntent().getStringExtra("userName");
        selectedGender = getIntent().getStringExtra("selectedGender");

        // 뷰 초기화
        editHeight = findViewById(R.id.edit_age);
        backButton = findViewById(R.id.back_button);
        nextButton = findViewById(R.id.next_button);

        // 뒤로가기 버튼 클릭 시 성별 선택 화면으로 이동
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GenderSelectActivity.class);
            intent.putExtra("userName", userName);
            startActivity(intent);
            finish();
        });

        // 다음 버튼 클릭 시
        nextButton.setOnClickListener(v -> {
            String heightInput = editHeight.getText().toString().trim();

            if (heightInput.isEmpty()) {
                Toast.makeText(this, "신장을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int height = Integer.parseInt(heightInput);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uid = user.getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // 신장 데이터 생성
                Map<String, Object> update = new HashMap<>();
                update.put("height", height);

                // Firestore에 신장 정보 저장
                db.collection("users").document(uid)
                        .set(update, SetOptions.merge())
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "신장 저장 완료!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, AgeInputActivity.class);
                            intent.putExtra("userName", userName);
                            intent.putExtra("selectedGender", selectedGender);
                            intent.putExtra("height", height);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "숫자로 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
