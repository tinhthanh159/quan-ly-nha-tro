package com.example.quan_ly_tro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.entity.ThuChi;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.google.android.material.card.MaterialCardView;

/**
 * Adapter cho danh sách thu/chi
 */
public class ThuChiAdapter extends ListAdapter<ThuChi, ThuChiAdapter.ThuChiViewHolder> {
    
    private final OnThuChiClickListener listener;
    
    public interface OnThuChiClickListener {
        void onThuChiClick(ThuChi thuChi);
        void onThuChiLongClick(ThuChi thuChi);
    }
    
    public ThuChiAdapter(OnThuChiClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }
    
    private static final DiffUtil.ItemCallback<ThuChi> DIFF_CALLBACK = new DiffUtil.ItemCallback<ThuChi>() {
        @Override
        public boolean areItemsTheSame(@NonNull ThuChi oldItem, @NonNull ThuChi newItem) {
            return oldItem.getId() == newItem.getId();
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull ThuChi oldItem, @NonNull ThuChi newItem) {
            return oldItem.getSoTien() == newItem.getSoTien() &&
                   oldItem.getLoai().equals(newItem.getLoai());
        }
    };
    
    @NonNull
    @Override
    public ThuChiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thu_chi, parent, false);
        return new ThuChiViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ThuChiViewHolder holder, int position) {
        ThuChi thuChi = getItem(position);
        holder.bind(thuChi);
    }
    
    class ThuChiViewHolder extends RecyclerView.ViewHolder {
        
        private final MaterialCardView cardView;
        private final FrameLayout frameIcon;
        private final ImageView ivIcon;
        private final TextView tvMoTa;
        private final TextView tvNgay;
        private final TextView tvSoTien;
        
        public ThuChiViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            frameIcon = itemView.findViewById(R.id.frame_icon);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvMoTa = itemView.findViewById(R.id.tv_mo_ta);
            tvNgay = itemView.findViewById(R.id.tv_ngay);
            tvSoTien = itemView.findViewById(R.id.tv_so_tien);
        }
        
        public void bind(ThuChi thuChi) {
            Context context = itemView.getContext();
            
            // Mô tả
            String moTa = thuChi.getMoTa();
            if (moTa == null || moTa.isEmpty()) {
                moTa = thuChi.getDanhMuc();
            }
            tvMoTa.setText(moTa);
            
            // Ngày
            tvNgay.setText(FormatUtils.formatDate(thuChi.getNgayGiaoDich()));
            
            // Số tiền và màu sắc
            if (ThuChi.LOAI_THU.equals(thuChi.getLoai())) {
                tvSoTien.setText("+" + FormatUtils.formatCurrency(thuChi.getSoTien()));
                tvSoTien.setTextColor(ContextCompat.getColor(context, R.color.income));
                frameIcon.setBackgroundResource(R.drawable.bg_circle_income);
                ivIcon.setImageResource(R.drawable.ic_income);
                ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.income));
            } else {
                tvSoTien.setText("-" + FormatUtils.formatCurrency(thuChi.getSoTien()));
                tvSoTien.setTextColor(ContextCompat.getColor(context, R.color.expense));
                frameIcon.setBackgroundResource(R.drawable.bg_circle_expense);
                ivIcon.setImageResource(R.drawable.ic_expense);
                ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.expense));
            }
            
            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onThuChiClick(thuChi);
                }
            });
            
            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onThuChiLongClick(thuChi);
                    return true;
                }
                return false;
            });
        }
    }
}
