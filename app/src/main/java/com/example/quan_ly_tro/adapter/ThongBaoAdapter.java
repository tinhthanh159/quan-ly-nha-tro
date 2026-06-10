package com.example.quan_ly_tro.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.entity.ThongBao;
import com.example.quan_ly_tro.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách thông báo với trạng thái đã đọc/chưa đọc
 */
public class ThongBaoAdapter extends RecyclerView.Adapter<ThongBaoAdapter.ViewHolder> {

    private List<ThongBao> thongBaoList = new ArrayList<>();
    private OnThongBaoClickListener listener;

    public interface OnThongBaoClickListener {
        void onThongBaoClick(ThongBao thongBao);
        void onThongBaoLongClick(ThongBao thongBao);
    }

    public void setThongBaoList(List<ThongBao> list) {
        this.thongBaoList = list;
        notifyDataSetChanged();
    }

    public void setOnThongBaoClickListener(OnThongBaoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thong_bao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThongBao tb = thongBaoList.get(position);
        holder.tvTitle.setText(tb.getTieuDe());
        holder.tvContent.setText(tb.getNoiDung());
        holder.tvTime.setText(FormatUtils.formatDate(tb.getNgayTao()));
        
        String author = tb.getNguoiGui() != null ? tb.getNguoiGui() : "Chủ trọ";
        holder.tvAuthor.setText("— Từ: " + author);

        // Hiệu ứng cho trạng thái đã đọc/chưa đọc
        if (tb.isRead()) {
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary));
            holder.itemView.setAlpha(0.8f);
            holder.dotUnread.setVisibility(View.GONE);
        } else {
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.on_surface));
            holder.itemView.setAlpha(1.0f);
            holder.dotUnread.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onThongBaoClick(tb);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onThongBaoLongClick(tb);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return thongBaoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime, tvAuthor;
        View dotUnread;
        
        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_title);
            tvContent = view.findViewById(R.id.tv_content);
            tvTime = view.findViewById(R.id.tv_time);
            tvAuthor = view.findViewById(R.id.tv_author);
            dotUnread = view.findViewById(R.id.view_unread_dot);
        }
    }
}
