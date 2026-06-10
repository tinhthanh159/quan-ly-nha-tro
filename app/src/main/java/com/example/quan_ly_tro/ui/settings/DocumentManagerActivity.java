package com.example.quan_ly_tro.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.adapter.TaiLieuAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình quản lý toàn bộ tài liệu PDF (Hợp đồng, Báo cáo, Hóa đơn)
 * Giao diện mới hiện đại, dùng chung cho cả Chủ trọ và Khách thuê
 */
public class DocumentManagerActivity extends AppCompatActivity implements TaiLieuAdapter.OnTaiLieuClickListener {

    private RecyclerView rvDocuments;
    private View layoutEmpty;
    private View btnBack;
    private TabLayout tabLayout;
    
    private TaiLieuAdapter adapter;
    private List<File> currentFiles = new ArrayList<>();
    private int currentTab = 0; // 0: Tất cả, 1: Hợp đồng, 2: Báo cáo & Hóa đơn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_manager);

        initViews();
        setupTabs();
        loadDocuments();
    }

    private void initViews() {
        rvDocuments = findViewById(R.id.rv_documents);
        layoutEmpty = findViewById(R.id.layout_empty);
        btnBack = findViewById(R.id.btn_back);
        tabLayout = findViewById(R.id.tab_layout);

        btnBack.setOnClickListener(v -> finish());

        adapter = new TaiLieuAdapter();
        adapter.setOnTaiLieuClickListener(this);
        rvDocuments.setLayoutManager(new LinearLayoutManager(this));
        rvDocuments.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Hợp đồng"));
        tabLayout.addTab(tabLayout.newTab().setText("Báo cáo/Hóa đơn"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadDocuments();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadDocuments() {
        List<File> allFiles = new ArrayList<>();
        
        // 1. Lấy từ thư mục Documents (Báo cáo & Hóa đơn)
        File docsDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (docsDir != null && docsDir.exists()) {
            File[] files = docsDir.listFiles((dir, name) -> name.endsWith(".pdf"));
            if (files != null) {
                for (File f : files) {
                    if (currentTab == 0 || currentTab == 2) allFiles.add(f);
                }
            }
        }

        // 2. Lấy từ thư mục Documents/Contracts (Hợp đồng)
        File contractsDir = new File(docsDir, "Contracts");
        if (contractsDir.exists()) {
            File[] files = contractsDir.listFiles((dir, name) -> name.endsWith(".pdf"));
            if (files != null) {
                for (File f : files) {
                    if (currentTab == 0 || currentTab == 1) allFiles.add(f);
                }
            }
        }

        if (allFiles.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvDocuments.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        rvDocuments.setVisibility(View.VISIBLE);

        // Sắp xếp mới nhất lên đầu
        allFiles.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        currentFiles = allFiles;
        adapter.setFileList(allFiles);
    }

    @Override
    public void onFileClick(File file) {
        openPdf(file);
    }

    @Override
    public void onFileMoreClick(File file) {
        showActionMenu(file);
    }

    private void showActionMenu(File file) {
        String[] options = {"Mở tài liệu", "Chia sẻ", "Xóa"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Tùy chọn tài liệu")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openPdf(file);
                    else if (which == 1) sharePdf(file);
                    else if (which == 2) deletePdf(file);
                })
                .show();
    }

    private void openPdf(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(intent, "Mở tài liệu với:"));
        } catch (Exception e) {
            sharePdf(file);
        }
    }

    private void sharePdf(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Chia sẻ tài liệu qua:"));
    }

    private void deletePdf(File file) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa tài liệu")
                .setMessage("Bạn có chắc chắn muốn xóa file này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (file.delete()) {
                        Toast.makeText(this, "Đã xóa file", Toast.LENGTH_SHORT).show();
                        loadDocuments();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
