package com.example.quan_ly_tro.ui.khachthue;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.entity.HopDong;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.database.entity.User;
import com.example.quan_ly_tro.ui.phong.PhongViewModel;
import com.example.quan_ly_tro.utils.ContractPdfUtils;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.example.quan_ly_tro.widget.SignatureView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.UUID;

/**
 * Activity tạo hợp đồng với chữ ký điện tử
 */
public class TaoHopDongActivity extends AppCompatActivity {

    private KhachThueViewModel khachThueViewModel;
    private PhongViewModel phongViewModel;

    private TextView tvTitle;
    private ImageButton btnBack;
    private TextInputEditText edtChuTro, edtNgayBatDau, edtNgayKetThuc, edtTienCoc, edtNoiDung;
    private SignatureView signatureView;
    private MaterialButton btnClearSignature, btnHuy, btnXuatPdf, btnHuyHopDong;

    private int khachThueId;
    private boolean isRemoteSend = false;
    private boolean isTenantSigning = false;
    private int hopDongIdToSign = -1;
    private KhachThue currentKhachThue;
    private Phong currentPhong;
    private HopDong currentHopDong;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_hop_dong);

        database = AppDatabase.getDatabase(this);
        khachThueId = getIntent().getIntExtra("khach_thue_id", -1);
        isRemoteSend = getIntent().getBooleanExtra("is_remote_send", false);
        isTenantSigning = getIntent().getBooleanExtra("is_tenant_signing", false);
        hopDongIdToSign = getIntent().getIntExtra("hop_dong_id", -1);
        
        if (khachThueId == -1 && !isTenantSigning && hopDongIdToSign == -1) {
            finish();
            return;
        }

        initViews();
        if (isRemoteSend) {
            btnXuatPdf.setText("GỬI CHO KHÁCH KÝ");
            signatureView.setVisibility(android.view.View.GONE);
            findViewById(R.id.btn_clear_signature).setVisibility(android.view.View.GONE);
        }
        if (isTenantSigning) {
            btnXuatPdf.setText("KÝ VÀ HOÀN TẤT");
            lockFieldsForTenant();
        }
        
        if (!isTenantSigning && hopDongIdToSign != -1) {
             btnHuyHopDong.setVisibility(android.view.View.VISIBLE);
        }
        
        initViewModels();
        setupClickListeners();
        loadData();
    }

    private void lockFieldsForTenant() {
        edtChuTro.setEnabled(false);
        edtNgayBatDau.setEnabled(false);
        edtNgayKetThuc.setEnabled(false);
        edtTienCoc.setEnabled(false);
        edtNoiDung.setEnabled(false);
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        btnBack = findViewById(R.id.btn_back);
        edtChuTro = findViewById(R.id.edt_chu_tro);
        edtNgayBatDau = findViewById(R.id.edt_ngay_bat_dau);
        edtNgayKetThuc = findViewById(R.id.edt_ngay_ket_thuc);
        edtTienCoc = findViewById(R.id.edt_tien_coc);
        edtNoiDung = findViewById(R.id.edt_noi_dung);
        signatureView = findViewById(R.id.signature_view);
        btnClearSignature = findViewById(R.id.btn_clear_signature);
        btnHuy = findViewById(R.id.btn_huy);
        btnXuatPdf = findViewById(R.id.btn_xuat_pdf);
        btnHuyHopDong = findViewById(R.id.btn_huy_hop_dong);
    }

    private void initViewModels() {
        khachThueViewModel = new ViewModelProvider(this).get(KhachThueViewModel.class);
        phongViewModel = new ViewModelProvider(this).get(PhongViewModel.class);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHuy.setOnClickListener(v -> finish());
        btnClearSignature.setOnClickListener(v -> signatureView.clear());

        edtNgayBatDau.setOnClickListener(v -> showDatePicker(edtNgayBatDau));
        edtNgayKetThuc.setOnClickListener(v -> showDatePicker(edtNgayKetThuc));

        btnXuatPdf.setOnClickListener(v -> generateContract());
        btnHuyHopDong.setOnClickListener(v -> cancelContract());
    }
    
    private void cancelContract() {
        if (currentHopDong == null) return;
        
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Hủy hợp đồng")
                .setMessage("Bạn có chắc chắn muốn hủy hợp đồng này? Khách thuê sẽ nhận được thông báo ngay lập tức.")
                .setPositiveButton("Hủy Hợp Đồng", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        currentHopDong.setTrangThai(HopDong.TRANG_THAI_CANCELLED);
                        database.hopDongDao().update(currentHopDong);
                        
                        com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = 
                                new com.example.quan_ly_tro.sync.FirebaseSyncManager(TaoHopDongActivity.this);
                        syncManager.syncSingleHopDong(currentHopDong);
                        
                        runOnUiThread(() -> {
                            Toast.makeText(TaoHopDongActivity.this, "Đã hủy hợp đồng thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
            editText.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadData() {
        if (isTenantSigning && hopDongIdToSign != -1) {
            database.hopDongDao().getHopDongById(hopDongIdToSign).observe(this, hd -> {
                if (hd != null) {
                    currentHopDong = hd;
                    edtNgayBatDau.setText(hd.getNgayBatDau());
                    edtNgayKetThuc.setText(hd.getNgayKetThuc());
                    edtTienCoc.setText(String.valueOf((long)hd.getTienCoc()));
                    edtNoiDung.setText(hd.getNoiDung());
                    
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        User landlord = database.userDao().getLandlordSync();
                        if (landlord != null) {
                            runOnUiThread(() -> edtChuTro.setText(landlord.getHoTen()));
                        }
                    });

                    khachThueId = hd.getKhachThueId();
                    loadBaseData();
                }
            });
        } else if (!isTenantSigning && hopDongIdToSign != -1) {
            // Load for Landlord to view/cancel
            database.hopDongDao().getHopDongById(hopDongIdToSign).observe(this, hd -> {
                if (hd != null) {
                    currentHopDong = hd;
                    edtNgayBatDau.setText(hd.getNgayBatDau());
                    edtNgayKetThuc.setText(hd.getNgayKetThuc());
                    edtTienCoc.setText(String.valueOf((long)hd.getTienCoc()));
                    edtNoiDung.setText(hd.getNoiDung());
                    khachThueId = hd.getKhachThueId();
                    
                    // Nếu hợp đồng đã hủy, khóa nút hủy lại
                    if (HopDong.TRANG_THAI_CANCELLED.equals(hd.getTrangThai())) {
                        btnHuyHopDong.setEnabled(false);
                        btnHuyHopDong.setText("ĐÃ HỦY");
                    }
                    
                    loadBaseData();
                }
            });
        } else {
            loadBaseData();
            Calendar cal = Calendar.getInstance();
            edtNgayBatDau.setText(FormatUtils.formatDate(cal.getTimeInMillis()));
            cal.add(Calendar.YEAR, 1);
            edtNgayKetThuc.setText(FormatUtils.formatDate(cal.getTimeInMillis()));
        }
    }

    private void loadBaseData() {
        khachThueViewModel.getKhachThueById(khachThueId).observe(this, khachThue -> {
            if (khachThue != null) {
                currentKhachThue = khachThue;
                tvTitle.setText("Hợp đồng: " + khachThue.getHoTen());
                
                if (khachThue.getPhongId() != null) {
                    phongViewModel.getPhongById(khachThue.getPhongId()).observe(this, phong -> {
                        currentPhong = phong;
                        if (phong != null && !isTenantSigning) {
                            edtTienCoc.setText(String.valueOf((long) phong.getGiaThue()));
                        }
                    });
                }
            }
        });
    }

    private void generateContract() {
        if (currentKhachThue == null || currentPhong == null) {
            Toast.makeText(this, "Thiếu thông tin khách hoặc phòng", Toast.LENGTH_SHORT).show();
            return;
        }

        String chuTro = edtChuTro.getText().toString().trim();
        String ngayBD = edtNgayBatDau.getText().toString().trim();
        String ngayKT = edtNgayKetThuc.getText().toString().trim();
        String tienCocStr = edtTienCoc.getText().toString().trim();
        
        if (isTenantSigning && chuTro.isEmpty()) {
            chuTro = "Chủ nhà";
        }

        if (chuTro.isEmpty() || ngayBD.isEmpty() || ngayKT.isEmpty() || tienCocStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double tienCoc;
        try {
            tienCoc = Double.parseDouble(tienCocStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Tiền cọc không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalChuTro = chuTro;
        final double finalTienCoc = tienCoc;
        
        if (!isRemoteSend && !signatureView.isEdited()) {
            Toast.makeText(this, "Vui lòng ký tên trước khi hoàn tất", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Bitmap signature = isRemoteSend ? null : signatureView.getSignatureBitmap();

        if (isRemoteSend) {
            HopDong hopDong = new HopDong();
            hopDong.setCloudId(UUID.randomUUID().toString());
            hopDong.setPhongId(currentPhong.getId());
            hopDong.setKhachThueId(currentKhachThue.getId());
            hopDong.setNgayBatDau(ngayBD);
            hopDong.setNgayKetThuc(ngayKT);
            hopDong.setGiaThue(currentPhong.getGiaThue());
            hopDong.setTienCoc(finalTienCoc);
            hopDong.setNoiDung(edtNoiDung.getText().toString().trim());
            hopDong.setTrangThai(HopDong.TRANG_THAI_WAITING_FOR_TENANT);
            hopDong.setNgayTao(System.currentTimeMillis());

            AppDatabase.databaseWriteExecutor.execute(() -> {
                database.hopDongDao().insert(hopDong);
                
                // Đồng bộ duy nhất 1 hợp đồng này lên Cloud (nhanh & an toàn)
                com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = 
                        new com.example.quan_ly_tro.sync.FirebaseSyncManager(TaoHopDongActivity.this);
                syncManager.syncSingleHopDong(hopDong);

                runOnUiThread(() -> {
                    Toast.makeText(TaoHopDongActivity.this, "Đã gửi hợp đồng cho khách!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        } else if (isTenantSigning) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                HopDong existing = database.hopDongDao().getHopDongByIdSync(hopDongIdToSign);
                if (existing != null) {
                    existing.setTrangThai(HopDong.TRANG_THAI_ACTIVE);
                    database.hopDongDao().update(existing);
                    
                    // Đồng bộ duy nhất 1 thay đổi này
                    com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = 
                            new com.example.quan_ly_tro.sync.FirebaseSyncManager(TaoHopDongActivity.this);
                    syncManager.syncSingleHopDong(existing);

                    runOnUiThread(() -> {
                        ContractPdfUtils.generateContractWithSignature(
                                TaoHopDongActivity.this, existing, currentPhong, currentKhachThue, finalChuTro, signature
                        );
                        finish();
                    });
                }
            });
        } else {
            HopDong hopDong = new HopDong();
            hopDong.setCloudId(UUID.randomUUID().toString());
            hopDong.setPhongId(currentPhong.getId());
            hopDong.setKhachThueId(currentKhachThue.getId());
            hopDong.setNgayBatDau(ngayBD);
            hopDong.setNgayKetThuc(ngayKT);
            hopDong.setGiaThue(currentPhong.getGiaThue());
            hopDong.setTienCoc(tienCoc);
            hopDong.setNoiDung(edtNoiDung.getText().toString().trim());
            // Sửa lỗi: Hợp đồng vừa tạo phải chờ khách ký, không thể Active ngay
            hopDong.setTrangThai(HopDong.TRANG_THAI_WAITING_FOR_TENANT);
            hopDong.setNgayTao(System.currentTimeMillis());

            AppDatabase.databaseWriteExecutor.execute(() -> {
                // Lưu vào database
                long newId = database.hopDongDao().insert(hopDong);
                hopDong.setId((int) newId);
                
                // Đồng bộ lên Cloud
                com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = 
                        new com.example.quan_ly_tro.sync.FirebaseSyncManager(TaoHopDongActivity.this);
                syncManager.syncSingleHopDong(hopDong);

                runOnUiThread(() -> {
                    Toast.makeText(TaoHopDongActivity.this, "Đã lưu và xuất hợp đồng!", Toast.LENGTH_SHORT).show();
                    ContractPdfUtils.generateContractWithSignature(
                            this, hopDong, currentPhong, currentKhachThue, finalChuTro, signature
                    );
                    finish();
                });
            });
        }
    }
}
