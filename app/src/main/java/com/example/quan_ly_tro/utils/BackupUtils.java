package com.example.quan_ly_tro.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class để backup và restore database
 */
public class BackupUtils {
    
    private static final String DATABASE_NAME = "quan_ly_tro_database";
    
    /**
     * Backup database ra file
     */
    public static boolean backupDatabase(Context context) {
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            File walFile = new File(dbFile.getPath() + "-wal");
            File shmFile = new File(dbFile.getPath() + "-shm");

            if (!dbFile.exists()) {
                Toast.makeText(context, "Không tìm thấy database", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            // Tạo thư mục backup
            File backupDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Backup");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            // Tạo tên file với timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            
            // Copy file chính
            copyFile(dbFile, new File(backupDir, "backup_" + timestamp + ".db"));
            
            // Copy file WAL nếu có
            if (walFile.exists()) {
                copyFile(walFile, new File(backupDir, "backup_" + timestamp + ".db-wal"));
            }
            
            // Copy file SHM nếu có
            if (shmFile.exists()) {
                copyFile(shmFile, new File(backupDir, "backup_" + timestamp + ".db-shm"));
            }
            
            Toast.makeText(context, "Đã sao lưu đầy đủ dữ liệu", Toast.LENGTH_LONG).show();
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi sao lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    /**
     * Restore database từ file
     */
    public static boolean restoreDatabase(Context context, File backupFile) {
        try {
            if (!backupFile.exists()) {
                Toast.makeText(context, "File backup không tồn tại", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            
            // Copy file backup vào database
            copyFile(backupFile, dbFile);
            
            Toast.makeText(context, "Đã khôi phục dữ liệu thành công!", Toast.LENGTH_LONG).show();
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khôi phục: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    /**
     * Lấy danh sách các file backup
     */
    public static File[] getBackupFiles(Context context) {
        File backupDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Backup");
        if (!backupDir.exists()) {
            return new File[0];
        }
        
        return backupDir.listFiles((dir, name) -> name.endsWith(".db"));
    }
    
    /**
     * Xóa file backup
     */
    public static boolean deleteBackup(File backupFile) {
        return backupFile.delete();
    }
    
    /**
     * Lấy thư mục backup
     */
    public static File getBackupDirectory(Context context) {
        File backupDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        return backupDir;
    }
    
    private static void copyFile(File src, File dst) throws IOException {
        try (FileChannel inChannel = new FileInputStream(src).getChannel();
             FileChannel outChannel = new FileOutputStream(dst).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }
}
