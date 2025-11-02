package com.example.capstonedesign;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class InquiryHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InquiryAdapter adapter;
    private List<Inquiry> inquiryList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquiry_history);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = findViewById(R.id.rv_inquiry_history);
        inquiryList = new ArrayList<>();
        adapter = new InquiryAdapter(this, inquiryList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ImageButton btnBack = findViewById(R.id.btn_back_history);
        btnBack.setOnClickListener(v -> finish());

        loadInquiryHistory();
    }

    private void loadInquiryHistory() {
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("inquiries")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    inquiryList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Inquiry inquiry = document.toObject(Inquiry.class);
                        inquiryList.add(inquiry);
                    }
                    adapter.notifyDataSetChanged();

                    if (inquiryList.isEmpty()) {
                        Toast.makeText(this, "문의 내역이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "문의 내역을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}