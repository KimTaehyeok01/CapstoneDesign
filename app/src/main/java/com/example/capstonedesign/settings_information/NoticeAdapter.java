package com.example.capstonedesign.settings_information;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.capstonedesign.R; // R 클래스 import
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    private final Context context;
    private final List<Notice> noticeList;

    public NoticeAdapter(Context context, List<Notice> noticeList) {
        this.context = context;
        this.noticeList = noticeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        holder.bind(notice);
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvContent;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notice_title);
            tvDate = itemView.findViewById(R.id.tv_notice_date);
            tvContent = itemView.findViewById(R.id.tv_notice_content);

            itemView.setOnClickListener(v -> {
                boolean isVisible = tvContent.getVisibility() == View.VISIBLE;
                tvContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            });
        }

        void bind(Notice notice) {
            tvTitle.setText(notice.getTitle());
            tvContent.setText(notice.getContent());

            Timestamp timestamp = notice.getTimestamp();
            if (timestamp != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd", Locale.KOREA);
                tvDate.setText(sdf.format(timestamp.toDate()));
            } else {
                tvDate.setText("");
            }
        }
    }
}