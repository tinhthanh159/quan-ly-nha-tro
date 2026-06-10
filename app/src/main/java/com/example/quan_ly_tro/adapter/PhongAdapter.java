package com.example.quan_ly_tro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.google.android.material.card.MaterialCardView;

/**
 * Adapter cho danh sách phòng
 */
public class PhongAdapter extends ListAdapter<Phong, PhongAdapter.PhongViewHolder> {
    
    private final OnPhongClickListener listener;
    
    public interface OnPhongClickListener {
        void onPhongClick(Phong phong);
        void onPhongLongClick(Phong phong);
    }
    
    public PhongAdapter(OnPhongClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }
    
    private static final DiffUtil.ItemCallback<Phong> DIFF_CALLBACK = new DiffUtil.ItemCallback<Phong>() {
        @Override
        public boolean areItemsTheSame(@NonNull Phong oldItem, @NonNull Phong newItem) {
            return oldItem.getId() == newItem.getId();
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull Phong oldItem, @NonNull Phong newItem) {
            return oldItem.getSoPhong().equals(newItem.getSoPhong()) &&
                   oldItem.getTrangThai().equals(newItem.getTrangThai()) &&
                   oldItem.getGiaThue() == newItem.getGiaThue();
        }
    };
    
    @NonNull
    @Override
    public PhongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_phong, parent, false);
        return new PhongViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PhongViewHolder holder, int position) {
        Phong phong = getItem(position);
        holder.bind(phong);
    }
    
    class PhongViewHolder extends RecyclerView.ViewHolder {
        
        private final MaterialCardView cardView;
        private final TextView tvSoPhongIcon;
        private final TextView tvSoPhong;
        private final TextView tvLoaiPhong;
        private final TextView tvGiaThue;
        private final TextView tvTrangThai;
        
        public PhongViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvSoPhongIcon = itemView.findViewById(R.id.tv_so_phong_icon);
            tvSoPhong = itemView.findViewById(R.id.tv_so_phong);
            tvLoaiPhong = itemView.findViewById(R.id.tv_loai_phong);
            tvGiaThue = itemView.findViewById(R.id.tv_gia_thue);
            tvTrangThai = itemView.findViewById(R.id.tv_trang_thai);
        }
        
        public void bind(Phong phong) {
            Context context = itemView.getContext();
            
            // Số phòng
            String soPhong = phong.getSoPhong();
            tvSoPhong.setText("Phòng " + soPhong);
            tvSoPhongIcon.setText(soPhong.length() > 3 ? soPhong.substring(0, 3) : soPhong);
            
            // Loại phòng và diện tích
            String loaiDienTich = phong.getLoaiPhong();
            if (phong.getDienTich() > 0) {
                loaiDienTich += " • " + FormatUtils.formatArea(phong.getDienTich());
            }
            tvLoaiPhong.setText(loaiDienTich);
            
            // Giá thuê
            tvGiaThue.setText(FormatUtils.formatCurrency(phong.getGiaThue()) + "/tháng");
            
            // Trạng thái với màu sắc
            tvTrangThai.setText(phong.getTrangThai());
            setStatusColor(context, phong.getTrangThai());
            
            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPhongClick(phong);
                }
            });
            
            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onPhongLongClick(phong);
                    return true;
                }
                return false;
            });
        }
        
        private void setStatusColor(Context context, String trangThai) {
            int textColor;
            int bgColor;
            
            switch (trangThai) {
                case Phong.TRANG_THAI_TRONG:
                    textColor = ContextCompat.getColor(context, R.color.status_available);
                    bgColor = ContextCompat.getColor(context, R.color.status_available_bg);
                    break;
                case Phong.TRANG_THAI_DANG_THUE:
                    textColor = ContextCompat.getColor(context, R.color.status_occupied);
                    bgColor = ContextCompat.getColor(context, R.color.status_occupied_bg);
                    break;
                case Phong.TRANG_THAI_DANG_SUA:
                    textColor = ContextCompat.getColor(context, R.color.status_maintenance);
                    bgColor = ContextCompat.getColor(context, R.color.status_maintenance_bg);
                    break;
                default:
                    textColor = ContextCompat.getColor(context, R.color.gray);
                    bgColor = ContextCompat.getColor(context, R.color.gray_light);
            }
            
            tvTrangThai.setTextColor(textColor);
            tvTrangThai.getBackground().setTint(bgColor);
        }
    }
}
