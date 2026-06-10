package com.example.quan_ly_tro.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.dao.HoaDonDao;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.utils.FormatUtils;
import com.example.quan_ly_tro.utils.NotificationHelper;

import java.util.List;

/**
 * Worker để kiểm tra hóa đơn chưa thanh toán và gửi thông báo nhắc nhở
 */
public class ReminderWorker extends Worker {
    
    public static final String WORK_NAME = "payment_reminder_work";
    
    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        
        try {
            // Lấy danh sách hóa đơn chưa thanh toán từ database
            AppDatabase db = AppDatabase.getDatabase(context);
            HoaDonDao hoaDonDao = db.hoaDonDao();
            
            List<HoaDon> unpaidInvoices = hoaDonDao.getHoaDonChuaThanhToanSync();
            
            if (unpaidInvoices != null && !unpaidInvoices.isEmpty()) {
                int count = unpaidInvoices.size();
                double totalAmount = 0;
                for (HoaDon hd : unpaidInvoices) {
                    totalAmount += hd.getTongTien();
                }
                
                String formattedAmount = FormatUtils.formatCurrency(totalAmount);
                
                // Hiển thị thông báo
                NotificationHelper.showUnpaidInvoiceNotification(context, count, formattedAmount);
            }
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
