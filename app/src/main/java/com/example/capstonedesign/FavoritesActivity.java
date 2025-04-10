package com.example.capstonedesign;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstonedesign.adapter.FavoritePlaceAdapter;
import com.example.capstonedesign.model.FavoritePlace;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoritePlaceAdapter adapter;
    private List<FavoritePlace> placeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites); // 너가 만든 찜 목록 레이아웃

        recyclerView = findViewById(R.id.recyclerView);

        // 예시 찜 데이터
        placeList = new ArrayList<>();
        placeList.add(new FavoritePlace("역삼 클라이밍랩", "서울 강남구 테헤란로30길 49", "서울 강남", "무료이용 가능", R.drawable.button1));
        placeList.add(new FavoritePlace("스크바다이브", "서울 강남구 논현로76길 27", "서울 강남", "무료이용 가능", R.drawable.group1));
        placeList.add(new FavoritePlace("쿨스에듀케이션", "서울 강남구 논현로76길 27", "서울 강남", "무료이용 가능", R.drawable.group2));

        adapter = new FavoritePlaceAdapter(placeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
