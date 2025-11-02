package com.example.capstonedesign.settings_information;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstonedesign.R; // R 클래스 import
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NoticeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoticeAdapter adapter;
    private List<Notice> noticeList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.rv_notices);
        noticeList = new ArrayList<>();
        adapter = new NoticeAdapter(this, noticeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ImageButton btnBack = findViewById(R.id.btn_back_notice);
        btnBack.setOnClickListener(v -> finish());

        loadNotices();
    }

    private void loadNotices() {
        db.collection("notices")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    noticeList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Notice notice = document.toObject(Notice.class);
                        noticeList.add(notice);
                    }
                    adapter.notifyDataSetChanged();

                    if (noticeList.isEmpty()) {
                        Toast.makeText(this, "등록된 공지사항이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "공지사항을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}