package com.example.capstonedesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InquiryAdapter extends RecyclerView.Adapter<InquiryAdapter.ViewHolder> {

    private final Context context;
    private final List<Inquiry> inquiryList;

    public InquiryAdapter(Context context, List<Inquiry> inquiryList) {
        this.context = context;
        this.inquiryList = inquiryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inquiry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Inquiry inquiry = inquiryList.get(position);
        holder.bind(inquiry);
    }

    @Override
    public int getItemCount() {
        return inquiryList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvTitle, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tv_inquiry_status);
            tvTitle = itemView.findViewById(R.id.tv_inquiry_title);
            tvDate = itemView.findViewById(R.id.tv_inquiry_date);
        }

        void bind(Inquiry inquiry) {
            tvTitle.setText(inquiry.getTitle());

            Timestamp timestamp = inquiry.getTimestamp();
            if (timestamp != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd", Locale.KOREA);
                tvDate.setText(sdf.format(timestamp.toDate()));
            } else {
                tvDate.setText("");
            }

            String status = inquiry.getStatus();
            if ("pending".equalsIgnoreCase(status)) {
                tvStatus.setText("대기중");
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            } else {
                tvStatus.setText("답변완료");
                tvStatus.setBackgroundResource(R.drawable.bg_status_resolved);
            }
        }
    }
}