package com.example.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ContactActivity extends AppCompatActivity {

    private EditText etInquiryTitle, etInquiryContent;
    private Button btnSubmitInquiry;
    private TextView btnViewHistory;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        etInquiryTitle = findViewById(R.id.et_inquiry_title);
        etInquiryContent = findViewById(R.id.et_inquiry_content);
        btnSubmitInquiry = findViewById(R.id.btn_submit_inquiry);
        btnViewHistory = findViewById(R.id.btn_view_history);
        btnBack = findViewById(R.id.btn_back_contact);

        btnBack.setOnClickListener(v -> finish());

        btnSubmitInquiry.setOnClickListener(v -> submitInquiry());

        btnViewHistory.setOnClickListener(v -> {
            // TODO: 문의 내역을 보여주는 Activity를 만들고 연결하세요.
            // startActivity(new Intent(this, InquiryHistoryActivity.class));
            Toast.makeText(this, "문의 내역 보기 (구현 예정)", Toast.LENGTH_SHORT).show();
        });
    }

    private void submitInquiry() {
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etInquiryTitle.getText().toString().trim();
        String content = etInquiryContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firestore에 저장할 데이터 생성
        Map<String, Object> inquiry = new HashMap<>();
        inquiry.put("userId", currentUser.getUid());
        inquiry.put("userEmail", currentUser.getEmail());
        inquiry.put("title", title);
        inquiry.put("content", content);
        inquiry.put("timestamp", FieldValue.serverTimestamp());
        inquiry.put("status", "pending"); // 처리 상태 (예: 'pending', 'resolved')

        // 'inquiries' 라는 새로운 컬렉션에 데이터 추가
        db.collection("inquiries")
                .add(inquiry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "문의가 정상적으로 접수되었습니다.", Toast.LENGTH_SHORT).show();
                    finish(); // 제출 후 화면 닫기
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                });
    }
}