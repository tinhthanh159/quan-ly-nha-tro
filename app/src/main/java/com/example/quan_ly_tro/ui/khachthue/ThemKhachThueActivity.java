package com.example.quan_ly_tro.ui.khachthue;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity thêm/sửa khách thuê
 */
public class ThemKhachThueActivity extends AppCompatActivity {
    
    private KhachThueViewModel viewModel;
    
    private TextView tvTitle;
    private ImageButton btnBack;
    private TextInputLayout tilPhong, tilHoTen, tilCccd, tilSdt, tilEmail;
    private TextInputLayout tilQueQuan, tilNgheNghiep, tilNgayVao, tilGhiChu, tilPassword;
    private AutoCompleteTextView dropdownPhong;
    private TextInputEditText edtHoTen, edtCccd, edtSdt, edtEmail, edtPassword;
    private TextInputEditText edtQueQuan, edtNgheNghiep, edtNgayVao, edtGhiChu;
    private MaterialButton btnHuy, btnLuu;
    
    private int khachThueId = -1;
    private KhachThue currentKhachThue;
    
    private List<Phong> phongList = new ArrayList<>();
    private Map<String, Integer> phongNameToIdMap = new HashMap<>();
    private long selectedNgayVao = System.currentTimeMillis();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_khach_thue);
        
        initViews();
        initViewModel();
        setupClickListeners();
        loadPhongDropdown();
        
        // Kiểm tra có phải sửa không
        khachThueId = getIntent().getIntExtra("khach_thue_id", -1);
        if (khachThueId != -1) {
            tvTitle.setText(R.string.khach_sua);
            tilPassword.setVisibility(android.view.View.GONE);
            loadKhachThueData();
        } else {
            // Set ngày vào mặc định là hôm nay
            edtNgayVao.setText(FormatUtils.formatDate(System.currentTimeMillis()));
        }
    }
    
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        btnBack = findViewById(R.id.btn_back);
        
        tilPhong = findViewById(R.id.til_phong);
        tilHoTen = findViewById(R.id.til_ho_ten);
        tilCccd = findViewById(R.id.til_cccd);
        tilSdt = findViewById(R.id.til_sdt);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilQueQuan = findViewById(R.id.til_que_quan);
        tilNgheNghiep = findViewById(R.id.til_nghe_nghiep);
        tilNgayVao = findViewById(R.id.til_ngay_vao);
        tilGhiChu = findViewById(R.id.til_ghi_chu);
        
        dropdownPhong = findViewById(R.id.dropdown_phong);
        edtHoTen = findViewById(R.id.edt_ho_ten);
        edtCccd = findViewById(R.id.edt_cccd);
        edtSdt = findViewById(R.id.edt_sdt);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtQueQuan = findViewById(R.id.edt_que_quan);
        edtNgheNghiep = findViewById(R.id.edt_nghe_nghiep);
        edtNgayVao = findViewById(R.id.edt_ngay_vao);
        edtGhiChu = findViewById(R.id.edt_ghi_chu);
        
        btnHuy = findViewById(R.id.btn_huy);
        btnLuu = findViewById(R.id.btn_luu);
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(KhachThueViewModel.class);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHuy.setOnClickListener(v -> finish());
        btnLuu.setOnClickListener(v -> saveKhachThue());
        
        // Date picker cho ngày vào
        edtNgayVao.setOnClickListener(v -> showDatePicker());
        tilNgayVao.setEndIconOnClickListener(v -> showDatePicker());
        
        // Đảm bảo dropdown hiện ra khi click vào
        dropdownPhong.setOnClickListener(v -> dropdownPhong.showDropDown());
    }
    
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedNgayVao);
        
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selectedNgayVao = selected.getTimeInMillis();
                    edtNgayVao.setText(FormatUtils.formatDate(selectedNgayVao));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }
    
    private boolean isPhongLoaded = false;
    private void loadPhongDropdown() {
        if (isPhongLoaded && currentKhachThue == null) return;
        
        viewModel.getAllPhong().observe(this, phongs -> {
            if (phongs != null) {
                isPhongLoaded = true;
                phongList.clear();
                phongNameToIdMap.clear();
                
                List<String> phongNames = new ArrayList<>();
                phongNames.add("-- Chọn phòng --");
                
                for (Phong phong : phongs) {
                    phongList.add(phong);
                    String displayName = "Phòng " + phong.getSoPhong();
                    
                    // Thêm thông tin trạng thái để người dùng biết
                    if (Phong.TRANG_THAI_DANG_THUE.equals(phong.getTrangThai())) {
                        displayName += " (Đang thuê)";
                    } else if (Phong.TRANG_THAI_DANG_SUA.equals(phong.getTrangThai())) {
                        displayName += " (Đang sửa)";
                    } else {
                        displayName += " (Trống)";
                    }
                    
                    phongNames.add(displayName);
                    phongNameToIdMap.put(displayName, phong.getId());
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        phongNames
                );
                dropdownPhong.setAdapter(adapter);
                
                // Chọn phòng hiện tại nếu đang sửa
                String selectedPhongName = phongNames.get(0);
                if (currentKhachThue != null && currentKhachThue.getPhongId() != null) {
                    for (Phong p : phongs) {
                        if (p.getId() == currentKhachThue.getPhongId().intValue()) {
                            selectedPhongName = "Phòng " + p.getSoPhong();
                            if (Phong.TRANG_THAI_DANG_THUE.equals(p.getTrangThai())) selectedPhongName += " (Đang thuê)";
                            else if (Phong.TRANG_THAI_DANG_SUA.equals(p.getTrangThai())) selectedPhongName += " (Đang sửa)";
                            else selectedPhongName += " (Trống)";
                            break;
                        }
                    }
                }
                dropdownPhong.setText(selectedPhongName, false);
            }
        });
    }
    
    private void loadKhachThueData() {
        viewModel.getKhachThueById(khachThueId).observe(this, khachThue -> {
            if (khachThue != null && currentKhachThue == null) {
                currentKhachThue = khachThue;
                
                edtHoTen.setText(khachThue.getHoTen());
                edtCccd.setText(khachThue.getCccd());
                edtSdt.setText(khachThue.getSoDienThoai());
                edtEmail.setText(khachThue.getEmail());
                edtQueQuan.setText(khachThue.getQueQuan());
                edtNgheNghiep.setText(khachThue.getNgheNghiep());
                edtGhiChu.setText(khachThue.getGhiChu());
                
                selectedNgayVao = khachThue.getNgayVao();
                edtNgayVao.setText(FormatUtils.formatDate(selectedNgayVao));
                
                // Gọi load dropdown sau khi đã có currentKhachThue
                loadPhongDropdown();
            }
        });
    }
    
    private void saveKhachThue() {
        // Validate
        String hoTen = edtHoTen.getText().toString().trim();
        String cccd = edtCccd.getText().toString().trim();
        String sdt = edtSdt.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String queQuan = edtQueQuan.getText().toString().trim();
        String ngheNghiep = edtNgheNghiep.getText().toString().trim();
        String ghiChu = edtGhiChu.getText().toString().trim();
        String selectedPhongName = dropdownPhong.getText().toString();
        
        // Validate họ tên
        if (hoTen.isEmpty()) {
            tilHoTen.setError(getString(R.string.error_required));
            return;
        }
        tilHoTen.setError(null);
        
        // Validate SĐT
        if (sdt.isEmpty()) {
            tilSdt.setError(getString(R.string.error_required));
            return;
        }
        tilSdt.setError(null);
        
        // Lấy phòng ID
        Integer phongId = phongNameToIdMap.get(selectedPhongName);
        // Tạo hoặc cập nhật khách thuê
        KhachThue khachThue;
        if (khachThueId != -1 && currentKhachThue != null) {
            // Cập nhật
            khachThue = currentKhachThue;
        } else {
            // Tạo mới
            khachThue = new KhachThue();
            khachThue.setCloudId(java.util.UUID.randomUUID().toString());
        }
        khachThue.setHoTen(hoTen);
        khachThue.setCccd(cccd);
        khachThue.setSoDienThoai(sdt);
        khachThue.setEmail(email);
        khachThue.setQueQuan(queQuan);
        khachThue.setNgheNghiep(ngheNghiep);
        khachThue.setGhiChu(ghiChu);
        khachThue.setNgayVao(selectedNgayVao);
        khachThue.setPhongId(phongId);
        
        com.example.quan_ly_tro.data.database.AppDatabase.databaseWriteExecutor.execute(() -> {
            com.example.quan_ly_tro.data.database.AppDatabase db = com.example.quan_ly_tro.data.database.AppDatabase.getDatabase(this);
            com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = new com.example.quan_ly_tro.sync.FirebaseSyncManager(this);
            
            if (khachThueId != -1) {
                db.khachThueDao().update(khachThue);
            } else {
                long newId = db.khachThueDao().insert(khachThue);
                khachThue.setId((int) newId);
                createUserForTenant(khachThue, phongId);
                
                // Cập nhật trạng thái Phòng thành "Đang thuê" khi thêm khách mới
                if (phongId != null) {
                    Phong phong = db.phongDao().getPhongByIdSync(phongId);
                    if (phong != null && !Phong.TRANG_THAI_DANG_THUE.equals(phong.getTrangThai())) {
                        phong.setTrangThai(Phong.TRANG_THAI_DANG_THUE);
                        db.phongDao().update(phong);
                        syncManager.syncSinglePhong(phong); // Đồng bộ trạng thái phòng lên Cloud
                    }
                }
            }
            
            syncManager.syncSingleKhachThue(khachThue);
            
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.msg_luu_thanh_cong, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void createUserForTenant(KhachThue khachThue, Integer phongId) {
        String password = edtPassword.getText().toString().trim();
        if (password.isEmpty() || password.length() < 6) {
            runOnUiThread(() -> Toast.makeText(this, "Vui lòng nhập mật khẩu hợp lệ (>6 ký tự) để tạo tài khoản khách", Toast.LENGTH_SHORT).show());
            return;
        }

        com.example.quan_ly_tro.data.database.entity.User user = new com.example.quan_ly_tro.data.database.entity.User();
        user.setEmail(khachThue.getEmail());
        user.setHoTen(khachThue.getHoTen());
        user.setSoDienThoai(khachThue.getSoDienThoai());
        user.setRole(com.example.quan_ly_tro.data.database.entity.User.ROLE_TENANT);
        user.setPhongId(phongId);
        
        // Lưu vào Room (Local)
        com.example.quan_ly_tro.data.database.AppDatabase.databaseWriteExecutor.execute(() -> {
            com.example.quan_ly_tro.data.database.AppDatabase.getDatabase(this).userDao().insert(user);
        });

        // Tạo tài khoản trên Firebase sử dụng Secondary App để không bị đăng xuất
        try {
            FirebaseOptions options = FirebaseApp.getInstance().getOptions();
            FirebaseApp secondaryApp;
            try {
                secondaryApp = FirebaseApp.getInstance("Secondary");
            } catch (IllegalStateException e) {
                secondaryApp = FirebaseApp.initializeApp(getApplicationContext(), options, "Secondary");
            }
            
            FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);
            secondaryAuth.createUserWithEmailAndPassword(khachThue.getEmail(), password)
                    .addOnSuccessListener(authResult -> {
                        if (authResult.getUser() != null) {
                            String uid = authResult.getUser().getUid();
                            
                            // Tạo record trong bảng users trên Firestore
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", uid);
                            userMap.put("email", khachThue.getEmail());
                            userMap.put("role", com.example.quan_ly_tro.data.database.entity.User.ROLE_TENANT);
                            userMap.put("ngayTao", System.currentTimeMillis());

                            FirebaseFirestore.getInstance().collection("users").document(uid).set(userMap);
                            
                            runOnUiThread(() -> Toast.makeText(ThemKhachThueActivity.this, "Đã tạo tài khoản cho khách thuê", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() -> Toast.makeText(ThemKhachThueActivity.this, "Lỗi tạo tài khoản: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
