package com.example.capstonedesign;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {

    private final List<String> items;
    private final OnItemClickListener itemClickListener;
    private final OnDeleteClickListener deleteClickListener;

    // 아이템 클릭과 삭제 버튼 클릭을 처리하기 위한 인터페이스
    public interface OnItemClickListener {
        void onItemClick(String item);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public RecentSearchAdapter(List<String> items, OnItemClickListener itemClickListener, OnDeleteClickListener deleteClickListener) {
        this.items = items;
        this.itemClickListener = itemClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 각 아이템에 사용할 레이아웃 파일을 지정해야 합니다. (아래에서 만들 예정)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSearchTerm;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvSearchTerm = itemView.findViewById(R.id.tvSearchTerm);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(final String item, final int position) {
            tvSearchTerm.setText(item);
            // 전체 아이템 클릭 시
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(item));
            // 삭제 버튼 클릭 시
            btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(position));
        }
    }
}