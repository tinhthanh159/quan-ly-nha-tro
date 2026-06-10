package com.example.quan_ly_tro;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.quan_ly_tro.ui.dashboard.DashboardFragment;
import com.example.quan_ly_tro.ui.hoadon.HoaDonFragment;
import com.example.quan_ly_tro.ui.khachthue.KhachThueFragment;
import com.example.quan_ly_tro.ui.phong.PhongFragment;
import com.example.quan_ly_tro.ui.thuchi.ThuChiFragment;
import com.example.quan_ly_tro.utils.NotificationHelper;
import com.example.quan_ly_tro.utils.ReminderScheduler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main Activity với Bottom Navigation
 */
public class MainActivity extends AppCompatActivity {
    
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    
    // Fragments
    private DashboardFragment dashboardFragment;
    private PhongFragment phongFragment;
    private KhachThueFragment khachThueFragment;
    private HoaDonFragment hoaDonFragment;
    private ThuChiFragment thuChiFragment;
    
    private Fragment activeFragment;
    private com.google.firebase.firestore.ListenerRegistration contractListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra login ngay lập tức
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new android.content.Intent(this, com.example.quan_ly_tro.ui.auth.LoginActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);
        
        initViews();
        initFragments();
        setupBottomNavigation();
        
        // Đồng bộ dữ liệu 2 chiều trong nền
        com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = new com.example.quan_ly_tro.sync.FirebaseSyncManager(this);
        syncManager.pullFromCloud(new com.example.quan_ly_tro.sync.FirebaseSyncManager.OnSyncCallback() {
            @Override
            public void onSuccess(String message) {
                // Sau khi tải dữ liệu về thành công, đồng bộ các thay đổi local chưa được đẩy lên
                syncManager.syncToCloud(null);
            }

            @Override
            public void onError(String error) {
                syncManager.syncToCloud(null);
            }
        });
        
        setupRealtimeContracts();
        
        // Xác nhận khwi động nhắc nhở thanh toán
        NotificationHelper.createNotificationChannel(this);
        ReminderScheduler.scheduleDailyReminder(this);
    }
    
    private void setupRealtimeContracts() {
        contractListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("shared_hop_dong")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        com.example.quan_ly_tro.data.database.AppDatabase.databaseWriteExecutor.execute(() -> {
                            com.example.quan_ly_tro.data.database.AppDatabase db = com.example.quan_ly_tro.data.database.AppDatabase.getDatabase(this);
                            for (com.google.firebase.firestore.DocumentChange dc : value.getDocumentChanges()) {
                                com.example.quan_ly_tro.data.database.entity.HopDong hd = dc.getDocument().toObject(com.example.quan_ly_tro.data.database.entity.HopDong.class);
                                if (hd != null) {
                                    if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                                        dc.getType() == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                                        db.hopDongDao().insert(hd);
                                    }
                                }
                            }
                        });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (contractListener != null) {
            contractListener.remove();
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }
    
    private void initFragments() {
        fragmentManager = getSupportFragmentManager();
        
        dashboardFragment = new DashboardFragment();
        phongFragment = new PhongFragment();
        khachThueFragment = new KhachThueFragment();
        hoaDonFragment = new HoaDonFragment();
        thuChiFragment = new ThuChiFragment();
        
        // Thêm tất cả fragments và ẩn, chỉ hiện Dashboard
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        transaction.add(R.id.fragment_container, thuChiFragment, "thu_chi").hide(thuChiFragment);
        transaction.add(R.id.fragment_container, hoaDonFragment, "hoa_don").hide(hoaDonFragment);
        transaction.add(R.id.fragment_container, khachThueFragment, "khach_thue").hide(khachThueFragment);
        transaction.add(R.id.fragment_container, phongFragment, "phong").hide(phongFragment);
        transaction.add(R.id.fragment_container, dashboardFragment, "dashboard");
        
        transaction.commit();
        
        activeFragment = dashboardFragment;
    }
    
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                selectedFragment = dashboardFragment;
            } else if (itemId == R.id.nav_phong) {
                selectedFragment = phongFragment;
            } else if (itemId == R.id.nav_khach_thue) {
                selectedFragment = khachThueFragment;
            } else if (itemId == R.id.nav_hoa_don) {
                selectedFragment = hoaDonFragment;
            } else if (itemId == R.id.nav_thu_chi) {
                selectedFragment = thuChiFragment;
            }
            
            if (selectedFragment != null && selectedFragment != activeFragment) {
                fragmentManager.beginTransaction()
                        .hide(activeFragment)
                        .show(selectedFragment)
                        .commit();
                activeFragment = selectedFragment;
            }
            
            return true;
        });
        
        // Mặc định chọn Dashboard
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
    }
}