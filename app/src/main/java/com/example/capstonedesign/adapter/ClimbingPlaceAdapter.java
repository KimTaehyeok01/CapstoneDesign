package com.example.capstonedesign.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstonedesign.R;
import com.example.yourapp.model.ClimbingPlace;

import java.util.List;

public class ClimbingPlaceAdapter extends RecyclerView.Adapter<ClimbingPlaceAdapter.ViewHolder> {

    private List<ClimbingPlace> placeList;

    public ClimbingPlaceAdapter(List<ClimbingPlace> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public ClimbingPlaceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_climbing_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClimbingPlaceAdapter.ViewHolder holder, int position) {
        ClimbingPlace place = placeList.get(position);
        holder.placeName.setText(place.getName());
        holder.placeAddress.setText(place.getAddress());
        holder.placeRegion.setText(place.getRegion());
        holder.placePrice.setText(place.getPriceInfo());

        // 아이콘 이미지 설정은 필요시 추가 (지금은 기본 drawable 사용 중)
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView placeName, placeAddress, placeRegion, placePrice;
        ImageView heartIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.placeName);
            placeAddress = itemView.findViewById(R.id.placeAddress);
            placeRegion = itemView.findViewById(R.id.placeRegion); // XML에 추가 필요
            placePrice = itemView.findViewById(R.id.placePrice);   // XML에 추가 필요
            heartIcon = itemView.findViewById(R.id.heartIcon);
        }
    }
}
