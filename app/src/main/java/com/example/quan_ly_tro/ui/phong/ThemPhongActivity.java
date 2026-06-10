package com.example.quan_ly_tro.ui.phong;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.example.quan_ly_tro.utils.QrCodeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.OutputStream;

/**
 * Activity thêm/sửa phòng
 */
public class ThemPhongActivity extends AppCompatActivity {
    
    private PhongViewModel viewModel;
    
    private TextView tvTitle;
    private ImageButton btnBack;
    private TextInputLayout tilSoPhong, tilLoaiPhong, tilGiaThue, tilDienTich, tilTrangThai, tilMoTa;
    private TextInputEditText edtSoPhong, edtGiaThue, edtDienTich, edtMoTa;
    private AutoCompleteTextView dropdownLoaiPhong, dropdownTrangThai;
    private MaterialButton btnHuy, btnLuu;
    private ImageButton btnQrCode;
    
    private int phongId = -1; // -1 = thêm mới
    private Phong currentPhong;
    
    private final String[] loaiPhongOptions = {
            Phong.LOAI_PHONG_DON,
            Phong.LOAI_PHONG_DOI,
            Phong.LOAI_PHONG_STUDIO
    };
    
    private final String[] trangThaiOptions = {
            Phong.TRANG_THAI_TRONG,
            Phong.TRANG_THAI_DANG_THUE,
            Phong.TRANG_THAI_DANG_SUA
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_phong);
        
        initViews();
        initViewModel();
        setupDropdowns();
        setupClickListeners();
        
        // Kiểm tra có phải sửa phòng không
        phongId = getIntent().getIntExtra("phong_id", -1);
        if (phongId != -1) {
            tvTitle.setText(R.string.phong_sua);
            loadPhongData();
        }
    }
    
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        btnBack = findViewById(R.id.btn_back);
        
        tilSoPhong = findViewById(R.id.til_so_phong);
        tilLoaiPhong = findViewById(R.id.til_loai_phong);
        tilGiaThue = findViewById(R.id.til_gia_thue);
        tilDienTich = findViewById(R.id.til_dien_tich);
        tilTrangThai = findViewById(R.id.til_trang_thai);
        tilMoTa = findViewById(R.id.til_mo_ta);
        
        edtSoPhong = findViewById(R.id.edt_so_phong);
        edtGiaThue = findViewById(R.id.edt_gia_thue);
        edtDienTich = findViewById(R.id.edt_dien_tich);
        edtMoTa = findViewById(R.id.edt_mo_ta);
        
        dropdownLoaiPhong = findViewById(R.id.dropdown_loai_phong);
        dropdownTrangThai = findViewById(R.id.dropdown_trang_thai);
        
        btnHuy = findViewById(R.id.btn_huy);
        btnLuu = findViewById(R.id.btn_luu);
        btnQrCode = findViewById(R.id.btn_qr_code);
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(PhongViewModel.class);
    }
    
    private void setupDropdowns() {
        ArrayAdapter<String> loaiAdapter = new ArrayAdapter<>(
                this, 
                android.R.layout.simple_dropdown_item_1line, 
                loaiPhongOptions
        );
        dropdownLoaiPhong.setAdapter(loaiAdapter);
        dropdownLoaiPhong.setText(loaiPhongOptions[0], false);
        
        ArrayAdapter<String> trangThaiAdapter = new ArrayAdapter<>(
                this, 
                android.R.layout.simple_dropdown_item_1line, 
                trangThaiOptions
        );
        dropdownTrangThai.setAdapter(trangThaiAdapter);
        dropdownTrangThai.setText(trangThaiOptions[0], false);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHuy.setOnClickListener(v -> finish());
        btnLuu.setOnClickListener(v -> savePhong());
        btnQrCode.setOnClickListener(v -> showQrDialog());
    }
    
    private void loadPhongData() {
        viewModel.getPhongById(phongId).observe(this, phong -> {
            if (phong != null) {
                currentPhong = phong;
                edtSoPhong.setText(phong.getSoPhong());
                dropdownLoaiPhong.setText(phong.getLoaiPhong(), false);
                edtGiaThue.setText(String.valueOf((long) phong.getGiaThue()));
                edtDienTich.setText(String.valueOf(phong.getDienTich()));
                dropdownTrangThai.setText(phong.getTrangThai(), false);
                edtMoTa.setText(phong.getMoTa());
                
                // Show QR button when editing
                btnQrCode.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void savePhong() {
        // Validate
        String soPhong = edtSoPhong.getText().toString().trim();
        String loaiPhong = dropdownLoaiPhong.getText().toString();
        String giaThueStr = edtGiaThue.getText().toString().trim();
        String dienTichStr = edtDienTich.getText().toString().trim();
        String trangThai = dropdownTrangThai.getText().toString();
        String moTa = edtMoTa.getText().toString().trim();
        
        if (soPhong.isEmpty()) {
            tilSoPhong.setError(getString(R.string.error_required));
            return;
        }
        tilSoPhong.setError(null);
        
        if (giaThueStr.isEmpty()) {
            tilGiaThue.setError(getString(R.string.error_required));
            return;
        }
        tilGiaThue.setError(null);
        
        double giaThue = Double.parseDouble(giaThueStr);
        double dienTich = dienTichStr.isEmpty() ? 0 : Double.parseDouble(dienTichStr);
        
        // Tạo hoặc cập nhật phòng
        Phong phong;
        if (phongId != -1 && currentPhong != null) {
            // Cập nhật
            phong = currentPhong;
        } else {
            // Tạo mới
            phong = new Phong();
            phong.setCloudId(java.util.UUID.randomUUID().toString());
        }
        
        phong.setSoPhong(soPhong);
        phong.setLoaiPhong(loaiPhong);
        phong.setGiaThue(giaThue);
        phong.setDienTich(dienTich);
        phong.setTrangThai(trangThai);
        phong.setMoTa(moTa);
        
        com.example.quan_ly_tro.data.database.AppDatabase.databaseWriteExecutor.execute(() -> {
            com.example.quan_ly_tro.data.database.AppDatabase db = com.example.quan_ly_tro.data.database.AppDatabase.getDatabase(this);
            if (phongId != -1) {
                db.phongDao().update(phong);
            } else {
                long newId = db.phongDao().insert(phong);
                phong.setId((int) newId);
            }
            com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = new com.example.quan_ly_tro.sync.FirebaseSyncManager(this);
            syncManager.syncSinglePhong(phong);
            
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.msg_luu_thanh_cong, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
    
    private void showQrDialog() {
        if (currentPhong == null) return;
        
        // Generate QR content
        String qrContent = QrCodeUtils.createRoomQrContent(
                currentPhong.getId(),
                currentPhong.getSoPhong(),
                currentPhong.getGiaThue(),
                currentPhong.getTrangThai()
        );
        
        Bitmap qrBitmap = QrCodeUtils.generateQrCode(qrContent);
        if (qrBitmap == null) {
            Toast.makeText(this, "Không thể tạo QR Code", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr_code, null);
        
        ImageView ivQrCode = dialogView.findViewById(R.id.iv_qr_code);
        TextView tvRoomName = dialogView.findViewById(R.id.tv_room_name);
        TextView tvRoomInfo = dialogView.findViewById(R.id.tv_room_info);
        MaterialButton btnShare = dialogView.findViewById(R.id.btn_share);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
        
        ivQrCode.setImageBitmap(qrBitmap);
        tvRoomName.setText("Phòng " + currentPhong.getSoPhong());
        tvRoomInfo.setText(FormatUtils.formatCurrency(currentPhong.getGiaThue()) + "/tháng");
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();
        
        btnShare.setOnClickListener(v -> {
            shareQrCode(qrBitmap);
            dialog.dismiss();
        });
        
        btnSave.setOnClickListener(v -> {
            saveQrCode(qrBitmap);
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void shareQrCode(Bitmap bitmap) {
        try {
            String fileName = "QR_Phong_" + currentPhong.getSoPhong() + ".png";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                OutputStream out = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "QR Code Phòng " + currentPhong.getSoPhong());
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ QR Code"));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi chia sẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveQrCode(Bitmap bitmap) {
        try {
            String fileName = "QR_Phong_" + currentPhong.getSoPhong() + "_" + System.currentTimeMillis() + ".png";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QuanLyTro");
            
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                OutputStream out = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                Toast.makeText(this, "Đã lưu vào Thư viện ảnh", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
