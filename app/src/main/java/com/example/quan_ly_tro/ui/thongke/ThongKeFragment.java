package com.example.quan_ly_tro.ui.thongke;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.example.quan_ly_tro.utils.PdfExportUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment hiển thị thống kê với chức năng xuất PDF
 */
public class ThongKeFragment extends Fragment {
    
    private ThongKeViewModel viewModel;
    
    private TextView tvThang, tvDoanhThuThang;
    private TextView tvTongPhong, tvPhongThue;
    private TextView tvThu, tvChi, tvLoiNhuan;
    private TextView tvHoaDonDaThu, tvHoaDonChuaThu, tvTienChuaThu;
    private TextView tvOccupancy;
    private LinearProgressIndicator progressOccupancy;
    private ImageButton btnPrevMonth, btnNextMonth;
    private MaterialButton btnExport;
    
    // Charts
    private LineChart lineChartRevenue;
    private PieChart pieChartRooms;
    
    private int totalPhong = 0;
    private int phongDangThue = 0;
    
    // Data for export
    private List<Phong> cachedPhongList = new ArrayList<>();
    private List<KhachThue> cachedKhachThueList = new ArrayList<>();
    private List<HoaDon> cachedHoaDonList = new ArrayList<>();
    private double cachedTongThu = 0;
    private double cachedTongChi = 0;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_ke, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initViewModel();
        setupCharts();
        setupClickListeners();
        observeData();
    }
    
    private void initViews(View view) {
        tvThang = view.findViewById(R.id.tv_thang);
        tvDoanhThuThang = view.findViewById(R.id.tv_doanh_thu_thang);
        
        tvTongPhong = view.findViewById(R.id.tv_tong_phong);
        tvPhongThue = view.findViewById(R.id.tv_phong_thue);
        
        tvThu = view.findViewById(R.id.tv_thu);
        tvChi = view.findViewById(R.id.tv_chi);
        tvLoiNhuan = view.findViewById(R.id.tv_loi_nhuan);
        
        tvHoaDonDaThu = view.findViewById(R.id.tv_hoa_don_da_thu);
        tvHoaDonChuaThu = view.findViewById(R.id.tv_hoa_don_chua_thu);
        tvTienChuaThu = view.findViewById(R.id.tv_tien_chua_thu);
        
        tvOccupancy = view.findViewById(R.id.tv_occupancy);
        progressOccupancy = view.findViewById(R.id.progress_occupancy);
        
        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);
        btnExport = view.findViewById(R.id.btn_export);
        
        // Charts
        lineChartRevenue = view.findViewById(R.id.line_chart_revenue);
        pieChartRooms = view.findViewById(R.id.pie_chart_rooms);
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(ThongKeViewModel.class);
    }
    
    private void setupClickListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            viewModel.previousMonth();
            updateMonthDisplay();
        });
        
        btnNextMonth.setOnClickListener(v -> {
            viewModel.nextMonth();
            updateMonthDisplay();
        });
        
        // Export PDF button
        btnExport.setOnClickListener(v -> exportPdf());
    }
    
    private void exportPdf() {
        PdfExportUtils.exportBaoCaoTongHop(
                requireContext(),
                cachedPhongList,
                cachedKhachThueList,
                cachedHoaDonList,
                cachedTongThu,
                cachedTongChi
        );
    }
    
    private void updateMonthDisplay() {
        tvThang.setText(viewModel.getFormattedMonth());
        loadDoanhThuThang();
    }
    
    private void loadDoanhThuThang() {
        String thangNam = viewModel.getThangNamKey();
        viewModel.getDoanhThuThang(thangNam).observe(getViewLifecycleOwner(), doanhThu -> {
            double value = doanhThu != null ? doanhThu : 0;
            tvDoanhThuThang.setText(FormatUtils.formatCurrency(value));
        });
    }
    
    private void observeData() {
        // Month display
        viewModel.getSelectedMonth().observe(getViewLifecycleOwner(), cal -> {
            updateMonthDisplay();
        });
        
        // Tổng phòng
        viewModel.getAllPhong().observe(getViewLifecycleOwner(), phongList -> {
            if (phongList != null) {
                cachedPhongList = phongList;
                totalPhong = phongList.size();
                tvTongPhong.setText(String.valueOf(totalPhong));
                updateOccupancyRate();
                updatePieChart();
            }
        });
        
        // Phòng đang thuê
        viewModel.getSoPhongDangThue().observe(getViewLifecycleOwner(), count -> {
            phongDangThue = count != null ? count : 0;
            tvPhongThue.setText(String.valueOf(phongDangThue));
            updateOccupancyRate();
        });
        
        // Tổng thu
        viewModel.getTongThu().observe(getViewLifecycleOwner(), thu -> {
            cachedTongThu = thu != null ? thu : 0;
            tvThu.setText(FormatUtils.formatCurrency(cachedTongThu));
            updateLoiNhuan();
        });
        
        // Tổng chi
        viewModel.getTongChi().observe(getViewLifecycleOwner(), chi -> {
            cachedTongChi = chi != null ? chi : 0;
            tvChi.setText(FormatUtils.formatCurrency(cachedTongChi));
            updateLoiNhuan();
        });
        
        // Hóa đơn
        viewModel.getAllHoaDon().observe(getViewLifecycleOwner(), hoaDonList -> {
            if (hoaDonList != null) {
                cachedHoaDonList = hoaDonList;
                int daThu = 0;
                int chuaThu = 0;
                for (HoaDon hd : hoaDonList) {
                    if (HoaDon.TRANG_THAI_DA_THANH_TOAN.equals(hd.getTrangThai())) {
                        daThu++;
                    } else {
                        chuaThu++;
                    }
                }
                tvHoaDonDaThu.setText(String.valueOf(daThu));
                tvHoaDonChuaThu.setText(String.valueOf(chuaThu));
            }
        });
        
        // Tiền chưa thu
        viewModel.getTongTienChuaThu().observe(getViewLifecycleOwner(), tien -> {
            double value = tien != null ? tien : 0;
            tvTienChuaThu.setText(FormatUtils.formatCurrency(value));
        });
        
        // Load khách thuê for export
        viewModel.getAllKhachThue().observe(getViewLifecycleOwner(), khachThueList -> {
            if (khachThueList != null) {
                cachedKhachThueList = khachThueList;
            }
        });
    }
    
    private void updateOccupancyRate() {
        if (totalPhong > 0) {
            int percentage = (phongDangThue * 100) / totalPhong;
            progressOccupancy.setProgress(percentage);
            tvOccupancy.setText(String.format("%d%% (%d/%d phòng)", percentage, phongDangThue, totalPhong));
        } else {
            progressOccupancy.setProgress(0);
            tvOccupancy.setText("0% (0/0 phòng)");
        }
    }
    
    private void updateLoiNhuan() {
        double loiNhuan = cachedTongThu - cachedTongChi;
        tvLoiNhuan.setText(FormatUtils.formatCurrency(loiNhuan));
    }
    
    private void setupCharts() {
        setupLineChart();
        setupPieChart();
        loadChartData();
    }
    
    private void setupLineChart() {
        lineChartRevenue.getDescription().setEnabled(false);
        lineChartRevenue.setTouchEnabled(true);
        lineChartRevenue.setDragEnabled(true);
        lineChartRevenue.setScaleEnabled(false);
        lineChartRevenue.setPinchZoom(false);
        lineChartRevenue.setDrawGridBackground(false);
        lineChartRevenue.setExtraBottomOffset(10f);
        
        // X axis
        XAxis xAxis = lineChartRevenue.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_variant));
        
        // Y axis
        lineChartRevenue.getAxisLeft().setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_variant));
        lineChartRevenue.getAxisLeft().setDrawGridLines(true);
        lineChartRevenue.getAxisLeft().setGridColor(ContextCompat.getColor(requireContext(), R.color.divider));
        lineChartRevenue.getAxisRight().setEnabled(false);
        
        // Legend
        Legend legend = lineChartRevenue.getLegend();
        legend.setEnabled(false);
    }
    
    private void setupPieChart() {
        pieChartRooms.getDescription().setEnabled(false);
        pieChartRooms.setUsePercentValues(true);
        pieChartRooms.setDrawHoleEnabled(true);
        pieChartRooms.setHoleColor(Color.TRANSPARENT);
        pieChartRooms.setHoleRadius(50f);
        pieChartRooms.setTransparentCircleRadius(55f);
        pieChartRooms.setDrawEntryLabels(false);
        pieChartRooms.setRotationEnabled(true);
        
        // Legend
        Legend legend = pieChartRooms.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface));
    }
    
    private void loadChartData() {
        loadLineChartData();
    }
    
    private void loadLineChartData() {
        // Get last 6 months data
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yy", Locale.getDefault());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -5); // Start 5 months ago
        
        for (int i = 0; i < 6; i++) {
            String monthKey = String.format(Locale.getDefault(), "%02d/%d", 
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
            labels.add(sdf.format(cal.getTime()));
            
            final int index = i;
            viewModel.getDoanhThuThang(monthKey).observe(getViewLifecycleOwner(), revenue -> {
                float value = revenue != null ? revenue.floatValue() / 1000000f : 0f; // Convert to millions
                entries.add(new Entry(index, value));
                
                if (entries.size() == 6) {
                    updateLineChart(entries, labels);
                }
            });
            
            cal.add(Calendar.MONTH, 1);
        }
    }
    
    private void updateLineChart(List<Entry> entries, List<String> labels) {
        // Sort entries by x value
        entries.sort((e1, e2) -> Float.compare(e1.getX(), e2.getX()));
        
        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu (triệu VNĐ)");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
        dataSet.setFillAlpha(50);
        
        LineData lineData = new LineData(dataSet);
        lineChartRevenue.setData(lineData);
        lineChartRevenue.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChartRevenue.animateX(500);
        lineChartRevenue.invalidate();
    }
    
    private void updatePieChart() {
        if (totalPhong == 0) return;
        
        List<PieEntry> entries = new ArrayList<>();
        int phongTrong = 0;
        int phongSua = 0;
        
        for (Phong phong : cachedPhongList) {
            if (Phong.TRANG_THAI_TRONG.equals(phong.getTrangThai())) {
                phongTrong++;
            } else if (Phong.TRANG_THAI_DANG_SUA.equals(phong.getTrangThai())) {
                phongSua++;
            }
        }
        
        if (phongDangThue > 0) entries.add(new PieEntry(phongDangThue, "Đang thuê"));
        if (phongTrong > 0) entries.add(new PieEntry(phongTrong, "Trống"));
        if (phongSua > 0) entries.add(new PieEntry(phongSua, "Đang sửa"));
        
        if (entries.isEmpty()) return;
        
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.income),
                ContextCompat.getColor(requireContext(), R.color.warning),
                ContextCompat.getColor(requireContext(), R.color.expense)
        );
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        
        PieData data = new PieData(dataSet);
        pieChartRooms.setData(data);
        pieChartRooms.setCenterText(totalPhong + "\nphòng");
        pieChartRooms.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface));
        pieChartRooms.setCenterTextSize(14f);
        pieChartRooms.animateY(500);
        pieChartRooms.invalidate();
    }
}
