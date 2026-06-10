package com.example.quan_ly_tro.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.ui.phong.ThemPhongActivity;
import com.example.quan_ly_tro.ui.khachthue.ThemKhachThueActivity;
import com.example.quan_ly_tro.ui.hoadon.TaoHoaDonActivity;
import com.example.quan_ly_tro.ui.thongke.ThongKeActivity;
import com.example.quan_ly_tro.ui.settings.SettingsActivity;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.google.android.material.card.MaterialCardView;

/**
 * Fragment hiển thị Dashboard tổng quan
 */
public class DashboardFragment extends Fragment {
    
    private DashboardViewModel viewModel;
    
    private TextView tvTongPhong;
    private TextView tvPhongTrong;
    private TextView tvDangThue;
    private TextView tvHoaDonChuaThu;
    private TextView tvDoanhThu;
    private TextView tvDaThu;
    private TextView tvConNo;
    private TextView tvThangHienTai;
    
    private View btnThemPhong;
    private View btnThemKhach;
    private View btnTaoHoaDon;
    private View btnPostNotif;
    private View btnManageIssues;
    private View btnViewStats;
    private MaterialCardView cardDoanhThu;
    private MaterialCardView btnSettings;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initViewModel();
        setupClickListeners();
        observeData();
    }
    
    private void initViews(View view) {
        tvTongPhong = view.findViewById(R.id.tv_tong_phong);
        tvPhongTrong = view.findViewById(R.id.tv_phong_trong);
        tvDangThue = view.findViewById(R.id.tv_dang_thue);
        tvHoaDonChuaThu = view.findViewById(R.id.tv_hoa_don_chua_thu);
        tvDoanhThu = view.findViewById(R.id.tv_doanh_thu);
        tvDaThu = view.findViewById(R.id.tv_da_thu);
        tvConNo = view.findViewById(R.id.tv_con_no);
        tvThangHienTai = view.findViewById(R.id.tv_thang_hien_tai);
        
        btnThemPhong = view.findViewById(R.id.btn_them_phong);
        btnThemKhach = view.findViewById(R.id.btn_them_khach);
        btnTaoHoaDon = view.findViewById(R.id.btn_tao_hoa_don);
        btnPostNotif = view.findViewById(R.id.btn_post_notif);
        btnManageIssues = view.findViewById(R.id.btn_manage_issues);
        btnViewStats = view.findViewById(R.id.btn_view_stats);
        cardDoanhThu = view.findViewById(R.id.card_doanh_thu);
        btnSettings = view.findViewById(R.id.btn_settings);
        
        // Set current month
        tvThangHienTai.setText(FormatUtils.getCurrentMonthText());
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    }
    
    private void setupClickListeners() {
        btnThemPhong.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ThemPhongActivity.class);
            startActivity(intent);
        });
        
        btnThemKhach.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ThemKhachThueActivity.class);
            startActivity(intent);
        });
        
        btnTaoHoaDon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TaoHoaDonActivity.class);
            startActivity(intent);
        });

        btnPostNotif.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PostAnnouncementActivity.class);
            startActivity(intent);
        });

        btnManageIssues.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ManageIssuesActivity.class);
            startActivity(intent);
        });

        btnViewStats.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ThongKeActivity.class);
            startActivity(intent);
        });
        
        // Click vào card doanh thu để xem thống kê chi tiết
        cardDoanhThu.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ThongKeActivity.class);
            startActivity(intent);
        });
        
        // Settings button
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });
    }
    
    private void observeData() {
        viewModel.getTongSoPhong().observe(getViewLifecycleOwner(), count -> {
            tvTongPhong.setText(String.valueOf(count != null ? count : 0));
        });
        
        viewModel.getSoPhongTrong().observe(getViewLifecycleOwner(), count -> {
            tvPhongTrong.setText(String.valueOf(count != null ? count : 0));
        });
        
        viewModel.getSoPhongDangThue().observe(getViewLifecycleOwner(), count -> {
            tvDangThue.setText(String.valueOf(count != null ? count : 0));
        });
        
        viewModel.getSoHoaDonChuaThanhToan().observe(getViewLifecycleOwner(), count -> {
            tvHoaDonChuaThu.setText(String.valueOf(count != null ? count : 0));
        });
        
        viewModel.getDoanhThuThang().observe(getViewLifecycleOwner(), amount -> {
            updateTotalRevenue();
        });

        viewModel.getTongTienChuaThu().observe(getViewLifecycleOwner(), amount -> {
            updateTotalRevenue();
        });
    }

    private void updateTotalRevenue() {
        Double daThu = viewModel.getDoanhThuThang().getValue();
        Double chuaThu = viewModel.getTongTienChuaThu().getValue();
        double dThu = daThu != null ? daThu : 0;
        double cThu = chuaThu != null ? chuaThu : 0;
        tvDaThu.setText(FormatUtils.formatCurrency(dThu));
        tvConNo.setText(FormatUtils.formatCurrency(cThu));
        tvDoanhThu.setText(FormatUtils.formatCurrency(dThu + cThu));
    }
}
