package com.example.capstonedesign;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private final List<Map<String, Object>> placeList;
    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseUser currentUser;

    public RecommendationAdapter(Context context, List<Map<String, Object>> placeList) {
        this.context = context;
        this.placeList = placeList;
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> placeData = placeList.get(position);
        holder.bind(placeData);
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaceImage;
        TextView tvPlaceName, tvPlaceLocation, tvPlaceDistance;
        ImageButton btnFavorite;
        MaterialButton btnShare, btnViewNearby;
        boolean isFavorite = false;

        ViewHolder(View itemView) {
            super(itemView);
            ivPlaceImage = itemView.findViewById(R.id.iv_place_image);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            tvPlaceLocation = itemView.findViewById(R.id.tv_place_location);
            tvPlaceDistance = itemView.findViewById(R.id.tv_place_distance);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
            btnShare = itemView.findViewById(R.id.btn_share);
            btnViewNearby = itemView.findViewById(R.id.btn_view_nearby);
        }

        void bind(final Map<String, Object> placeData) {
            String name = placeData.get("name") != null ? placeData.get("name").toString() : "";
            String topic = placeData.get("topic") != null ? placeData.get("topic").toString() : "";
            String imageUrl = placeData.get("image") != null ? placeData.get("image").toString() : "";
            Double latitude = (Double) placeData.get("latitude");
            Double longitude = (Double) placeData.get("longitude");

            tvPlaceName.setText(name);
            tvPlaceLocation.setText(topic);
            tvPlaceDistance.setVisibility(View.GONE);

            if (!imageUrl.isEmpty()) {
                Glide.with(context).load(imageUrl).into(ivPlaceImage);
            }

            checkFavoriteStatus(name);
            btnFavorite.setOnClickListener(v -> toggleFavorite(name));

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PlaceDetailActivity.class);
                intent.putExtra("place_name", name);
                context.startActivity(intent);
            });

            btnShare.setOnClickListener(v -> {
                // 1. 공유할 텍스트 내용 만들기
                String shareText = "이런 장소는 어떠세요?\n\n📍 " + name;

                // 2. 위도, 경도 값이 있을 경우 구글 지도 링크 추가
                if (latitude != null && longitude != null) {
                    String mapLink = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
                    shareText += "\n\n지도에서 위치 확인하기:\n" + mapLink;
                }

                // 3. Intent.ACTION_SEND를 사용하여 공유 인텐트 생성
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

                // 4. 사용자가 공유할 앱을 선택할 수 있는 Chooser(선택창) 띄우기
                context.startActivity(Intent.createChooser(shareIntent, "장소 공유하기"));
            });

            btnViewNearby.setOnClickListener(v -> {
                if (latitude != null && longitude != null) {
                    Intent intent = new Intent(context, MapActivity.class);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("place_name", name);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "위치 정보가 없는 장소입니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void checkFavoriteStatus(String placeName) {
            if (currentUser == null) return;
            DocumentReference favRef = db.collection("users").document(currentUser.getUid()).collection("favorites").document(placeName);
            favRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    isFavorite = true;
                    btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
                } else {
                    isFavorite = false;
                    btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                }
            });
        }

        private void toggleFavorite(String placeName) {
            if (currentUser == null) {
                Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            DocumentReference favRef = db.collection("users").document(currentUser.getUid()).collection("favorites").document(placeName);
            if (!isFavorite) {
                Map<String, Object> favoriteData = new HashMap<>();
                favoriteData.put("name", placeName);
                favRef.set(favoriteData).addOnSuccessListener(unused -> {
                    Toast.makeText(context, "찜 목록에 추가했습니다.", Toast.LENGTH_SHORT).show();
                    isFavorite = true;
                    btnFavorite.setImageResource(R.drawable.baseline_favorite_24);
                });
            } else {
                favRef.delete().addOnSuccessListener(unused -> {
                    Toast.makeText(context, "찜 목록에서 삭제했습니다.", Toast.LENGTH_SHORT).show();
                    isFavorite = false;
                    btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                });
            }
        }
    }
}