package com.example.quan_ly_tro.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.adapter.SuCoAdapter;
import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.entity.SuCo;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ManageIssuesActivity extends AppCompatActivity {

    private RecyclerView rvIssues;
    private TextView tvEmpty;
    private ImageButton btnBack;
    private SuCoAdapter adapter;
    private AppDatabase database;

    private com.google.firebase.firestore.ListenerRegistration issuesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_issues);

        database = AppDatabase.getDatabase(this);
        initViews();
        setupRecyclerView();
        loadIssues();
        setupRealtimeListener(); // Thêm lắng nghe real-time
    }

    private void setupRealtimeListener() {
        issuesListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("shared_su_co")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ManageIssues", "Listen failed.", error);
                        return;
                    }
                    if (value != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            for (com.google.firebase.firestore.DocumentChange dc : value.getDocumentChanges()) {
                                SuCo sc = dc.getDocument().toObject(SuCo.class);
                                if (sc != null) {
                                    if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED ||
                                        dc.getType() == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                                        database.suCoDao().insert(sc);
                                    } else if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                        database.suCoDao().delete(sc);
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
        if (issuesListener != null) issuesListener.remove();
    }

    private void initViews() {
        rvIssues = findViewById(R.id.rv_issues);
        tvEmpty = findViewById(R.id.tv_empty);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new SuCoAdapter();
        rvIssues.setLayoutManager(new LinearLayoutManager(this));
        rvIssues.setAdapter(adapter);

        adapter.setOnItemClickListener(this::showStatusUpdateDialog);
        
        // Nhấn giữ để xóa
        adapter.setOnItemLongClickListener(this::showDeleteDialog);
    }

    private void showDeleteDialog(SuCo suCo) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa báo cáo sự cố")
                .setMessage("Bạn có chắc chắn muốn xóa báo cáo này khỏi hệ thống?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        // Xóa trên Cloud trước
                        com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = 
                                new com.example.quan_ly_tro.sync.FirebaseSyncManager(this);
                        syncManager.deleteSuCoFromCloud(suCo);
                        
                        database.suCoDao().delete(suCo);
                        runOnUiThread(() -> Toast.makeText(this, "Đã xóa báo cáo", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadIssues() {
        database.suCoDao().getAllSuCo().observe(this, list -> {
            if (list == null || list.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvIssues.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvIssues.setVisibility(View.VISIBLE);
                adapter.setSuCoList(list);
            }
        });
    }

    private void showStatusUpdateDialog(SuCo suCo) {
        String[] statuses = {SuCo.STATUS_PENDING, SuCo.STATUS_IN_PROGRESS, SuCo.STATUS_DONE};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cập nhật trạng thái sự cố")
                .setItems(statuses, (dialog, which) -> {
                    suCo.setTrangThai(statuses[which]);
                    if (statuses[which].equals(SuCo.STATUS_DONE)) {
                        suCo.setNgayHoanThanh(System.currentTimeMillis());
                    }
                    
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.suCoDao().update(suCo);
                        
                        // Đẩy cập nhật lên Cloud ngay lập tức
                        com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = 
                                new com.example.quan_ly_tro.sync.FirebaseSyncManager(this);
                        syncManager.syncToCloud(null); // Đẩy toàn bộ thay đổi local lên
                        
                        runOnUiThread(() -> Toast.makeText(this, "Đã cập nhật trạng thái!", Toast.LENGTH_SHORT).show());
                    });
                })
                .show();
    }
}
