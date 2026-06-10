package com.example.quan_ly_tro.utils;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.quan_ly_tro.worker.ReminderWorker;

import java.util.concurrent.TimeUnit;

/**
 * Utility để lập lịch nhắc nhở thanh toán
 */
public class ReminderScheduler {
    
    /**
     * Lập lịch chạy nhắc nhở mỗi ngày
     */
    public static void scheduleDailyReminder(Context context) {
        // Constraints - không bắt buộc mạng
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();
        
        // Chạy mỗi 24 giờ (tối thiểu 15 phút cho PeriodicWorkRequest)
        PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
                ReminderWorker.class, 
                24, TimeUnit.HOURS
        )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.MINUTES) // Delay 1 phút khi khởi động
                .build();
        
        // Enqueue work, thay thế nếu đã tồn tại
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                ReminderWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                reminderWork
        );
    }
    
    /**
     * Chạy nhắc nhở ngay lập tức (1 lần)
     */
    public static void runReminderNow(Context context) {
        androidx.work.OneTimeWorkRequest reminderWork = 
                new androidx.work.OneTimeWorkRequest.Builder(ReminderWorker.class)
                        .build();
        
        WorkManager.getInstance(context).enqueue(reminderWork);
    }
    
    /**
     * Hủy tất cả nhắc nhở
     */
    public static void cancelReminders(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(ReminderWorker.WORK_NAME);
    }
}
