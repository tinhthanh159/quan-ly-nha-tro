package com.example.quan_ly_tro.ui.thuchi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.adapter.ThuChiAdapter;
import com.example.quan_ly_tro.data.database.entity.ThuChi;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

/**
 * Fragment hiển thị danh sách thu/chi
 */
public class ThuChiFragment extends Fragment implements ThuChiAdapter.OnThuChiClickListener {
    
    private ThuChiViewModel viewModel;
    private ThuChiAdapter adapter;
    
    private RecyclerView rvThuChi;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAdd;
    private TabLayout tabLayout;
    
    private TextView tvTongThu;
    private TextView tvTongChi;
    private TextView tvSoDu;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thu_chi, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initRecyclerView();
        initViewModel();
        setupClickListeners();
        observeData();
    }
    
    private void initViews(View view) {
        rvThuChi = view.findViewById(R.id.rv_thu_chi);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        fabAdd = view.findViewById(R.id.fab_add);
        tabLayout = view.findViewById(R.id.tab_layout);
        
        tvTongThu = view.findViewById(R.id.tv_tong_thu);
        tvTongChi = view.findViewById(R.id.tv_tong_chi);
        tvSoDu = view.findViewById(R.id.tv_so_du);
    }
    
    private void initRecyclerView() {
        adapter = new ThuChiAdapter(this);
        rvThuChi.setLayoutManager(new LinearLayoutManager(getContext()));
        rvThuChi.setAdapter(adapter);
        
        // Apply layout animation for smooth item appearance
        rvThuChi.setLayoutAnimation(android.view.animation.AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.layout_animation_fall_down));
        
        // Animate FAB
        fabAdd.startAnimation(android.view.animation.AnimationUtils.loadAnimation(
                requireContext(), R.anim.fab_scale_up));
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(ThuChiViewModel.class);
    }
    
    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ThemThuChiActivity.class);
            startActivity(intent);
        });
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // Tất cả
                        viewModel.setFilter(null);
                        break;
                    case 1: // Thu
                        viewModel.setFilter(ThuChi.LOAI_THU);
                        break;
                    case 2: // Chi
                        viewModel.setFilter(ThuChi.LOAI_CHI);
                        break;
                }
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void observeData() {
        viewModel.getTongThu().observe(getViewLifecycleOwner(), amount -> {
            double tongThu = amount != null ? amount : 0;
            tvTongThu.setText(FormatUtils.formatCurrency(tongThu));
        });
        
        viewModel.getTongChi().observe(getViewLifecycleOwner(), amount -> {
            double tongChi = amount != null ? amount : 0;
            tvTongChi.setText(FormatUtils.formatCurrency(tongChi));
        });
        
        viewModel.getSoDu().observe(getViewLifecycleOwner(), amount -> {
            double soDu = amount != null ? amount : 0;
            tvSoDu.setText(FormatUtils.formatCurrency(soDu));
        });
        
        viewModel.getFilteredThuChi().observe(getViewLifecycleOwner(), thuChiList -> {
            if (thuChiList != null && !thuChiList.isEmpty()) {
                adapter.submitList(thuChiList);
                rvThuChi.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            } else {
                rvThuChi.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
    
    @Override
    public void onThuChiClick(ThuChi thuChi) {
        Intent intent = new Intent(getActivity(), ThemThuChiActivity.class);
        intent.putExtra("thu_chi_id", thuChi.getId());
        startActivity(intent);
    }
    
    @Override
    public void onThuChiLongClick(ThuChi thuChi) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn có chắc muốn xóa giao dịch này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.delete(thuChi);
                })
                .show();
    }
}
