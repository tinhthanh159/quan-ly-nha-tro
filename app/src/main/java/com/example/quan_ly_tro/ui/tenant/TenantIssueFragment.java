package com.example.quan_ly_tro.ui.tenant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.entity.SuCo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_tro.adapter.SuCoAdapter;
import java.util.List;

public class TenantIssueFragment extends Fragment {

    private TextInputEditText edtTitle, edtDesc;
    private MaterialButton btnSend;
    private RecyclerView rvHistory;
    private SuCoAdapter adapter;
    private AppDatabase database;
    private com.google.firebase.firestore.ListenerRegistration issuesListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tenant_issue, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        database = AppDatabase.getDatabase(requireContext());
        edtTitle = view.findViewById(R.id.edt_issue_title);
        edtDesc = view.findViewById(R.id.edt_issue_desc);
        btnSend = view.findViewById(R.id.btn_send_issue);
        rvHistory = view.findViewById(R.id.rv_issue_history);

        setupRecyclerView();
        btnSend.setOnClickListener(v -> sendIssue());
        loadIssueHistory();
        setupRealtimeListener();
    }

    private void setupRealtimeListener() {
        issuesListener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("shared_su_co")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
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
    public void onDestroyView() {
        super.onDestroyView();
        if (issuesListener != null) issuesListener.remove();
    }

    private void setupRecyclerView() {
        adapter = new SuCoAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(adapter);

        // Click để xem chi tiết hoặc sửa (nếu cần)
        adapter.setOnItemClickListener(suCo -> {
            // Hiện tại chỉ hiện thông báo đơn giản
            Toast.makeText(getContext(), "Trạng thái: " + suCo.getTrangThai(), Toast.LENGTH_SHORT).show();
        });

        // Nhấn giữ để xóa
        adapter.setOnItemLongClickListener(this::showDeleteDialog);
    }

    private void showDeleteDialog(SuCo suCo) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa báo cáo sự cố")
                .setMessage("Bạn có chắc chắn muốn xóa lịch sử báo cáo này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        database.suCoDao().delete(suCo);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> 
                                Toast.makeText(getContext(), " Đã xóa báo cáo", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadIssueHistory() {
        // Lấy danh sách sự cố
        database.suCoDao().getAllSuCo().observe(getViewLifecycleOwner(), list -> {
            if (list != null) adapter.setSuCoList(list);
        });
    }

    private void sendIssue() {
        String title = edtTitle.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        android.content.Context appContext = requireContext().getApplicationContext();
        
        btnSend.setEnabled(false);
        btnSend.setText("Đang gửi...");

        AppDatabase.databaseWriteExecutor.execute(() -> {
            com.example.quan_ly_tro.data.database.entity.User user = database.userDao().getUserByUidSync(uid);
            Integer phongId = (user != null) ? user.getPhongId() : null;
            
            final SuCo suCo = new SuCo();
            suCo.setCloudId(java.util.UUID.randomUUID().toString()); // Tạo ID duy nhất
            suCo.setTieuDe(title);
            suCo.setMoTa(desc);
            suCo.setTrangThai(SuCo.STATUS_PENDING);
            suCo.setNgayTao(System.currentTimeMillis());
            
            if (phongId != null) {
                suCo.setPhongId(phongId);
                com.example.quan_ly_tro.data.database.entity.Phong phong = database.phongDao().getPhongByIdSync(phongId);
                if (phong != null) suCo.setTenPhong("Phòng " + phong.getSoPhong());
            } else {
                suCo.setTenPhong("Không xác định");
            }

            database.suCoDao().insert(suCo);
            
            // Tự động đẩy lên Firebase với callback
            com.example.quan_ly_tro.sync.FirebaseSyncManager syncManager = new com.example.quan_ly_tro.sync.FirebaseSyncManager(appContext);
            syncManager.syncToCloud(new com.example.quan_ly_tro.sync.FirebaseSyncManager.OnSyncCallback() {
                @Override
                public void onSuccess(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            btnSend.setEnabled(true);
                            btnSend.setText("Gửi báo cáo");
                            Toast.makeText(appContext, "Đã gửi yêu cầu thành công!", Toast.LENGTH_SHORT).show();
                            edtTitle.setText("");
                            edtDesc.setText("");
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            btnSend.setEnabled(true);
                            btnSend.setText("Gửi lại");
                            Toast.makeText(appContext, "Lỗi đồng bộ: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        });
    }
}
