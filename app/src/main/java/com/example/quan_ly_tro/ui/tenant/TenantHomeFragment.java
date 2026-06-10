package com.example.quan_ly_tro.ui.tenant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.adapter.ThongBaoAdapter;
import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.entity.HopDong;
import com.example.quan_ly_tro.data.database.entity.User;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.google.firebase.auth.FirebaseAuth;

public class TenantHomeFragment extends Fragment {

    private TextView tvWelcome, tvRoomName, tvRentPrice, tvStartDate;
    private RecyclerView rvAnnouncements;
    private ThongBaoAdapter adapter;
    private AppDatabase database;
    private com.google.firebase.firestore.ListenerRegistration announcementListener;
    private com.google.firebase.firestore.ListenerRegistration contractListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tenant_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        database = AppDatabase.getDatabase(requireContext());
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvRoomName = view.findViewById(R.id.tv_room_name);
        tvRentPrice = view.findViewById(R.id.tv_rent_price);
        tvStartDate = view.findViewById(R.id.tv_start_date);
        rvAnnouncements = view.findViewById(R.id.rv_announcements);
        
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), com.example.quan_ly_tro.ui.auth.LoginActivity.class));
            getActivity().finish();
        });
        
        setupRecyclerView();
        loadTenantData();
        loadAnnouncements();
        checkPendingContracts();
    }

    private void setupRealtimeContracts() {
        contractListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("shared_hop_dong")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (com.google.firebase.firestore.DocumentChange dc : value.getDocumentChanges()) {
                                HopDong hd = dc.getDocument().toObject(HopDong.class);
                                if (hd != null) {
                                    if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                                        dc.getType() == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                                        database.hopDongDao().insert(hd);
                                    }
                                }
                            }
                        });
                    }
                });
    }

    private void setupRealtimeAnnouncements() {
        announcementListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("shared_thong_bao")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (com.google.firebase.firestore.DocumentChange dc : value.getDocumentChanges()) {
                                com.example.quan_ly_tro.data.database.entity.ThongBao tb = 
                                        dc.getDocument().toObject(com.example.quan_ly_tro.data.database.entity.ThongBao.class);
                                if (tb != null) {
                                    if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                                        dc.getType() == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                                        database.thongBaoDao().insert(tb);
                                    }
                                }
                            }
                        });
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (announcementListener != null) announcementListener.remove();
        if (contractListener != null) contractListener.remove();
    }

    private void setupRecyclerView() {
        adapter = new ThongBaoAdapter();
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAnnouncements.setAdapter(adapter);

        adapter.setOnThongBaoClickListener(new ThongBaoAdapter.OnThongBaoClickListener() {
            @Override
            public void onThongBaoClick(com.example.quan_ly_tro.data.database.entity.ThongBao thongBao) {
                // Đánh dấu đã đọc
                if (!thongBao.isRead()) {
                    thongBao.setRead(true);
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.thongBaoDao().update(thongBao);
                    });
                }
                
                // Hiện nội dung chi tiết
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle(thongBao.getTieuDe())
                        .setMessage(thongBao.getNoiDung())
                        .setPositiveButton("Đóng", null)
                        .show();
            }

            @Override
            public void onThongBaoLongClick(com.example.quan_ly_tro.data.database.entity.ThongBao thongBao) {
                // Xóa (ẩn) thông báo
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xóa thông báo")
                        .setMessage("Bạn có muốn ẩn thông báo này khỏi danh sách không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            AppDatabase.databaseWriteExecutor.execute(() -> {
                                // Xóa trên Cloud trước khi xóa local
                                com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = 
                                        new com.example.quan_ly_tro.sync.FirebaseSyncManager(requireContext());
                                syncManager.deleteThongBaoFromCloud(thongBao);
                                
                                database.thongBaoDao().delete(thongBao);
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
    }

    private void loadAnnouncements() {
        database.thongBaoDao().getAllThongBao().observe(getViewLifecycleOwner(), list -> {
            if (list != null) adapter.setThongBaoList(list);
        });
    }

    private void loadTenantData() {
        String uid = FirebaseAuth.getInstance().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (uid == null || getContext() == null) return;

        android.content.Context appContext = getContext().getApplicationContext();

        // 1. Luôn thực hiện PULL từ Cloud trước để đảm bảo có "Cầu nối" mới nhất
        com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = 
                new com.example.quan_ly_tro.sync.FirebaseSyncManager(appContext);
        
        syncManager.pullFromCloud(new com.example.quan_ly_tro.sync.FirebaseSyncManager.OnSyncCallback() {
            @Override
            public void onSuccess(String message) {
                // Sau khi Pull xong, mới hiển thị dữ liệu lên UI
                refreshUI(uid, email);
            }

            @Override
            public void onError(String error) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            android.widget.Toast.makeText(appContext, error, android.widget.Toast.LENGTH_LONG).show();
                            refreshUI(uid, email);
                        }
                    });
                }
            }
        });
    }

    private void refreshUI(String uid, String email) {
        if (!isAdded()) return;
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                User user = database.userDao().getUserByUidSync(uid);
                
                // Nếu user chưa có phongId (vừa đăng ký xong), thử tìm trong bảng KhachThue
                if (user != null && user.getPhongId() == null && email != null) {
                    com.example.quan_ly_tro.data.database.entity.KhachThue kt = 
                            database.khachThueDao().getKhachThueByEmailSync(email);
                    if (kt != null) {
                        user.setPhongId(kt.getPhongId());
                        user.setHoTen(kt.getHoTen());
                        database.userDao().update(user);
                    }
                }

                if (user != null) {
                    final String name = user.getHoTen();
                    final Integer pId = user.getPhongId();
                    
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                tvWelcome.setText("Xin chào, " + (name != null ? name : "Khách thuê") + "!");
                                if (pId != null) {
                                    loadRoomDetails(pId);
                                } else {
                                    tvRoomName.setText("Đang chờ gán phòng...");
                                }
                                
                                // CHỈ BẬT REALTIME SAU KHI ĐÃ ĐỒNG BỘ XONG DỮ LIỆU CƠ BẢN
                                setupRealtimeAnnouncements();
                                setupRealtimeContracts();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadRoomDetails(int phongId) {
        if (getView() == null) return;
        
        database.phongDao().getPhongById(phongId).observe(getViewLifecycleOwner(), phong -> {
            if (phong != null) {
                tvRoomName.setText("Phòng: " + phong.getSoPhong());
                tvRentPrice.setText(FormatUtils.formatCurrency(phong.getGiaThue()) + "/tháng");
            }
        });
        
        // Lấy ngày vào ở từ hồ sơ Khách Thuê (chính xác hơn) thay vì lấy từ hợp đồng
        String email = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (email != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                com.example.quan_ly_tro.data.database.entity.KhachThue kt = database.khachThueDao().getKhachThueByEmailSync(email);
                if (kt != null && isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            tvStartDate.setText(FormatUtils.formatDate(kt.getNgayVao()));
                        }
                    });
                }
            });
        }
    }

    private void checkPendingContracts() {
        if (getView() == null) return;
        
        database.hopDongDao().getAllHopDong().observe(getViewLifecycleOwner(), list -> {
            boolean hasPending = false;
            if (list != null) {
                for (HopDong hd : list) {
                    if (HopDong.TRANG_THAI_WAITING_FOR_TENANT.equals(hd.getTrangThai())) {
                        showPendingContractAlert(hd);
                        hasPending = true;
                        break;
                    }
                }
            }
            
            if (!hasPending) {
                View view = getView();
                if (view != null) {
                    com.google.android.material.card.MaterialCardView card = view.findViewById(R.id.card_notification);
                    if (card != null) card.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showPendingContractAlert(HopDong hd) {
        View view = getView();
        if (view == null) return;
        
        com.google.android.material.card.MaterialCardView card = view.findViewById(R.id.card_notification);
        TextView tvContent = view.findViewById(R.id.tv_notif_content);
        
        card.setVisibility(View.VISIBLE);
        tvContent.setText("Bạn có 1 hợp đồng chờ ký. Bấm để xem và ký ngay!");
        
        card.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.quan_ly_tro.ui.khachthue.TaoHopDongActivity.class);
            intent.putExtra("hop_dong_id", hd.getId());
            intent.putExtra("is_tenant_signing", true);
            startActivity(intent);
        });
    }
}
