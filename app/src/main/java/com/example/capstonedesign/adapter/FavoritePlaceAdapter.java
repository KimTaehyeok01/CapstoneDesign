package com.example.capstonedesign.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capstonedesign.R;
import com.example.capstonedesign.model.FavoritePlace;

import java.util.List;

public class FavoritePlaceAdapter extends RecyclerView.Adapter<FavoritePlaceAdapter.PlaceViewHolder> {

    private List<FavoritePlace> placeList;

    public FavoritePlaceAdapter(List<FavoritePlace> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_climbing_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        FavoritePlace place = placeList.get(position);
        holder.placeName.setText(place.getName());
        holder.placeAddress.setText(place.getAddress());
        holder.placeRegion.setText(place.getRegion());
        holder.placePrice.setText(place.getPriceInfo());
        holder.placeImage.setImageResource(place.getImageResId());
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView placeImage;
        TextView placeName, placeAddress, placeRegion, placePrice;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            placeImage = itemView.findViewById(R.id.placeImage);
            placeName = itemView.findViewById(R.id.placeName);
            placeAddress = itemView.findViewById(R.id.placeAddress);
            placeRegion = itemView.findViewById(R.id.placeRegion);
            placePrice = itemView.findViewById(R.id.placePrice);
        }
    }
}
