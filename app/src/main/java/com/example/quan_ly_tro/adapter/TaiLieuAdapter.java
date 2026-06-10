package com.example.quan_ly_tro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách tài liệu PDF với giao diện hiện đại
 */
public class TaiLieuAdapter extends RecyclerView.Adapter<TaiLieuAdapter.ViewHolder> {

    private List<File> fileList = new ArrayList<>();
    private OnTaiLieuClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnTaiLieuClickListener {
        void onFileClick(File file);
        void onFileMoreClick(File file);
    }

    public void setFileList(List<File> list) {
        this.fileList = list;
        notifyDataSetChanged();
    }

    public void setOnTaiLieuClickListener(OnTaiLieuClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tai_lieu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = fileList.get(position);
        holder.tvFileName.setText(file.getName());
        holder.tvFileDate.setText(sdf.format(new Date(file.lastModified())));
        
        boolean isContract = file.getParent().endsWith("Contracts");
        if (isContract) {
            holder.tvFileType.setText("Hợp đồng");
            holder.cardFileIcon.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_light));
            holder.ivFileIcon.setImageResource(R.drawable.ic_receipt);
            holder.ivFileIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
        } else {
            holder.tvFileType.setText("Báo cáo / Hóa đơn");
            holder.cardFileIcon.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.tertiary_container));
            holder.ivFileIcon.setImageResource(R.drawable.ic_chart);
            holder.ivFileIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.tertiary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onFileClick(file);
        });

        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onFileMoreClick(file);
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName, tvFileType, tvFileDate;
        ImageView ivFileIcon;
        MaterialCardView cardFileIcon;
        ImageButton btnMore;

        ViewHolder(View view) {
            super(view);
            tvFileName = view.findViewById(R.id.tv_file_name);
            tvFileType = view.findViewById(R.id.tv_file_type);
            tvFileDate = view.findViewById(R.id.tv_file_date);
            ivFileIcon = view.findViewById(R.id.iv_file_icon);
            cardFileIcon = view.findViewById(R.id.card_file_icon);
            btnMore = view.findViewById(R.id.btn_more);
        }
    }
}
