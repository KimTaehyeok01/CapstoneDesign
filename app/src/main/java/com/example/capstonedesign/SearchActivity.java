package com.example.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    // FilterActivity/SearchFilterActivity에서 넘겨주는 키
    public static final String EXTRA_CATEGORIES = "selectedTypes";     // category
    public static final String EXTRA_SEASONS    = "selectedSeasons";   // season
    public static final String EXTRA_TOPICS     = "selectedRegions";   // topic

    private EditText editSearch;
    private ImageButton btnSearchGlass, btnCancel, btnFilter;
    private TextView tvNearbySearch;

    private RecyclerView recyclerSearchResults;
    private SearchResultAdapter adapter;
    private final List<String> searchResults = new ArrayList<>();
    private FirebaseFirestore firestore;

    // 현재 적용된 필터 상태
    private ArrayList<String> curCategories = new ArrayList<>(); // category
    private ArrayList<String> curSeasons    = new ArrayList<>(); // season
    private ArrayList<String> curTopics     = new ArrayList<>(); // topic

    // 필터 화면 띄우고 결과 받기
    private final ActivityResultLauncher<Intent> openFilter =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Log.d(TAG, "openFilter callback. resultCode=" + result.getResultCode() +
                        ", data=" + (result.getData() == null ? "null" : "non-null"));
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    curCategories = data.getStringArrayListExtra(EXTRA_CATEGORIES);
                    curSeasons    = data.getStringArrayListExtra(EXTRA_SEASONS);
                    curTopics     = data.getStringArrayListExtra(EXTRA_TOPICS);

                    if (curCategories == null) curCategories = new ArrayList<>();
                    if (curSeasons == null)    curSeasons    = new ArrayList<>();
                    if (curTopics == null)     curTopics     = new ArrayList<>();

                    Log.d(TAG, "Received filters → categories=" + curCategories +
                            ", seasons=" + curSeasons + ", topics=" + curTopics);

                    // 받은 필터로 바로 검색 실행
                    performSearch();
                } else {
                    Log.w(TAG, "openFilter: RESULT not OK or data is null");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.d(TAG, "onCreate");

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        editSearch      = findViewById(R.id.editSearch);
        btnSearchGlass  = findViewById(R.id.btnSearchGlass);
        btnCancel       = findViewById(R.id.btnCancel);
        tvNearbySearch  = findViewById(R.id.tvNearbySearch);
        btnFilter       = findViewById(R.id.btnFilter);

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

        // 필터 버튼 → 필터 화면 열고 결과는 콜백으로 받음
        btnFilter.setOnClickListener(view -> {
            Log.d(TAG, "Opening filter screen…");
            Intent intent = new Intent(SearchActivity.this, SearchFilterActivity.class);
            openFilter.launch(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    /**
     * Firestore 전체 조회 후
     * 1) 이름 키워드
     * 2) category / season / topic 필터 적용
     */
    private void performSearch() {
        String keyword = editSearch.getText().toString().trim().toLowerCase();
        Log.d(TAG, "performSearch() keyword='" + keyword + "', filters → categories=" + curCategories +
                ", seasons=" + curSeasons + ", topics=" + curTopics);

        CollectionReference placesRef = firestore.collection("sports_locations");

        placesRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchResults.clear();
                    int total = queryDocumentSnapshots.size();
                    int passed = 0;
                    Log.d(TAG, "Firestore fetched documents: " + total);

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String c = doc.getString("category");
                        String s = doc.getString("season");
                        String t = doc.getString("topic");
                        if (name == null) name = "(no-name)";

                        // 1) 이름 키워드
                        if (!keyword.isEmpty() && (name == null || !name.toLowerCase().contains(keyword))) {
                            Log.v(TAG, "SKIP(name) doc=" + name + " because keyword not in name");
                            continue;
                        }

                        // 2) category / season / topic 필터
                        if (!matchOneOfWithLog(doc, "category", curCategories, name)) continue;
                        if (!matchOneOfWithLog(doc, "season",   curSeasons,    name)) continue;
                        if (!matchOneOfWithLog(doc, "topic",    curTopics,     name)) continue;

                        // 통과
                        passed++;
                        searchResults.add(name);
                        Log.v(TAG, "PASS doc=" + name + " [category=" + c + ", season=" + s + ", topic=" + t + "]");
                    }

                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Search finished. total=" + total + ", passed=" + passed +
                            ", showing=" + searchResults.size());

                    if (searchResults.isEmpty()) {
                        Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore get() failed: " + e.getMessage(), e);
                    Toast.makeText(this, "검색 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * 선택 리스트(selected)가 비어 있으면 조건 통과.
     * 비어 있지 않으면 문서의 field 값이 selected 중 하나라도 포함되어야 통과.
     * (로그 포함 버전)
     */
    private boolean matchOneOfWithLog(DocumentSnapshot doc, String field, List<String> selected, String docName) {
        if (selected == null || selected.isEmpty()) {
            Log.v(TAG, "PASS(" + field + ") because selected list is empty");
            return true; // 필터 미적용
        }

        Object v = doc.get(field);
        if (v == null) {
            Log.v(TAG, "SKIP(" + field + ") doc=" + docName + " because field is null");
            return false;
        }

        boolean ok;
        if (v instanceof List) {
            ok = false;
            for (Object o : (List<?>) v) {
                if (o != null && selected.contains(o.toString())) {
                    ok = true; break;
                }
            }
        } else {
            ok = selected.contains(v.toString());
        }

        if (!ok) {
            Log.v(TAG, "SKIP(" + field + ") doc=" + docName + " fieldValue=" + v + " not in " + selected);
        }
        return ok;
    }
}
