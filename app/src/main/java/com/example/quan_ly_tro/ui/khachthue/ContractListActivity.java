package com.example.quan_ly_tro.ui.khachthue;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity hiển thị danh sách các file hợp đồng PDF đã tạo với giao diện hiện đại
 */
public class ContractListActivity extends AppCompatActivity implements TaiLieuAdapter.OnTaiLieuClickListener {

    private RecyclerView rvContracts;
    private View layoutEmpty;
    private View btnBack;
    
    private TaiLieuAdapter adapter;
    private List<File> contractFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_list);

        initViews();
        loadContracts();
    }

    private void initViews() {
        rvContracts = findViewById(R.id.rv_contracts);
        layoutEmpty = findViewById(R.id.layout_empty);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        adapter = new TaiLieuAdapter();
        adapter.setOnTaiLieuClickListener(this);
        rvContracts.setLayoutManager(new LinearLayoutManager(this));
        rvContracts.setAdapter(adapter);
    }

    private void loadContracts() {
        File docsDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Contracts");
        File[] files = docsDir.listFiles((dir, name) -> name.endsWith(".pdf"));

        if (files == null || files.length == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvContracts.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        rvContracts.setVisibility(View.VISIBLE);

        // Sắp xếp file mới nhất lên đầu
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        contractFiles = Arrays.asList(files);
        adapter.setFileList(contractFiles);
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
        String[] options = {"Mở hợp đồng", "Chia sẻ", "Xóa"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Tùy chọn hợp đồng")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openPdf(file);
                    else if (which == 1) sharePdf(file);
                    else if (which == 2) showDeleteDialog(file);
                })
                .show();
    }

    private void openPdf(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        try {
            startActivity(Intent.createChooser(intent, "Mở hợp đồng với:"));
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
        startActivity(Intent.createChooser(intent, "Chia sẻ hợp đồng qua:"));
    }

    private void showDeleteDialog(File file) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa hợp đồng")
                .setMessage("Bạn có chắc chắn muốn xóa file này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (file.delete()) {
                        Toast.makeText(this, "Đã xóa file", Toast.LENGTH_SHORT).show();
                        loadContracts();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
