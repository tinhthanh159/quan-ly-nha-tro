package com.example.quan_ly_tro.ui.thuchi;

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
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.database.entity.ThuChi;
import com.example.quan_ly_tro.data.repository.PhongRepository;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity thêm/sửa giao dịch thu/chi
 */
public class ThemThuChiActivity extends AppCompatActivity {
    
    private ThuChiViewModel viewModel;
    private PhongRepository phongRepository;
    
    private TextView tvTitle;
    private ImageButton btnBack;
    private MaterialButtonToggleGroup toggleLoai;
    private MaterialButton btnThu, btnChi;
    private TextInputLayout tilSoTien, tilDanhMuc, tilNgay, tilPhong, tilMoTa;
    private TextInputEditText edtSoTien, edtNgay, edtMoTa;
    private AutoCompleteTextView dropdownDanhMuc, dropdownPhong;
    private MaterialButton btnHuy, btnLuu;
    
    private int thuChiId = -1;
    private ThuChi currentThuChi;
    private long selectedNgay = System.currentTimeMillis();
    private String selectedLoai = ThuChi.LOAI_THU;
    
    private Map<String, Integer> phongNameToIdMap = new HashMap<>();
    
    private final String[] danhMucThu = {
            ThuChi.DANH_MUC_TIEN_THUE,
            ThuChi.DANH_MUC_TIEN_COC,
            ThuChi.DANH_MUC_TIEN_DICH_VU,
            ThuChi.DANH_MUC_THU_KHAC
    };
    
    private final String[] danhMucChi = {
            ThuChi.DANH_MUC_SUA_CHUA,
            ThuChi.DANH_MUC_BAO_TRI,
            ThuChi.DANH_MUC_THIET_BI,
            ThuChi.DANH_MUC_DIEN_NUOC,
            ThuChi.DANH_MUC_CHI_KHAC
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_thu_chi);
        
        initViews();
        initViewModel();
        setupClickListeners();
        loadPhongDropdown();
        updateDanhMucDropdown();
        
        thuChiId = getIntent().getIntExtra("thu_chi_id", -1);
        if (thuChiId != -1) {
            tvTitle.setText("Sửa giao dịch");
            loadThuChiData();
        } else {
            edtNgay.setText(FormatUtils.formatDate(System.currentTimeMillis()));
        }
    }
    
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        btnBack = findViewById(R.id.btn_back);
        
        toggleLoai = findViewById(R.id.toggle_loai);
        btnThu = findViewById(R.id.btn_thu);
        btnChi = findViewById(R.id.btn_chi);
        
        tilSoTien = findViewById(R.id.til_so_tien);
        tilDanhMuc = findViewById(R.id.til_danh_muc);
        tilNgay = findViewById(R.id.til_ngay);
        tilPhong = findViewById(R.id.til_phong);
        tilMoTa = findViewById(R.id.til_mo_ta);
        
        edtSoTien = findViewById(R.id.edt_so_tien);
        edtNgay = findViewById(R.id.edt_ngay);
        edtMoTa = findViewById(R.id.edt_mo_ta);
        
        dropdownDanhMuc = findViewById(R.id.dropdown_danh_muc);
        dropdownPhong = findViewById(R.id.dropdown_phong);
        
        btnHuy = findViewById(R.id.btn_huy);
        btnLuu = findViewById(R.id.btn_luu);
        
        // Default select Thu
        toggleLoai.check(R.id.btn_thu);
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(ThuChiViewModel.class);
        phongRepository = new PhongRepository(getApplication());
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHuy.setOnClickListener(v -> finish());
        btnLuu.setOnClickListener(v -> saveThuChi());
        
        // Toggle loại
        toggleLoai.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_thu) {
                    selectedLoai = ThuChi.LOAI_THU;
                } else {
                    selectedLoai = ThuChi.LOAI_CHI;
                }
                updateDanhMucDropdown();
            }
        });
        
        // Date picker
        edtNgay.setOnClickListener(v -> showDatePicker());
        tilNgay.setEndIconOnClickListener(v -> showDatePicker());
    }
    
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedNgay);
        
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selectedNgay = selected.getTimeInMillis();
                    edtNgay.setText(FormatUtils.formatDate(selectedNgay));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }
    
    private void updateDanhMucDropdown() {
        String[] danhMucs = ThuChi.LOAI_THU.equals(selectedLoai) ? danhMucThu : danhMucChi;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                danhMucs
        );
        dropdownDanhMuc.setAdapter(adapter);
        if (danhMucs.length > 0) {
            dropdownDanhMuc.setText(danhMucs[0], false);
        }
    }
    
    private void loadPhongDropdown() {
        phongRepository.getAllPhong().observe(this, phongs -> {
            if (phongs != null) {
                phongNameToIdMap.clear();
                
                List<String> phongNames = new ArrayList<>();
                phongNames.add("-- Không chọn --");
                
                for (Phong phong : phongs) {
                    String displayName = "Phòng " + phong.getSoPhong();
                    phongNames.add(displayName);
                    phongNameToIdMap.put(displayName, phong.getId());
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        phongNames
                );
                dropdownPhong.setAdapter(adapter);
                dropdownPhong.setText(phongNames.get(0), false);
            }
        });
    }
    
    private void loadThuChiData() {
        viewModel.getThuChiById(thuChiId).observe(this, thuChi -> {
            if (thuChi != null) {
                currentThuChi = thuChi;
                
                // Set loại
                selectedLoai = thuChi.getLoai();
                if (ThuChi.LOAI_THU.equals(selectedLoai)) {
                    toggleLoai.check(R.id.btn_thu);
                } else {
                    toggleLoai.check(R.id.btn_chi);
                }
                
                edtSoTien.setText(String.valueOf((long) thuChi.getSoTien()));
                dropdownDanhMuc.setText(thuChi.getDanhMuc(), false);
                selectedNgay = thuChi.getNgayGiaoDich();
                edtNgay.setText(FormatUtils.formatDate(selectedNgay));
                edtMoTa.setText(thuChi.getMoTa());
                
                // Set phòng
                if (thuChi.getPhongId() != null) {
                    for (Map.Entry<String, Integer> entry : phongNameToIdMap.entrySet()) {
                        if (entry.getValue().equals(thuChi.getPhongId())) {
                            dropdownPhong.setText(entry.getKey(), false);
                            break;
                        }
                    }
                }
            }
        });
    }
    
    private void saveThuChi() {
        String soTienStr = edtSoTien.getText().toString().trim();
        String danhMuc = dropdownDanhMuc.getText().toString();
        String moTa = edtMoTa.getText().toString().trim();
        String selectedPhongName = dropdownPhong.getText().toString();
        
        // Validate
        if (soTienStr.isEmpty()) {
            tilSoTien.setError(getString(R.string.error_required));
            return;
        }
        tilSoTien.setError(null);
        
        double soTien = Double.parseDouble(soTienStr);
        Integer phongId = phongNameToIdMap.get(selectedPhongName);
        
        ThuChi thuChi;
        if (thuChiId != -1 && currentThuChi != null) {
            thuChi = currentThuChi;
        } else {
            thuChi = new ThuChi();
        }
        
        thuChi.setLoai(selectedLoai);
        thuChi.setSoTien(soTien);
        thuChi.setDanhMuc(danhMuc);
        thuChi.setNgayGiaoDich(selectedNgay);
        thuChi.setMoTa(moTa);
        thuChi.setPhongId(phongId);
        
        if (thuChiId != -1) {
            viewModel.update(thuChi);
        } else {
            viewModel.insert(thuChi);
        }
        
        Toast.makeText(this, R.string.msg_luu_thanh_cong, Toast.LENGTH_SHORT).show();
        finish();
    }
}
