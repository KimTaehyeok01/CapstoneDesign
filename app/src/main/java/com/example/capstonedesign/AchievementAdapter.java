package com.example.capstonedesign;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private List<Achievement> achievementList;

    public AchievementAdapter(List<Achievement> achievementList) {
        this.achievementList = achievementList;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievementList.get(position);
        holder.badge.setImageResource(achievement.getBadgeResId());
        holder.title.setText(achievement.getTitle());
        holder.description.setText(achievement.getDescription());
        holder.date.setText(achievement.getDate());
    }

    @Override
    public int getItemCount() {
        return achievementList.size();
    }

    public static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView badge;
        TextView title, description, date;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            badge = itemView.findViewById(R.id.iv_badge);
            title = itemView.findViewById(R.id.tv_achievement_title);
            description = itemView.findViewById(R.id.tv_achievement_description);
            date = itemView.findViewById(R.id.tv_achievement_date);
        }
    }
}