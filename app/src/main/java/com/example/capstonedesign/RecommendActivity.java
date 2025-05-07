package com.example.capstonedesign;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class RecommendActivity extends AppCompatActivity {

    private TextView resultTextView;
    private Button recommendButton;
    private GPTRecommender recommender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_recommend); // XML 연결

        resultTextView = findViewById(R.id.resultTextView); // XML ID에 맞게 변경
        recommendButton = findViewById(R.id.recommendButton);
        recommender = new GPTRecommender();

        // Firebase 로그인된 사용자 ID 가져오기
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recommendButton.setOnClickListener(v -> {
            resultTextView.setText("추천 중...");

            LinearLayout itemContainer = findViewById(R.id.item_container);
            itemContainer.removeAllViews(); // 기존 카드 제거

            recommender.getRecommendations(userId)
                    .addOnSuccessListener(result -> {
                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            LayoutInflater inflater = LayoutInflater.from(RecommendActivity.this);

                            int count = Math.min(5, jsonArray.length()); // 카드 개수 제한

                            // 👉 여기서 화면에 출력할 디버그 메시지 만들기
                            StringBuilder debugInfo = new StringBuilder();
                            debugInfo.append("받은 추천 개수: ").append(jsonArray.length()).append("\n");

                            for (int i = 0; i < count; i++) {
                                JSONObject item = jsonArray.getJSONObject(i);

                                // 카드 뷰 생성 및 표시
                                View cardView = inflater.inflate(R.layout.item_today_recommend, itemContainer, false);

                                ((TextView) cardView.findViewById(R.id.tv_place_name))
                                        .setText(item.optString("name", "이름 없음"));
                                ((TextView) cardView.findViewById(R.id.tv_place_address))
                                        .setText(item.optString("address", "주소 없음"));
                                ((TextView) cardView.findViewById(R.id.tv_place_region))
                                        .setText(item.optString("region", "지역 정보 없음"));
                                ((TextView) cardView.findViewById(R.id.tv_place_price))
                                        .setText(item.optString("price", "가격 정보 없음"));

                                itemContainer.addView(cardView);

                                // 👉 디버그 텍스트에 추가
                                debugInfo.append(i + 1).append(". ").append(item.optString("name")).append("\n");
                            }

                            // 결과를 resultTextView에 출력
                            resultTextView.setText(debugInfo.toString());

                        } catch (JSONException e) {
                            resultTextView.setText("파싱 오류: " + e.getMessage());
                        }
                    })
                    .addOnFailureListener(e -> {
                        resultTextView.setText("추천 실패: " + e.getMessage());
                    });
        });

    }
}
