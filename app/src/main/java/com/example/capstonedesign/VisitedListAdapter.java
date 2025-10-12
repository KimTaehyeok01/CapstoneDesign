package com.example.capstonedesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class VisitedListAdapter extends RecyclerView.Adapter<VisitedListAdapter.ViewHolder> {

    private final List<VisitedPlace> visitedPlaces;
    private final Context context;

    public VisitedListAdapter(Context context, List<VisitedPlace> visitedPlaces) {
        this.context = context;
        this.visitedPlaces = visitedPlaces;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visited_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VisitedPlace place = visitedPlaces.get(position);
        holder.tvPlaceName.setText(place.getName());
        holder.tvPlaceAddress.setText(place.getAddress()); // 주소에서 도시 이름만 추출하는 로직 추가 가능

        Glide.with(context)
                .load(place.getImageUrl())
                .into(holder.ivPlaceImage);
    }

    @Override
    public int getItemCount() {
        return visitedPlaces.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaceImage;
        TextView tvPlaceName, tvPlaceAddress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaceImage = itemView.findViewById(R.id.iv_place_image);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            tvPlaceAddress = itemView.findViewById(R.id.tv_place_address);
        }
    }
}