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
import com.example.quan_ly_tro.data.database.entity.HopDong;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter cho danh sách khách thuê - Hỗ trợ hiển thị trạng thái hợp đồng
 */
public class KhachThueAdapter extends ListAdapter<KhachThue, KhachThueAdapter.KhachThueViewHolder> {
    
    private final OnKhachThueClickListener listener;
    private Map<Integer, String> phongMap = new HashMap<>(); // phongId -> soPhong
    private Map<Integer, String> contractStatusMap = new HashMap<>(); // khachThueId -> status
    
    public interface OnKhachThueClickListener {
        void onKhachThueClick(KhachThue khachThue);
        void onKhachThueLongClick(KhachThue khachThue);
    }
    
    public KhachThueAdapter(OnKhachThueClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }
    
    public void setPhongMap(Map<Integer, String> phongMap) {
        this.phongMap = phongMap;
        notifyDataSetChanged();
    }

    public void setContractStatusMap(Map<Integer, String> map) {
        this.contractStatusMap = map;
        notifyDataSetChanged();
    }
    
    private static final DiffUtil.ItemCallback<KhachThue> DIFF_CALLBACK = new DiffUtil.ItemCallback<KhachThue>() {
        @Override
        public boolean areItemsTheSame(@NonNull KhachThue oldItem, @NonNull KhachThue newItem) {
            return oldItem.getId() == newItem.getId();
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull KhachThue oldItem, @NonNull KhachThue newItem) {
            return oldItem.getHoTen().equals(newItem.getHoTen()) &&
                   oldItem.isDangThue() == newItem.isDangThue() &&
                   (oldItem.getPhongId() == null ? newItem.getPhongId() == null : 
                    oldItem.getPhongId().equals(newItem.getPhongId()));
        }
    };
    
    @NonNull
    @Override
    public KhachThueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_khach_thue, parent, false);
        return new KhachThueViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull KhachThueViewHolder holder, int position) {
        KhachThue khachThue = getItem(position);
        holder.bind(khachThue);
    }
    
    class KhachThueViewHolder extends RecyclerView.ViewHolder {
        
        private final MaterialCardView cardView;
        private final TextView tvAvatar;
        private final TextView tvHoTen;
        private final TextView tvPhong;
        private final TextView tvSdt;
        private final TextView tvTrangThai;
        private final TextView tvContractStatus;
        
        public KhachThueViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvHoTen = itemView.findViewById(R.id.tv_ho_ten);
            tvPhong = itemView.findViewById(R.id.tv_phong);
            tvSdt = itemView.findViewById(R.id.tv_sdt);
            tvTrangThai = itemView.findViewById(R.id.tv_trang_thai);
            tvContractStatus = itemView.findViewById(R.id.tv_contract_status);
        }
        
        public void bind(KhachThue khachThue) {
            Context context = itemView.getContext();
            
            // Họ tên
            tvHoTen.setText(khachThue.getHoTen());
            
            // Avatar
            String hoTen = khachThue.getHoTen();
            if (hoTen != null && !hoTen.isEmpty()) {
                String[] parts = hoTen.trim().split("\\s+");
                String initials;
                if (parts.length >= 2) {
                    initials = parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1);
                } else {
                    initials = parts[0].substring(0, Math.min(2, parts[0].length()));
                }
                tvAvatar.setText(initials.toUpperCase());
            }
            
            // Phòng
            if (khachThue.getPhongId() != null && phongMap.containsKey(khachThue.getPhongId())) {
                tvPhong.setText("Phòng " + phongMap.get(khachThue.getPhongId()));
                tvPhong.setVisibility(View.VISIBLE);
            } else {
                tvPhong.setText("Chưa có phòng");
                tvPhong.setVisibility(View.VISIBLE);
            }
            
            // SĐT
            tvSdt.setText(khachThue.getSoDienThoai() != null ? khachThue.getSoDienThoai() : "---");
            
            // Trạng thái khách
            if (khachThue.isDangThue()) {
                tvTrangThai.setText("Đang thuê");
                tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_occupied));
                tvTrangThai.getBackground().setTint(ContextCompat.getColor(context, R.color.status_occupied_bg));
            } else {
                tvTrangThai.setText("Đã trả");
                tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.gray));
                tvTrangThai.getBackground().setTint(ContextCompat.getColor(context, R.color.gray_light));
            }

            // Trạng thái hợp đồng
            String contractStatus = contractStatusMap.get(khachThue.getId());       
            if (contractStatus != null) { 
                tvContractStatus.setVisibility(View.VISIBLE);
                tvContractStatus.setText(contractStatus);
                if (HopDong.TRANG_THAI_WAITING_FOR_TENANT.equals(contractStatus)) { 
                    tvContractStatus.setTextColor(ContextCompat.getColor(context, R.color.status_maintenance));
                    tvContractStatus.getBackground().setTint(ContextCompat.getColor(context, R.color.status_maintenance_bg));
                } else if (HopDong.TRANG_THAI_EXPIRED.equals(contractStatus)) {
                    tvContractStatus.setTextColor(ContextCompat.getColor(context, R.color.unpaid));
                    tvContractStatus.getBackground().setTint(ContextCompat.getColor(context, R.color.status_unpaid_bg));
                } else if (HopDong.TRANG_THAI_CANCELLED.equals(contractStatus)) {
                    tvContractStatus.setTextColor(ContextCompat.getColor(context, R.color.gray));
                    tvContractStatus.getBackground().setTint(ContextCompat.getColor(context, R.color.gray_light));
                } else {
                    // Active
                    tvContractStatus.setTextColor(ContextCompat.getColor(context, R.color.status_available));
                    tvContractStatus.getBackground().setTint(ContextCompat.getColor(context, R.color.status_available_bg));
                }
            } else {
                tvContractStatus.setVisibility(View.GONE);
            }
            
            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onKhachThueClick(khachThue);
                }
            });
            
            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onKhachThueLongClick(khachThue);
                    return true;
                }
                return false;
            });
        }
    }
}
