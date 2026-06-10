package com.example.quan_ly_tro.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.adapter.ThongBaoAdapter;
import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.entity.ThongBao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Activity cho phép Admin đăng thông báo và xem lại lịch sử
 */
public class PostAnnouncementActivity extends AppCompatActivity implements ThongBaoAdapter.OnThongBaoClickListener {

    private TextInputEditText edtTitle, edtContent;
    private MaterialButton btnPost;
    private ImageButton btnBack;
    private RecyclerView rvHistory;
    private ThongBaoAdapter adapter;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_announcement);

        database = AppDatabase.getDatabase(this);
        initViews();
        setupRecyclerView();
        loadHistory();
    }

    private void initViews() {
        edtTitle = findViewById(R.id.edt_notif_title);
        edtContent = findViewById(R.id.edt_notif_content);
        btnPost = findViewById(R.id.btn_post);
        btnBack = findViewById(R.id.btn_back);
        rvHistory = findViewById(R.id.rv_announcement_history);

        btnBack.setOnClickListener(v -> finish());
        btnPost.setOnClickListener(v -> postAnnouncement());
    }

    private void setupRecyclerView() {
        adapter = new ThongBaoAdapter();
        adapter.setOnThongBaoClickListener(this);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);
        rvHistory.setNestedScrollingEnabled(false); // Vì nằm trong ScrollView
    }

    private void loadHistory() {
        database.thongBaoDao().getAllThongBao().observe(this, list -> {
            if (list != null) {
                adapter.setThongBaoList(list);
            }
        });
    }

    private void postAnnouncement() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        ThongBao thongBao = new ThongBao();
        thongBao.setTieuDe(title);
        thongBao.setNoiDung(content);
        thongBao.setNgayTao(System.currentTimeMillis());
        thongBao.setNguoiGui("Chủ trọ");

        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.thongBaoDao().insert(thongBao);
            
            // Tự động đẩy lên Firebase với callback
            com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = new com.example.quan_ly_tro.sync.FirebaseSyncManager(this);
            syncManager.syncToCloud(new com.example.quan_ly_tro.sync.FirebaseSyncManager.OnSyncCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(PostAnnouncementActivity.this, "Đã đăng và đồng bộ thành công!", Toast.LENGTH_SHORT).show();
                        edtTitle.setText("");
                        edtContent.setText("");
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(PostAnnouncementActivity.this, "Lỗi đồng bộ: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    @Override
    public void onThongBaoClick(ThongBao thongBao) {
        // Admin click xem chi tiết
        new MaterialAlertDialogBuilder(this)
                .setTitle(thongBao.getTieuDe())
                .setMessage(thongBao.getNoiDung())
                .setPositiveButton("Đóng", null)
                .show();
    }

    @Override
    public void onThongBaoLongClick(ThongBao thongBao) {
        // Admin nhấn giữ để xóa
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xóa thông báo này khỏi hệ thống?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.thongBaoDao().delete(thongBao);
                        runOnUiThread(() -> Toast.makeText(this, "Đã xóa thông báo", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
