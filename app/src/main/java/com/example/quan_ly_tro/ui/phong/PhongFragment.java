package com.example.quan_ly_tro.ui.phong;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.adapter.PhongAdapter;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment hiển thị danh sách phòng với chức năng tìm kiếm và lọc
 */
public class PhongFragment extends Fragment implements PhongAdapter.OnPhongClickListener {
    
    private PhongViewModel viewModel;
    private PhongAdapter adapter;
    
    private RecyclerView rvPhong;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAdd;
    
    private Chip chipAll;
    private Chip chipTrong;
    private Chip chipDangThue;
    private Chip chipDangSua;
    
    // Search views
    private ImageButton btnSearch;
    private TextInputLayout searchLayout;
    private TextInputEditText edtSearch;
    private boolean isSearchVisible = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_phong, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initRecyclerView();
        initViewModel();
        setupChipFilters();
        setupClickListeners();
        setupSearch();
        observeData();
    }
    
    private void initViews(View view) {
        rvPhong = view.findViewById(R.id.rv_phong);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        fabAdd = view.findViewById(R.id.fab_add);
        
        chipAll = view.findViewById(R.id.chip_all);
        chipTrong = view.findViewById(R.id.chip_trong);
        chipDangThue = view.findViewById(R.id.chip_dang_thue);
        chipDangSua = view.findViewById(R.id.chip_dang_sua);
        
        // Search views
        btnSearch = view.findViewById(R.id.btn_search);
        searchLayout = view.findViewById(R.id.search_layout);
        edtSearch = view.findViewById(R.id.edt_search);
    }
    
    private void initRecyclerView() {
        adapter = new PhongAdapter(this);
        rvPhong.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPhong.setAdapter(adapter);
        
        // Apply layout animation for smooth item appearance
        rvPhong.setLayoutAnimation(android.view.animation.AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.layout_animation_fall_down));
        
        // Animate FAB
        fabAdd.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                requireContext(), R.anim.fab_scale_up));
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(PhongViewModel.class);
    }
    
    private void setupChipFilters() {
        chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilter("");
        });
        
        chipTrong.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilter(Phong.TRANG_THAI_TRONG);
        });
        
        chipDangThue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilter(Phong.TRANG_THAI_DANG_THUE);
        });
        
        chipDangSua.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) viewModel.setFilter(Phong.TRANG_THAI_DANG_SUA);
        });
    }
    
    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> openThemPhong());
        
        View btnAddEmpty = getView().findViewById(R.id.btn_add_empty);
        if (btnAddEmpty != null) {
            btnAddEmpty.setOnClickListener(v -> openThemPhong());
        }
    }
    
    private void setupSearch() {
        // Toggle search bar visibility
        btnSearch.setOnClickListener(v -> toggleSearch());
        
        // Text change listener for search
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setSearchQuery(s.toString());
            }
        });
    }
    
    private void toggleSearch() {
        isSearchVisible = !isSearchVisible;
        if (isSearchVisible) {
            searchLayout.setVisibility(View.VISIBLE);
            edtSearch.requestFocus();
        } else {
            searchLayout.setVisibility(View.GONE);
            edtSearch.setText("");
            viewModel.setSearchQuery("");
        }
    }
    
    private void openThemPhong() {
        Intent intent = new Intent(getActivity(), ThemPhongActivity.class);
        startActivity(intent);
    }
    
    private void observeData() {
        viewModel.getFilteredPhong().observe(getViewLifecycleOwner(), phongList -> {
            if (phongList != null && !phongList.isEmpty()) {
                adapter.submitList(phongList);
                rvPhong.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            } else {
                rvPhong.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
    
    @Override
    public void onPhongClick(Phong phong) {
        // Mở màn hình chi tiết/sửa phòng
        Intent intent = new Intent(getActivity(), ThemPhongActivity.class);
        intent.putExtra("phong_id", phong.getId());
        startActivity(intent);
    }
    
    @Override
    public void onPhongLongClick(Phong phong) {
        // Hiển thị dialog xác nhận xóa
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa phòng")
                .setMessage("Bạn có chắc muốn xóa phòng " + phong.getSoPhong() + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.delete(phong);
                })
                .show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data khi quay lại fragment
    }
}
