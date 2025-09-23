package com.example.capstonedesign;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    public static final String EXTRA_CATEGORIES = "selectedTypes";
    public static final String EXTRA_SEASONS    = "selectedSeasons";
    public static final String EXTRA_TOPICS     = "selectedRegions";

    // UI 요소
    private EditText editSearch;
    private ImageButton btnSearchGlass, btnCancel, btnFilter;
    private TextView tvNearbySearch;

    // RecyclerView 및 어댑터
    private RecyclerView recyclerSearchResults;
    private SearchResultAdapter searchResultAdapter;
    private final List<String> searchResults = new ArrayList<>();
    private RecyclerView recyclerRecentSearches;
    private RecentSearchAdapter recentSearchAdapter;
    private final List<String> recentSearches = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SearchPrefs";
    private static final String KEY_RECENT_SEARCHES = "recent_searches";
    private FirebaseFirestore firestore;
    private ArrayList<String> curCategories = new ArrayList<>();
    private ArrayList<String> curSeasons    = new ArrayList<>();
    private ArrayList<String> curTopics     = new ArrayList<>();

    private final ActivityResultLauncher<Intent> openFilter =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    curCategories = data.getStringArrayListExtra(EXTRA_CATEGORIES);
                    curSeasons    = data.getStringArrayListExtra(EXTRA_SEASONS);
                    curTopics     = data.getStringArrayListExtra(EXTRA_TOPICS);
                    if (curCategories == null) curCategories = new ArrayList<>();
                    if (curSeasons == null)    curSeasons    = new ArrayList<>();
                    if (curTopics == null)     curTopics     = new ArrayList<>();
                    performSearch();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // UI 요소 초기화
        initViews();

        firestore = FirebaseFirestore.getInstance();

        // 검색 결과 어댑터 설정
        searchResultAdapter = new SearchResultAdapter(searchResults, item -> {
            Intent intent = new Intent(SearchActivity.this, PlaceDetailActivity.class);
            intent.putExtra("place_name", item);
            startActivity(intent);
        });
        recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerSearchResults.setAdapter(searchResultAdapter);

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 최근 검색어 어댑터 설정
        recentSearchAdapter = new RecentSearchAdapter(recentSearches,
                // 아이템 클릭 리스너
                item -> {
                    editSearch.setText(item);
                    performSearch();
                },
                // 삭제 버튼 클릭 리스너
                position -> {
                    deleteRecentSearch(position);
                }
        );
        recyclerRecentSearches.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecentSearches.setAdapter(recentSearchAdapter);

        // 이벤트 리스너 설정
        setupListeners();
        // 앱 시작 시 저장된 최근 검색어 불러오기
        loadRecentSearches();
        // 초기 화면 상태 설정
        showRecentSearchesView();
    }

    private void initViews() {
        ImageButton backButton = findViewById(R.id.back_button);
        editSearch      = findViewById(R.id.editSearch);
        btnSearchGlass  = findViewById(R.id.btnSearchGlass);
        btnCancel       = findViewById(R.id.btnCancel);
        tvNearbySearch  = findViewById(R.id.tvNearbySearch);
        btnFilter       = findViewById(R.id.btnFilter);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        recyclerRecentSearches = findViewById(R.id.recyclerRecentSearches);
    }

    private void setupListeners() {
        findViewById(R.id.back_button).setOnClickListener(view -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        btnSearchGlass.setOnClickListener(view -> performSearch());

        btnCancel.setOnClickListener(view -> {
            editSearch.setText("");
            // 검색어 지우면 다시 최근 검색어 목록을 보여줌
            showRecentSearchesView();
        });

        tvNearbySearch.setOnClickListener(view -> {
            Intent intent = new Intent(SearchActivity.this, MapActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnFilter.setOnClickListener(view -> {
            Intent intent = new Intent(SearchActivity.this, SearchFilterActivity.class);
            openFilter.launch(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void performSearch() {
        String keyword = editSearch.getText().toString().trim();
        Log.d(TAG, "performSearch() keyword='" + keyword + "', filters → categories=" + curCategories +
                ", seasons=" + curSeasons + ", topics=" + curTopics);

        // 검색어가 비어있지 않으면 최근 검색어에 저장
        if (!keyword.isEmpty()) {
            saveRecentSearch(keyword);
        }

        firestore.collection("sports_locations").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchResults.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name == null) continue;

                        boolean keywordMatch = keyword.isEmpty() || name.toLowerCase().contains(keyword.toLowerCase());
                        if (!keywordMatch) continue;

                        if (!matchOneOfWithLog(doc, "category", curCategories, name)) continue;
                        if (!matchOneOfWithLog(doc, "season",   curSeasons,    name)) continue;
                        if (!matchOneOfWithLog(doc, "topic",    curTopics,     name)) continue;

                        searchResults.add(name);
                    }

                    searchResultAdapter.notifyDataSetChanged();
                    showSearchResultsView();

                    if (searchResults.isEmpty()) {
                        Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "검색 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRecentSearches() {
        Set<String> savedSearches = sharedPreferences.getStringSet(KEY_RECENT_SEARCHES, new HashSet<>());
        recentSearches.clear();
        recentSearches.addAll(savedSearches);
        // 최신 검색어가 위로 오도록 정렬 (선택 사항)
        Collections.reverse(recentSearches);
        recentSearchAdapter.notifyDataSetChanged();
    }

    private void saveRecentSearch(String keyword) {
        // 기존 목록을 불러옴
        Set<String> savedSearches = new HashSet<>(sharedPreferences.getStringSet(KEY_RECENT_SEARCHES, new HashSet<>()));
        // 중복을 피하기 위해 먼저 제거 후 추가
        savedSearches.remove(keyword);
        savedSearches.add(keyword);

        // SharedPreferences에 저장
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_RECENT_SEARCHES, savedSearches);
        editor.apply();

        // 현재 화면의 목록도 업데이트
        loadRecentSearches();
    }

    private void deleteRecentSearch(int position) {
        String itemToDelete = recentSearches.get(position);

        // 현재 목록에서 제거
        recentSearches.remove(position);
        recentSearchAdapter.notifyItemRemoved(position);

        // SharedPreferences에서도 제거
        Set<String> savedSearches = new HashSet<>(sharedPreferences.getStringSet(KEY_RECENT_SEARCHES, new HashSet<>()));
        savedSearches.remove(itemToDelete);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_RECENT_SEARCHES, savedSearches);
        editor.apply();
    }

    private void showRecentSearchesView() {
        recyclerRecentSearches.setVisibility(View.VISIBLE);
        recyclerSearchResults.setVisibility(View.GONE);
    }

    private void showSearchResultsView() {
        recyclerRecentSearches.setVisibility(View.GONE);
        recyclerSearchResults.setVisibility(View.VISIBLE);
    }

    private boolean matchOneOfWithLog(DocumentSnapshot doc, String field, List<String> selected, String docName) {
        if (selected == null || selected.isEmpty()) return true;
        Object v = doc.get(field);
        if (v == null) return false;
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
        return ok;
    }
}