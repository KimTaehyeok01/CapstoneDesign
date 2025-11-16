package com.example.capstonedesign.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button; // 수정: ImageButton -> Button
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

public class AgeInputActivity extends AppCompatActivity {

    private EditText editAge;
    private ImageButton backButton;
    private Button nextButton; // 수정: ImageButton -> Button

    private String userName;
    private String selectedGender;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_age);

        // 이전 화면에서 전달된 값 가져오기
        userName = getIntent().getStringExtra("userName");
        selectedGender = getIntent().getStringExtra("selectedGender");
        height = getIntent().getIntExtra("height", -1);

        // 뷰 초기화
        editAge = findViewById(R.id.edit_age);
        backButton = findViewById(R.id.back_button);
        nextButton = findViewById(R.id.next_button);

        // 뒤로가기 버튼 클릭 시 키 입력 화면으로 이동
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, InputHeightActivity.class);
            intent.putExtra("userName", userName);
            intent.putExtra("selectedGender", selectedGender);
            intent.putExtra("height", height);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        });

        // 다음 버튼 클릭 시
        nextButton.setOnClickListener(v -> {
            String ageStr = editAge.getText().toString().trim();

            if (ageStr.isEmpty()) {
                Toast.makeText(this, "나이를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int age = Integer.parseInt(ageStr);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uid = user.getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // 나이 데이터 생성
                Map<String, Object> update = new HashMap<>();
                update.put("age", age);

                // Firestore에 나이 정보 저장
                db.collection("users").document(uid)
                        .set(update, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "나이 저장 완료!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, SportsSelectActivity.class);
                            intent.putExtra("userName", userName);
                            intent.putExtra("selectedGender", selectedGender);
                            intent.putExtra("height", height);
                            intent.putExtra("userAge", age);
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