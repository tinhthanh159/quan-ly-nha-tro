package com.example.quan_ly_tro.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.quan_ly_tro.MainActivity;
import com.example.quan_ly_tro.R;

/**
 * Helper class để tạo và quản lý thông báo
 */
public class NotificationHelper {
    
    public static final String CHANNEL_ID = "quan_ly_tro_channel";
    public static final String CHANNEL_NAME = "Nhắc nhở thanh toán";
    public static final String CHANNEL_DESC = "Thông báo nhắc nhở hóa đơn chưa thanh toán";
    
    public static final int NOTIFICATION_ID_UNPAID = 1001;
    
    /**
     * Tạo notification channel (cần cho Android 8.0+)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            
            NotificationManager notificationManager = 
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Hiển thị thông báo nhắc nhở hóa đơn chưa thanh toán
     */
    public static void showUnpaidInvoiceNotification(Context context, int unpaidCount, String amount) {
        createNotificationChannel(context);
        
        // Intent khi click vào notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String title = "Nhắc nhở thanh toán";
        String message = String.format("Bạn có %d hóa đơn chưa thanh toán (%s)", unpaidCount, amount);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_receipt)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID_UNPAID, builder.build());
        } catch (SecurityException e) {
            // Notification permission not granted
            e.printStackTrace();
        }
    }
    
    /**
     * Hủy thông báo
     */
    public static void cancelNotification(Context context, int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }
}
