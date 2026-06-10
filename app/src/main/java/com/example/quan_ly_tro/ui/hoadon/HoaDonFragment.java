package com.example.quan_ly_tro.ui.hoadon;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.adapter.HoaDonAdapter;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.ui.khachthue.KhachThueViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment hiển thị danh sách hóa đơn
 */
public class HoaDonFragment extends Fragment implements HoaDonAdapter.OnHoaDonClickListener {
    
    private HoaDonViewModel viewModel;
    private KhachThueViewModel khachThueViewModel;
    private HoaDonAdapter adapter;
    
    private RecyclerView rvHoaDon;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAdd;
    
    private Chip chipAll;
    private Chip chipChuaThanhToan;
    private Chip chipDaThanhToan;
    
    private Map<Integer, String> phongMap = new HashMap<>();
    private Map<Integer, String> khachThueMap = new HashMap<>();
    private Integer filterPhongId = null;
    private boolean isTenantMode = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hoa_don, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initRecyclerView();
        initViewModel();
        checkUserRole(); // Check role here
        setupChipFilters();
        setupClickListeners();
        observeData();
    }

    private void checkUserRole() {
        com.google.firebase.auth.FirebaseUser fbUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            android.content.Context context = getContext();
            if (context == null) return;
            android.content.Context appContext = context.getApplicationContext();
            
            com.example.quan_ly_tro.data.database.AppDatabase.databaseWriteExecutor.execute(() -> {
                com.example.quan_ly_tro.data.database.entity.User user = 
                        com.example.quan_ly_tro.data.database.AppDatabase.getDatabase(appContext).userDao().getUserByUidSync(fbUser.getUid());
                if (user != null && com.example.quan_ly_tro.data.database.entity.User.ROLE_TENANT.equals(user.getRole())) {
                    isTenantMode = true;
                    filterPhongId = (user.getPhongId() != null) ? user.getPhongId() : -1; // -1 if not assigned to ensure empty list
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (fabAdd != null) fabAdd.setVisibility(View.GONE);
                            View btnAddEmpty = getView() != null ? getView().findViewById(R.id.btn_add_empty) : null;
                            if (btnAddEmpty != null) btnAddEmpty.setVisibility(View.GONE);
                            
                            // Lọc danh sách theo phòng của khách ngay lập tức
                            viewModel.setFilterPhongId(filterPhongId);
                        });
                    }
                }
            });
        }
    }
    
    private void initViews(View view) {
        rvHoaDon = view.findViewById(R.id.rv_hoa_don);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        fabAdd = view.findViewById(R.id.fab_add);
        
        // Ẩn các nút thêm mới nếu là Tenant Mode
        if (isTenantMode) {
            if (fabAdd != null) fabAdd.setVisibility(View.GONE);
            View btnAddEmpty = view.findViewById(R.id.btn_add_empty);
            if (btnAddEmpty != null) btnAddEmpty.setVisibility(View.GONE);
        }
        
        chipAll = view.findViewById(R.id.chip_all);
        chipChuaThanhToan = view.findViewById(R.id.chip_chua_thanh_toan);
        chipDaThanhToan = view.findViewById(R.id.chip_da_thanh_toan);
    }
    
    private void initRecyclerView() {
        adapter = new HoaDonAdapter(this);
        rvHoaDon.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHoaDon.setAdapter(adapter);
        
        // Apply layout animation for smooth item appearance
        rvHoaDon.setLayoutAnimation(android.view.animation.AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.layout_animation_fall_down));
        
        // Animate FAB
        fabAdd.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                requireContext(), R.anim.fab_scale_up));
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(HoaDonViewModel.class);
        khachThueViewModel = new ViewModelProvider(this).get(KhachThueViewModel.class);
    }
    
    private void setupChipFilters() {
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilter("");
        });
        
        chipChuaThanhToan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilter(HoaDon.TRANG_THAI_CHUA_THANH_TOAN);
        });
        
        chipDaThanhToan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilter(HoaDon.TRANG_THAI_DA_THANH_TOAN);
        });
    }
    
    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> openTaoHoaDon());
        
        View btnAddEmpty = getView().findViewById(R.id.btn_add_empty);
        if (btnAddEmpty != null) {
            btnAddEmpty.setOnClickListener(v -> openTaoHoaDon());
        }
    }
    
    private void openTaoHoaDon() {
        Intent intent = new Intent(getActivity(), TaoHoaDonActivity.class);
        startActivity(intent);
    }
    
    private void observeData() {
        // Load phòng map
        viewModel.getAllPhong().observe(getViewLifecycleOwner(), phongList -> {
            if (phongList != null) {
                phongMap.clear();
                for (Phong phong : phongList) {
                    phongMap.put(phong.getId(), phong.getSoPhong());
                }
                adapter.setPhongMap(phongMap);
            }
        });
        
        // Load khách thuê map
        khachThueViewModel.getAllKhachThue().observe(getViewLifecycleOwner(), khachThueList -> {
            if (khachThueList != null) {
                khachThueMap.clear();
                for (KhachThue khach : khachThueList) {
                    khachThueMap.put(khach.getId(), khach.getHoTen());
                }
                adapter.setKhachThueMap(khachThueMap);
            }
        });
        
        // Load hóa đơn
        viewModel.getFilteredHoaDon().observe(getViewLifecycleOwner(), hoaDonList -> {
            if (hoaDonList != null) {
                java.util.List<HoaDon> finalItems = new java.util.ArrayList<>();
                if (filterPhongId != null) {
                    for (HoaDon hd : hoaDonList) {
                        if (hd.getPhongId() == filterPhongId) finalItems.add(hd);
                    }
                } else {
                    finalItems.addAll(hoaDonList);
                }

                if (!finalItems.isEmpty()) {
                    adapter.submitList(finalItems);
                    rvHoaDon.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                } else {
                    rvHoaDon.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    
    @Override
    public void onHoaDonClick(HoaDon hoaDon) {
        // Khi click vào hóa đơn, hiển thị menu hành động
        showActionMenu(hoaDon);
    }
    
    @Override
    public void onHoaDonLongClick(HoaDon hoaDon) {
        showActionMenu(hoaDon);
    }
    
    private void showActionMenu(HoaDon hoaDon) {
        if (isTenantMode) {
            // Khách thuê chỉ có quyền xem và share VietQR
            String[] options = {"Gửi Zalo & VietQR"};
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Hóa đơn tháng " + hoaDon.getThangNam())
                    .setItems(options, (dialog, which) -> shareHoaDonZalo(hoaDon))
                    .show();
            return;
        }

        String[] options;
        if (HoaDon.TRANG_THAI_CHUA_THANH_TOAN.equals(hoaDon.getTrangThai())) {
            // Hóa đơn CHƯA thanh toán: Hiện đầy đủ
            options = new String[]{"Thanh toán", "Gửi Zalo & VietQR", "Xóa"};
        } else {
            // Hóa đơn ĐÃ thanh toán: Không hiện Gửi Zalo/VietQR
            options = new String[]{"Xóa"};
        }
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Hóa đơn tháng " + hoaDon.getThangNam())
                .setItems(options, (dialog, which) -> {
                    if (HoaDon.TRANG_THAI_CHUA_THANH_TOAN.equals(hoaDon.getTrangThai())) {
                        if (which == 0) showThanhToanDialog(hoaDon);
                        else if (which == 1) shareHoaDonZalo(hoaDon);
                        else if (which == 2) showDeleteDialog(hoaDon);
                    } else {
                        if (which == 0) showDeleteDialog(hoaDon);
                    }
                })
                .show();
    }
    
    private void shareHoaDonZalo(HoaDon hoaDon) {
        Phong phong = null;
        for (Map.Entry<Integer, String> entry : phongMap.entrySet()) {
            if (entry.getKey().equals(hoaDon.getPhongId())) {
                phong = new Phong();
                phong.setId(entry.getKey());
                phong.setSoPhong(entry.getValue());
                break;
            }
        }
        
        // Show VietQR dialog first, then user can share
        com.example.quan_ly_tro.utils.ShareUtils.showVietQRDialog(requireContext(), hoaDon, phong);
    }
    
    private void showThanhToanDialog(HoaDon hoaDon) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xác nhận thanh toán")
                .setMessage("Xác nhận đã thu tiền hóa đơn này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    viewModel.thanhToan(hoaDon);
                })
                .show();
    }
    
    private void showDeleteDialog(HoaDon hoaDon) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa hóa đơn")
                .setMessage("Bạn có chắc muốn xóa hóa đơn này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.delete(hoaDon);
                })
                .show();
    }
}
