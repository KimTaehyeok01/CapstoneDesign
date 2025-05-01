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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText editSearch;
    private ImageButton btnSearchGlass, btnCancel;
    private TextView tvNearbySearch;

    private RecyclerView recyclerSearchResults;
    private SearchResultAdapter adapter;
    private List<String> searchResults = new ArrayList<>();
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        editSearch = findViewById(R.id.editSearch);
        btnSearchGlass = findViewById(R.id.btnSearchGlass);
        btnCancel = findViewById(R.id.btnCancel);
        tvNearbySearch = findViewById(R.id.tvNearbySearch);

        firestore = FirebaseFirestore.getInstance();

        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        adapter = new SearchResultAdapter(searchResults, item -> {
            Intent intent = new Intent(SearchActivity.this, PlaceDetailActivity.class);
            intent.putExtra("place_name", item);
            startActivity(intent);
        });

        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(adapter);

        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        btnSearchGlass.setOnClickListener(view -> performSearch());
        btnCancel.setOnClickListener(view -> editSearch.setText(""));

        tvNearbySearch.setOnClickListener(view -> {
            Intent intent = new Intent(SearchActivity.this, MapActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void performSearch() {
        String query = editSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference placesRef = firestore.collection("sports_locations");
        placesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchResults.clear();
                    String lowerQuery = query.toLowerCase();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null && name.toLowerCase().contains(lowerQuery)) {
                            searchResults.add(name);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (searchResults.isEmpty()) {
                        Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "검색 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
