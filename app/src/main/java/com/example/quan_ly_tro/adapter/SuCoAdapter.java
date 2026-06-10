package com.example.quan_ly_tro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.entity.SuCo;
import com.example.quan_ly_tro.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách sự cố với giao diện hiện đại
 */
public class SuCoAdapter extends RecyclerView.Adapter<SuCoAdapter.ViewHolder> {

    private List<SuCo> suCoList = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(SuCo suCo);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(SuCo suCo);
    }

    public void setSuCoList(List<SuCo> list) {
        this.suCoList = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_su_co, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SuCo sc = suCoList.get(position);
        holder.tvRoom.setText(sc.getTenPhong() != null ? sc.getTenPhong() : "P.?");
        holder.tvTitle.setText(sc.getTieuDe());
        holder.tvDescription.setText(sc.getMoTa());
        holder.tvDate.setText("Gửi lúc: " + FormatUtils.formatDate(sc.getNgayTao()));
        
        String status = sc.getTrangThai();
        holder.tvStatus.setText(status);

        // Màu sắc theo trạng thái
        int color;
        if (SuCo.STATUS_PENDING.equals(status)) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.unpaid);
        } else if (SuCo.STATUS_IN_PROGRESS.equals(status)) {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.primary);
        } else {
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_available);
        }
        holder.tvStatus.setTextColor(color);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(sc);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(sc);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return suCoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoom, tvTitle, tvDescription, tvStatus, tvDate;
        
        ViewHolder(View view) {
            super(view);
            tvRoom = view.findViewById(R.id.tv_room);
            tvTitle = view.findViewById(R.id.tv_title);
            tvDescription = view.findViewById(R.id.tv_description);
            tvStatus = view.findViewById(R.id.tv_status);
            tvDate = view.findViewById(R.id.tv_date);
        }
    }
}
