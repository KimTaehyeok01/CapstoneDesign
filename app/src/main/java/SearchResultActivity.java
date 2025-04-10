import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.capstonedesign.adapter.ClimbingPlaceAdapter;
import com.example.yourapp.model.ClimbingPlace;


import com.example.capstonedesign.R;

import java.util.ArrayList;
import java.util.List;




public class SearchResultActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClimbingPlaceAdapter adapter;
    private List<ClimbingPlace> placeList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interest); // 이게 XML이랑 연결

        recyclerView = findViewById(R.id.recyclerView);

        // 샘플 데이터 생성
        placeList = new ArrayList<>();
        placeList.add(new com.example.yourapp.model.ClimbingPlace("역삼 클라이밍랩", "서울 강남구 테헤란로30길 49", "서울 강남", "무료이용 가능"));
        placeList.add(new com.example.yourapp.model.ClimbingPlace("신림 클라임존", "서울 관악구 신림로 120", "서울 관악", "유료 이용"));

        Log.d("SearchResultActivity", "placeList size: " + placeList.size()); // <- 요기!!


        // 어댑터 연결
        adapter = new com.example.capstonedesign.adapter.ClimbingPlaceAdapter(placeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
