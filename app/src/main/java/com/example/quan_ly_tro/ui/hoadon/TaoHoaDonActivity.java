package com.example.quan_ly_tro.ui.hoadon;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.entity.DichVu;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity tạo hóa đơn mới
 */
public class TaoHoaDonActivity extends AppCompatActivity {
    
    private HoaDonViewModel viewModel;
    
    private TextView tvTitle, tvTienPhong, tvTongTien;
    private ImageButton btnBack;
    private TextInputLayout tilPhong, tilThang;
    private AutoCompleteTextView dropdownPhong;
    private TextInputEditText edtThang;
    private TextInputEditText edtSoDien, edtGiaDien, edtSoNuoc, edtGiaNuoc;
    private CheckBox cbThuRac, cbGuiXe;
    private LinearLayout layoutDichVu;
    private MaterialButton btnHuy, btnLuu;
    
    // Phí cố định
    private static final double PHI_THU_RAC = 20000;
    private static final double PHI_GUI_XE = 50000;
    
    private List<Phong> phongList = new ArrayList<>();
    private Map<String, Phong> phongNameMap = new HashMap<>();
    private List<DichVu> dichVuList = new ArrayList<>();
    private Map<Integer, CheckBox> dichVuCheckboxMap = new HashMap<>();
    
    private Phong selectedPhong = null;
    private double giaDien = 3500;
    private double giaNuoc = 15000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_hoa_don);
        
        initViews();
        initViewModel();
        setupClickListeners();
        setupTextWatchers();
        loadData();
        setDefaultMonth();
    }
    
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvTienPhong = findViewById(R.id.tv_tien_phong);
        tvTongTien = findViewById(R.id.tv_tong_tien);
        btnBack = findViewById(R.id.btn_back);
        
        tilPhong = findViewById(R.id.til_phong);
        tilThang = findViewById(R.id.til_thang);
        dropdownPhong = findViewById(R.id.dropdown_phong);
        edtThang = findViewById(R.id.edt_thang);
        
        edtSoDien = findViewById(R.id.edt_so_dien);
        edtGiaDien = findViewById(R.id.edt_gia_dien);
        edtSoNuoc = findViewById(R.id.edt_so_nuoc);
        edtGiaNuoc = findViewById(R.id.edt_gia_nuoc);
        
        cbThuRac = findViewById(R.id.cb_thu_rac);
        cbGuiXe = findViewById(R.id.cb_gui_xe);
        
        layoutDichVu = findViewById(R.id.layout_dich_vu);
        
        btnHuy = findViewById(R.id.btn_huy);
        btnLuu = findViewById(R.id.btn_luu);
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(HoaDonViewModel.class);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHuy.setOnClickListener(v -> finish());
        btnLuu.setOnClickListener(v -> saveHoaDon());
        
        dropdownPhong.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            if (phongNameMap.containsKey(selectedName)) {
                selectedPhong = phongNameMap.get(selectedName);
                updateTienPhong();
                calculateTotal();
            }
        });
    }
    
    private void setupTextWatchers() {
        TextWatcher calculateWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                calculateTotal();
            }
        };
        
        edtSoDien.addTextChangedListener(calculateWatcher);
        edtGiaDien.addTextChangedListener(calculateWatcher);
        edtSoNuoc.addTextChangedListener(calculateWatcher);
        edtGiaNuoc.addTextChangedListener(calculateWatcher);
        
        // Listeners cho checkbox phí dịch vụ
        cbThuRac.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
        cbGuiXe.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
    }
    
    private void setDefaultMonth() {
        Calendar cal = Calendar.getInstance();
        String thang = String.format("%02d/%d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
        edtThang.setText(thang);
    }
    
    private void loadData() {
        // Load tất cả phòng (để có thể chọn được)
        viewModel.getAllPhong().observe(this, phongs -> {
            if (phongs != null && !phongs.isEmpty()) {
                phongList.clear();
                phongNameMap.clear();
                
                List<String> phongNames = new ArrayList<>();
                for (Phong phong : phongs) {
                    phongList.add(phong);
                    String displayName = "Phòng " + phong.getSoPhong();
                    phongNames.add(displayName);
                    phongNameMap.put(displayName, phong);
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        phongNames
                );
                dropdownPhong.setAdapter(adapter);
            }
        });
        
        // Load dịch vụ
        viewModel.getAllDichVu().observe(this, dichVus -> {
            if (dichVus != null) {
                dichVuList.clear();
                dichVuList.addAll(dichVus);
                
                layoutDichVu.removeAllViews();
                dichVuCheckboxMap.clear();
                
                for (DichVu dichVu : dichVus) {
                    // Lưu giá điện, nước
                    if (DichVu.DICH_VU_DIEN.equals(dichVu.getTenDichVu())) {
                        giaDien = dichVu.getDonGia();
                    } else if (DichVu.DICH_VU_NUOC.equals(dichVu.getTenDichVu())) {
                        giaNuoc = dichVu.getDonGia();
                    } else if (!dichVu.isTinhTheoChiso()) {
                        // Chỉ hiển thị dịch vụ cố định (không tính theo chỉ số)
                        addDichVuCheckbox(dichVu);
                    }
                }
            }
        });
    }
    
    private void addDichVuCheckbox(DichVu dichVu) {
        View view = LayoutInflater.from(this).inflate(
                android.R.layout.simple_list_item_multiple_choice, 
                layoutDichVu, 
                false
        );
        
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(dichVu.getTenDichVu() + " - " + FormatUtils.formatCurrency(dichVu.getDonGia()));
        checkBox.setChecked(true);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> calculateTotal());
        
        layoutDichVu.addView(checkBox);
        dichVuCheckboxMap.put(dichVu.getId(), checkBox);
    }
    
    private void updateTienPhong() {
        if (selectedPhong != null) {
            tvTienPhong.setText(FormatUtils.formatCurrency(selectedPhong.getGiaThue()));
        } else {
            tvTienPhong.setText("0 đ");
        }
    }
    
    private void calculateTotal() {
        double total = 0;
        
        // Tiền phòng
        if (selectedPhong != null) {
            total += selectedPhong.getGiaThue();
        }
        
        // Tiền điện = Số kWh × Giá
        try {
            int soDien = Integer.parseInt(edtSoDien.getText().toString().trim());
            double giaDienHienTai = Double.parseDouble(edtGiaDien.getText().toString().trim());
            if (soDien > 0 && giaDienHienTai > 0) {
                total += soDien * giaDienHienTai;
            }
        } catch (NumberFormatException ignored) {}
        
        // Tiền nước = Số m³ × Giá
        try {
            int soNuoc = Integer.parseInt(edtSoNuoc.getText().toString().trim());
            double giaNuocHienTai = Double.parseDouble(edtGiaNuoc.getText().toString().trim());
            if (soNuoc > 0 && giaNuocHienTai > 0) {
                total += soNuoc * giaNuocHienTai;
            }
        } catch (NumberFormatException ignored) {}
        
        // Dịch vụ cố định
        for (DichVu dichVu : dichVuList) {
            if (!dichVu.isTinhTheoChiso()) {
                CheckBox cb = dichVuCheckboxMap.get(dichVu.getId());
                if (cb != null && cb.isChecked()) {
                    total += dichVu.getDonGia();
                }
            }
        }
        
        // Phí thu rác (20k)
        if (cbThuRac.isChecked()) {
            total += PHI_THU_RAC;
        }
        
        // Phí gửi xe (50k)
        if (cbGuiXe.isChecked()) {
            total += PHI_GUI_XE;
        }
        
        tvTongTien.setText(FormatUtils.formatCurrency(total));
    }
    
    private void saveHoaDon() {
        // Validate
        if (selectedPhong == null) {
            tilPhong.setError("Vui lòng chọn phòng");
            return;
        }
        tilPhong.setError(null);
        
        String thang = edtThang.getText().toString().trim();
        if (thang.isEmpty()) {
            tilThang.setError("Vui lòng nhập tháng");
            return;
        }
        tilThang.setError(null);
        
        // Tính tổng tiền
        double tongTien = 0;
        
        // Tiền phòng
        tongTien += selectedPhong.getGiaThue();
        
        // Tiền điện
        try {
            int soDien = Integer.parseInt(edtSoDien.getText().toString().trim());
            double giaDienHienTai = Double.parseDouble(edtGiaDien.getText().toString().trim());
            if (soDien > 0 && giaDienHienTai > 0) {
                tongTien += soDien * giaDienHienTai;
            }
        } catch (NumberFormatException ignored) {}
        
        // Tiền nước
        try {
            int soNuoc = Integer.parseInt(edtSoNuoc.getText().toString().trim());
            double giaNuocHienTai = Double.parseDouble(edtGiaNuoc.getText().toString().trim());
            if (soNuoc > 0 && giaNuocHienTai > 0) {
                tongTien += soNuoc * giaNuocHienTai;
            }
        } catch (NumberFormatException ignored) {}
        
        // Dịch vụ cố định
        for (DichVu dichVu : dichVuList) {
            if (!dichVu.isTinhTheoChiso()) {
                CheckBox cb = dichVuCheckboxMap.get(dichVu.getId());
                if (cb != null && cb.isChecked()) {
                    tongTien += dichVu.getDonGia();
                }
            }
        }
        
        // Phí thu rác (20k)
        if (cbThuRac.isChecked()) {
            tongTien += PHI_THU_RAC;
        }
        
        // Phí gửi xe (50k)
        if (cbGuiXe.isChecked()) {
            tongTien += PHI_GUI_XE;
        }
        
        // Tạo hóa đơn
        HoaDon hoaDon = new HoaDon();
        hoaDon.setCloudId(java.util.UUID.randomUUID().toString());
        hoaDon.setPhongId(selectedPhong.getId());
        hoaDon.setThangNam(thang);
        hoaDon.setNgayTao(System.currentTimeMillis());
        hoaDon.setTienPhong(selectedPhong.getGiaThue());
        hoaDon.setTongTien(tongTien);
        hoaDon.setTrangThai(HoaDon.TRANG_THAI_CHUA_THANH_TOAN);
        
        com.example.quan_ly_tro.data.database.AppDatabase.databaseWriteExecutor.execute(() -> {
            long newId = com.example.quan_ly_tro.data.database.AppDatabase.getDatabase(this).hoaDonDao().insert(hoaDon);
            hoaDon.setId((int) newId);
            
            com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = new com.example.quan_ly_tro.sync.FirebaseSyncManager(this);
            syncManager.syncSingleHoaDon(hoaDon);
            
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.msg_luu_thanh_cong, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
