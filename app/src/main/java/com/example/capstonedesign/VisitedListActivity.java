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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class VisitedListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VisitedListAdapter adapter;
    private List<VisitedPlace> visitedPlaces;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited_list);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_visited_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        visitedPlaces = new ArrayList<>();
        adapter = new VisitedListAdapter(this, visitedPlaces);
        recyclerView.setAdapter(adapter);

        loadVisitedPlaces();
    }

    private void loadVisitedPlaces() {
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> placeNames = (List<String>) documentSnapshot.get("stampedPlaces");
                if (placeNames == null || placeNames.isEmpty()) {
                    Toast.makeText(this, "방문한 장소가 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                fetchPlaceDetails(placeNames);
            }
        });
    }

    private void fetchPlaceDetails(List<String> placeNames) {
        if (placeNames.isEmpty()) return;
        visitedPlaces.clear();

        db.collection("sports_locations")
                .whereIn("name", placeNames)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String name = doc.getString("name");
                            String address = doc.getString("address");
                            String imageUrl = doc.getString("image");
                            String region = doc.getString("topic");
                            visitedPlaces.add(new VisitedPlace(name, address, imageUrl, region));
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (visitedPlaces.isEmpty()) {
                        Toast.makeText(this, "장소 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "데이터 로딩 실패", Toast.LENGTH_SHORT).show();
                });
    }
}