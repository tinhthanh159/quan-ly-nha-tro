package com.example.quan_ly_tro.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.utils.BackupUtils;
import com.example.quan_ly_tro.sync.FirebaseSyncManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.app.ProgressDialog;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity cài đặt với backup/restore
 */
public class SettingsActivity extends AppCompatActivity {
    
    private ImageButton btnBack;
    private MaterialButton btnBackup, btnRestore, btnCloudSync, btnManageDocs, btnLogout;
    private LinearLayout layoutBackupList;
    private FirebaseSyncManager syncManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        syncManager = new FirebaseSyncManager(this);
        
        // Apply enter animation
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        
        initViews();
        setupClickListeners();
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnBackup = findViewById(R.id.btn_backup);
        btnRestore = findViewById(R.id.btn_restore);
        btnCloudSync = findViewById(R.id.btn_cloud_sync);
        btnManageDocs = findViewById(R.id.btn_manage_docs);
        btnLogout = findViewById(R.id.btn_logout);
        layoutBackupList = findViewById(R.id.layout_backup_list);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnBackup.setOnClickListener(v -> performBackup());
        
        btnRestore.setOnClickListener(v -> showBackupList());

        btnCloudSync.setOnClickListener(v -> performCloudSync());

        if (btnManageDocs != null) {
            btnManageDocs.setOnClickListener(v -> {
                startActivity(new Intent(this, DocumentManagerActivity.class));
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, com.example.quan_ly_tro.ui.auth.LoginActivity.class));
                finishAffinity();
            });
        }
    }

    private void performCloudSync() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đồng bộ dữ liệu...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        syncManager.syncToCloud(new FirebaseSyncManager.OnSyncCallback() {
            @Override
            public void onSuccess(String message) {
                // Sau khi đẩy lên, tải về luôn
                syncManager.pullFromCloud(new FirebaseSyncManager.OnSyncCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Đồng bộ thành công!", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Lỗi tải về: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    new MaterialAlertDialogBuilder(SettingsActivity.this)
                            .setTitle("Lỗi đồng bộ")
                            .setMessage(error)
                            .setPositiveButton("Đóng", null)
                            .show();
                });
            }
        });
    }
    
    private void performBackup() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sao lưu dữ liệu")
                .setMessage("Bạn có muốn tạo bản sao lưu dữ liệu?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Sao lưu", (dialog, which) -> {
                    BackupUtils.backupDatabase(this);
                    loadBackupList();
                })
                .show();
    }
    
    private void showBackupList() {
        layoutBackupList.setVisibility(View.VISIBLE);
        loadBackupList();
    }
    
    private void loadBackupList() {
        layoutBackupList.removeAllViews();
        
        File[] backupFiles = BackupUtils.getBackupFiles(this);
        
        if (backupFiles == null || backupFiles.length == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Chưa có bản sao lưu nào");
            emptyText.setTextColor(getResources().getColor(R.color.gray, null));
            emptyText.setPadding(0, 16, 0, 16);
            layoutBackupList.addView(emptyText);
            return;
        }
        
        // Sort by date (newest first)
        java.util.Arrays.sort(backupFiles, (f1, f2) -> 
                Long.compare(f2.lastModified(), f1.lastModified()));
        
        for (File file : backupFiles) {
            View itemView = createBackupItemView(file);
            layoutBackupList.addView(itemView);
        }
    }
    
    private View createBackupItemView(File file) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_backup, layoutBackupList, false);
        
        TextView tvName = view.findViewById(R.id.tv_backup_name);
        TextView tvDate = view.findViewById(R.id.tv_backup_date);
        MaterialButton btnRestoreItem = view.findViewById(R.id.btn_restore_item);
        MaterialButton btnDeleteItem = view.findViewById(R.id.btn_delete_item);
        
        tvName.setText(file.getName());
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(file.lastModified())));
        
        btnRestoreItem.setOnClickListener(v -> confirmRestore(file));
        btnDeleteItem.setOnClickListener(v -> confirmDelete(file));
        
        return view;
    }
    
    private void confirmRestore(File file) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Khôi phục dữ liệu")
                .setMessage("Dữ liệu hiện tại sẽ bị thay thế bằng dữ liệu từ bản sao lưu này. Bạn có chắc chắn?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Khôi phục", (dialog, which) -> {
                    boolean success = BackupUtils.restoreDatabase(this, file);
                    if (success) {
                        // Restart app để áp dụng database mới
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("Thành công")
                                .setMessage("Vui lòng khởi động lại ứng dụng để áp dụng thay đổi.")
                                .setPositiveButton("OK", (d, w) -> {
                                    finishAffinity();
                                    System.exit(0);
                                })
                                .setCancelable(false)
                                .show();
                    }
                })
                .show();
    }
    
    private void confirmDelete(File file) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa bản sao lưu")
                .setMessage("Bạn có chắc muốn xóa bản sao lưu này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    BackupUtils.deleteBackup(file);
                    loadBackupList();
                })
                .show();
    }
}
