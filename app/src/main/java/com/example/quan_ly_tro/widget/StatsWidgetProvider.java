package com.example.quan_ly_tro.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.quan_ly_tro.MainActivity;
import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.Phong;

/**
 * AppWidgetProvider để hiển thị thống kê phòng trọ trên home screen
 */
public class StatsWidgetProvider extends AppWidgetProvider {
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    
    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_stats);
        
        // Set click to open app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.tv_tong_phong, pendingIntent);
        
        // Get data from database asynchronously
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            
            // Count rooms
            int tongPhong = 0;
            int phongTrong = 0;
            int phongDangThue = 0;
            
            try {
                // Using synchronous queries for widget
                java.util.List<Phong> phongList = db.phongDao().getAllPhongSync();
                if (phongList != null) {
                    tongPhong = phongList.size();
                    for (Phong p : phongList) {
                        if (Phong.TRANG_THAI_TRONG.equals(p.getTrangThai())) {
                            phongTrong++;
                        } else if (Phong.TRANG_THAI_DANG_THUE.equals(p.getTrangThai())) {
                            phongDangThue++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Count unpaid invoices
            int hoaDonChuaThu = 0;
            try {
                java.util.List<HoaDon> hoaDonList = db.hoaDonDao().getHoaDonChuaThanhToanSync();
                if (hoaDonList != null) {
                    hoaDonChuaThu = hoaDonList.size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Update views
            final int finalTongPhong = tongPhong;
            final int finalPhongTrong = phongTrong;
            final int finalPhongDangThue = phongDangThue;
            final int finalHoaDonChuaThu = hoaDonChuaThu;
            
            views.setTextViewText(R.id.tv_tong_phong, String.valueOf(finalTongPhong));
            views.setTextViewText(R.id.tv_phong_trong, String.valueOf(finalPhongTrong));
            views.setTextViewText(R.id.tv_dang_thue, String.valueOf(finalPhongDangThue));
            views.setTextViewText(R.id.tv_hoa_don_chua_thu, String.valueOf(finalHoaDonChuaThu));
            
            // Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
        
        // Initial update with zeros
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    
    @Override
    public void onEnabled(Context context) {
        // Widget first enabled
    }
    
    @Override
    public void onDisabled(Context context) {
        // Widget last disabled
    }
}
