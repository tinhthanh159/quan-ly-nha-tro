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
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter cho danh sách hóa đơn
 */
public class HoaDonAdapter extends ListAdapter<HoaDon, HoaDonAdapter.HoaDonViewHolder> {
    
    private final OnHoaDonClickListener listener;
    private Map<Integer, String> phongMap = new HashMap<>();
    private Map<Integer, String> khachThueMap = new HashMap<>();
    
    public interface OnHoaDonClickListener {
        void onHoaDonClick(HoaDon hoaDon);
        void onHoaDonLongClick(HoaDon hoaDon);
    }
    
    public HoaDonAdapter(OnHoaDonClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }
    
    public void setPhongMap(Map<Integer, String> phongMap) {
        this.phongMap = phongMap;
        notifyDataSetChanged();
    }
    
    public void setKhachThueMap(Map<Integer, String> khachThueMap) {
        this.khachThueMap = khachThueMap;
        notifyDataSetChanged();
    }
    
    private static final DiffUtil.ItemCallback<HoaDon> DIFF_CALLBACK = new DiffUtil.ItemCallback<HoaDon>() {
        @Override
        public boolean areItemsTheSame(@NonNull HoaDon oldItem, @NonNull HoaDon newItem) {
            return oldItem.getId() == newItem.getId();
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull HoaDon oldItem, @NonNull HoaDon newItem) {
            if (oldItem.getTongTien() != newItem.getTongTien()) {
                return false;
            }
            // Handle null trangThai safely
            String oldTrangThai = oldItem.getTrangThai();
            String newTrangThai = newItem.getTrangThai();
            if (oldTrangThai == null && newTrangThai == null) {
                return true;
            }
            if (oldTrangThai == null || newTrangThai == null) {
                return false;
            }
            return oldTrangThai.equals(newTrangThai);
        }
    };
    
    @NonNull
    @Override
    public HoaDonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hoa_don, parent, false);
        return new HoaDonViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HoaDonViewHolder holder, int position) {
        HoaDon hoaDon = getItem(position);
        holder.bind(hoaDon);
    }
    
    class HoaDonViewHolder extends RecyclerView.ViewHolder {
        
        private final MaterialCardView cardView;
        private final TextView tvPhong;
        private final TextView tvThang;
        private final TextView tvTrangThai;
        private final TextView tvTongTien;
        private final TextView tvKhachThue;
        
        public HoaDonViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvPhong = itemView.findViewById(R.id.tv_phong);
            tvThang = itemView.findViewById(R.id.tv_thang);
            tvTrangThai = itemView.findViewById(R.id.tv_trang_thai);
            tvTongTien = itemView.findViewById(R.id.tv_tong_tien);
            tvKhachThue = itemView.findViewById(R.id.tv_khach_thue);
        }
        
        public void bind(HoaDon hoaDon) {
            Context context = itemView.getContext();
            
            // Phòng
            if (phongMap.containsKey(hoaDon.getPhongId())) {
                tvPhong.setText("Phòng " + phongMap.get(hoaDon.getPhongId()));
            } else {
                tvPhong.setText("Phòng --");
            }
            
            // Tháng
            tvThang.setText("Tháng " + hoaDon.getThangNam());
            
            // Tổng tiền
            tvTongTien.setText(FormatUtils.formatCurrency(hoaDon.getTongTien()));
            
            // Khách thuê
            if (hoaDon.getKhachThueId() != null && khachThueMap.containsKey(hoaDon.getKhachThueId())) {
                tvKhachThue.setText("Khách: " + khachThueMap.get(hoaDon.getKhachThueId()));
                tvKhachThue.setVisibility(View.VISIBLE);
            } else {
                tvKhachThue.setVisibility(View.GONE);
            }
            
            // Trạng thái
            if (HoaDon.TRANG_THAI_DA_THANH_TOAN.equals(hoaDon.getTrangThai())) {
                tvTrangThai.setText("Đã thanh toán");
                tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_paid));
                tvTrangThai.getBackground().setTint(ContextCompat.getColor(context, R.color.status_paid_bg));
            } else {
                tvTrangThai.setText("Chưa thanh toán");
                tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_unpaid));
                tvTrangThai.getBackground().setTint(ContextCompat.getColor(context, R.color.status_unpaid_bg));
            }
            
            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHoaDonClick(hoaDon);
                }
            });
            
            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onHoaDonLongClick(hoaDon);
                    return true;
                }
                return false;
            });
        }
    }
}
