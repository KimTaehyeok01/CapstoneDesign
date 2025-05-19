package com.example.capstonedesign;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.core.widget.NestedScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TodayRecommendActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;
    private static final String TAG = "TodayRec";

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ImageButton btnBackRecommend;
    private TextView tvWeatherRecommend;
    private NestedScrollView scrollView;
    private LinearLayout itemContainer;
    private Button recommendButton;
    private TextView resultTextView;

    private final String weatherApiKey = "f5a32755e587860fe98d96a6a54af17f";
    private final String googleApiKey  = "AIzaSyDhaN2JivN_B886eY9yrzpF2YnPaCy2E6E";

    private GPTRecommender recommender;
    private final OkHttpClient httpClient = new OkHttpClient();
    private Handler fallbackHandler;
    private Runnable fallbackRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_recommend);

        btnBackRecommend   = findViewById(R.id.btnBackRecommend);
        tvWeatherRecommend = findViewById(R.id.tvWeatherRecommend);
        scrollView         = findViewById(R.id.scroll_view);
        itemContainer      = findViewById(R.id.item_container);
        recommendButton    = findViewById(R.id.recommendButton);
        resultTextView     = findViewById(R.id.resultTextView);

        db                  = FirebaseFirestore.getInstance();
        currentUser         = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        recommender         = new GPTRecommender();

        fallbackHandler = new Handler(Looper.getMainLooper());

        btnBackRecommend.setOnClickListener(v -> finish());

        recommendButton.setOnClickListener(v -> {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_UP));
            resultTextView.setText("추천 중...");
            itemContainer.animate()
                    .alpha(0f).setDuration(200)
                    .withEndAction(() -> {
                        fetchGPTRecommendations();
                        itemContainer.animate().alpha(1f).setDuration(200).start();
                    }).start();
        });

        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else {
            fetchLocationAndInit();
        }
    }

    private void fetchLocationAndInit() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        fetchWeather(location.getLatitude(), location.getLongitude());
                    } else {
                        tvWeatherRecommend.setText("위치를 찾을 수 없습니다.");
                    }
                });
    }

    private void fetchWeather(double lat, double lon) {
        final String url = "https://api.openweathermap.org/data/2.5/weather?lat="
                + lat + "&lon=" + lon
                + "&appid=" + weatherApiKey
                + "&lang=kr&units=metric";

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection)new java.net.URL(url).openConnection();
                conn.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();

                JSONObject j = new JSONObject(sb.toString());
                String desc = j.getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description");
                double temp = j.getJSONObject("main").getDouble("temp");

                runOnUiThread(() ->
                        tvWeatherRecommend.setText("현재 날씨: " + desc + " (" + temp + "°C)")
                );
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        tvWeatherRecommend.setText("날씨 불러오기 실패")
                );
            }
        }).start();
    }

    private void fetchGPTRecommendations() {
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            resultTextView.setText("추천 실패");
            return;
        }

        fallbackRunnable = () -> {
            if ("추천 중...".contentEquals(resultTextView.getText())) {
                Toast.makeText(this, "응답이 늦어 기본 추천으로 전환합니다.", Toast.LENGTH_SHORT).show();
                fetchRecommendations();
            }
        };
        fallbackHandler.postDelayed(fallbackRunnable, 5000);

        recommender.getRecommendations(currentUser.getUid())
                .addOnSuccessListener(raw -> {
                    fallbackHandler.removeCallbacks(fallbackRunnable);
                    Log.d(TAG, "GPT raw: " + raw);
                    try {
                        JSONArray arr = new JSONArray(raw);
                        if (arr.length() == 0) {
                            fetchRecommendations();
                            return;
                        }
                        itemContainer.removeAllViews();

                        final int limit = Math.min(arr.length(), 10);
                        final AtomicInteger processed = new AtomicInteger(0);
                        final AtomicInteger found     = new AtomicInteger(0);

                        for (int i = 0; i < limit; i++) {
                            final String name = arr.getJSONObject(i).optString("name");
                            if (name == null || name.isEmpty()) {
                                onProcessed(limit, processed, found);
                                continue;
                            }
                            // **필드 기반 조회로 수정**
                            db.collection("sports_locations")
                                    .whereEqualTo("name", name)
                                    .get()
                                    .addOnSuccessListener((QuerySnapshot qs) -> {
                                        if (!qs.isEmpty()) {
                                            for (DocumentSnapshot doc : qs.getDocuments()) {
                                                found.incrementAndGet();
                                                addRecommendationCard(doc);
                                            }
                                        } else {
                                            fetchGooglePlace(name, limit, processed, found);
                                        }
                                        onProcessed(limit, processed, found);
                                    })
                                    .addOnFailureListener(e -> {
                                        fetchGooglePlace(name, limit, processed, found);
                                        onProcessed(limit, processed, found);
                                    });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        fetchRecommendations();
                    }
                })
                .addOnFailureListener(e -> {
                    fallbackHandler.removeCallbacks(fallbackRunnable);
                    fetchRecommendations();
                });
    }

    private void onProcessed(int limit, AtomicInteger processed, AtomicInteger found) {
        if (processed.incrementAndGet() == limit) {
            if (found.get() == 0) {
                fetchRecommendations();
            } else {
                resultTextView.setText("추천완료");
            }
        }
    }

    private void fetchRecommendations() {
        db.collection("sports_locations").get()
                .addOnSuccessListener(qs -> {
                    java.util.List<DocumentSnapshot> docs = qs.getDocuments();
                    java.util.Collections.shuffle(docs);
                    itemContainer.removeAllViews();
                    for (int i = 0; i < Math.min(docs.size(), 10); i++) {
                        addRecommendationCard(docs.get(i));
                    }
                    resultTextView.setText("추천완료");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "랜덤 추천 실패", Toast.LENGTH_SHORT).show()
                );
    }

    private void fetchGooglePlace(String name, int limit, AtomicInteger processed, AtomicInteger found) {
        try {
            String query = URLEncoder.encode(name, "UTF-8");
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json"
                    + "?query=" + query
                    + "&key="   + googleApiKey;
            Request req = new Request.Builder().url(url).build();
            httpClient.newCall(req).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    onProcessed(limit, processed, found);
                }
                @Override public void onResponse(Call call, Response resp) throws IOException {
                    if (!resp.isSuccessful()) {
                        onProcessed(limit, processed, found);
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(resp.body().string());
                        JSONArray results = json.optJSONArray("results");
                        if (results == null || results.length() == 0) {
                            onProcessed(limit, processed, found);
                            return;
                        }
                        String placeId = results.getJSONObject(0).getString("place_id");
                        fetchGooglePlaceDetails(placeId, name, limit, processed, found);
                    } catch (Exception ex) {
                        onProcessed(limit, processed, found);
                    }
                }
            });
        } catch (Exception e) {
            onProcessed(limit, processed, found);
        }
    }

    private void fetchGooglePlaceDetails(String placeId, String name,
                                         int limit, AtomicInteger processed, AtomicInteger found) {
        String fields = "formatted_address,photos,formatted_phone_number";
        String url = "https://maps.googleapis.com/maps/api/place/details/json"
                + "?place_id=" + placeId
                + "&fields="   + fields
                + "&key="      + googleApiKey;
        Request req = new Request.Builder().url(url).build();
        httpClient.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                onProcessed(limit, processed, found);
            }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                if (!resp.isSuccessful()) {
                    onProcessed(limit, processed, found);
                    return;
                }
                try {
                    JSONObject result = new JSONObject(resp.body().string()).getJSONObject("result");
                    String address = result.optString("formatted_address","");
                    String phone   = result.optString("formatted_phone_number","");
                    String photoUrl;
                    if (result.has("photos")) {
                        String photoRef = result.getJSONArray("photos")
                                .getJSONObject(0)
                                .getString("photo_reference");
                        photoUrl = "https://maps.googleapis.com/maps/api/place/photo"
                                + "?maxwidth=400"
                                + "&photoreference=" + photoRef
                                + "&key=" + googleApiKey;
                    } else {
                        photoUrl = "";
                    }
                    runOnUiThread(() -> {
                        found.incrementAndGet();
                        addGoogleCard(name, address, phone, photoUrl);
                        onProcessed(limit, processed, found);
                    });
                } catch (Exception ex) {
                    onProcessed(limit, processed, found);
                }
            }
        });
    }

    private void addRecommendationCard(DocumentSnapshot doc) {
        if (!doc.exists()) return;
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_nearby_card, itemContainer, false);
        ImageView img = card.findViewById(R.id.img_place);
        TextView  n   = card.findViewById(R.id.tv_place_name);
        TextView  a   = card.findViewById(R.id.tv_place_address);
        TextView  r   = card.findViewById(R.id.tv_place_region);
        TextView  p   = card.findViewById(R.id.tv_place_price);
        ImageView fav = card.findViewById(R.id.img_favorite);

        String name     = doc.getString("name");
        String address  = doc.getString("address");
        String region   = doc.getString("topic");
        String details  = doc.getString("details");
        String imageUrl = doc.getString("image");

        n.setText(name    != null ? name    : "장소명 없음");
        a.setText(address != null ? address : "주소 없음");
        r.setText(region  != null ? region  : "지역 없음");
        p.setText(details != null ? details : "정보 없음");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(img);
        } else {
            img.setImageResource(R.drawable.ic_climb);
        }

        if (currentUser != null && name != null) {
            DocumentReference favRef = db.collection("users")
                    .document(currentUser.getUid())
                    .collection("favorites")
                    .document(name);
            favRef.get().addOnSuccessListener(snap -> {
                fav.setImageResource(
                        snap.exists()
                                ? R.drawable.baseline_favorite_24
                                : R.drawable.baseline_favorite_border_24
                );
            });
        } else {
            fav.setImageResource(R.drawable.baseline_favorite_border_24);
        }

        fav.setOnClickListener(v -> toggleFavorite(doc, fav));
        card.setOnClickListener(v -> {
            Intent i = new Intent(this, PlaceDetailActivity.class);
            i.putExtra("place_name", name);
            startActivity(i);
        });
        itemContainer.addView(card);
    }

    private void addGoogleCard(String name, String address, String phone, String photoUrl) {
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_nearby_card, itemContainer, false);
        ImageView img = card.findViewById(R.id.img_place);
        TextView  n   = card.findViewById(R.id.tv_place_name);
        TextView  a   = card.findViewById(R.id.tv_place_address);
        TextView  r   = card.findViewById(R.id.tv_place_region);
        TextView  p   = card.findViewById(R.id.tv_place_price);
        ImageView fav = card.findViewById(R.id.img_favorite);

        n.setText(name);
        a.setText(address.isEmpty() ? "주소 정보 없음" : address);
        r.setText("");
        p.setText(phone.isEmpty() ? "전화번호 없음" : phone);

        if (!photoUrl.isEmpty()) {
            Glide.with(this).load(photoUrl).into(img);
        } else {
            img.setImageResource(R.drawable.ic_climb);
        }
        fav.setVisibility(View.GONE);
        card.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=" + Uri.encode(name + " " + address)));
            i.setPackage("com.google.android.apps.maps");
            startActivity(i);
        });
        itemContainer.addView(card);
    }

    private void toggleFavorite(DocumentSnapshot doc, ImageView favButton) {
        String name = doc.getString("name");
        String addr = doc.getString("address");
        if (currentUser == null || name == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference favRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .document(name);
        favRef.get().addOnSuccessListener(snap -> {
            if (snap.exists()) {
                favRef.delete().addOnSuccessListener(u -> {
                    favButton.setImageResource(R.drawable.baseline_favorite_border_24);
                    Toast.makeText(this, "찜 해제됨", Toast.LENGTH_SHORT).show();
                });
            } else {
                Map<String,Object> data = new HashMap<>();
                data.put("name", name);
                data.put("address", addr);
                favRef.set(data).addOnSuccessListener(u -> {
                    favButton.setImageResource(R.drawable.baseline_favorite_24);
                    Toast.makeText(this, "찜 추가됨", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndInit();
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            fetchRecommendations();
        }
    }

    private static class GPTRecommender {
        private final FirebaseFunctions functions;
        GPTRecommender() {
            functions = FirebaseFunctions.getInstance("asia-northeast3");
        }
        Task<String> getRecommendations(String userId) {
            Map<String,Object> data = new HashMap<>();
            data.put("userId", userId);
            return functions.getHttpsCallable("recommendPlacesByGPT")
                    .call(data)
                    .continueWith(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        Object raw = task.getResult().getData();
                        if (raw instanceof String) return (String)raw;
                        @SuppressWarnings("unchecked")
                        Map<String,Object> map = (Map<String,Object>)raw;
                        return (String)map.get("result");
                    });
        }
    }
}