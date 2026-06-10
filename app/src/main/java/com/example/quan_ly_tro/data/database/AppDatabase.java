package com.example.quan_ly_tro.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.quan_ly_tro.data.database.dao.ChiTietHoaDonDao;
import com.example.quan_ly_tro.data.database.dao.DichVuDao;
import com.example.quan_ly_tro.data.database.dao.HoaDonDao;
import com.example.quan_ly_tro.data.database.dao.HopDongDao;
import com.example.quan_ly_tro.data.database.dao.KhachThueDao;
import com.example.quan_ly_tro.data.database.dao.PhongDao;
import com.example.quan_ly_tro.data.database.dao.ThuChiDao;
import com.example.quan_ly_tro.data.database.entity.ChiTietHoaDon;
import com.example.quan_ly_tro.data.database.entity.DichVu;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.HopDong;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.database.entity.ThuChi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.quan_ly_tro.data.database.dao.UserDao;
import com.example.quan_ly_tro.data.database.entity.User;

import com.example.quan_ly_tro.data.database.entity.SuCo;
import com.example.quan_ly_tro.data.database.entity.ThongBao;

import com.example.quan_ly_tro.data.database.dao.SuCoDao;
import com.example.quan_ly_tro.data.database.dao.ThongBaoDao;

import com.example.quan_ly_tro.utils.DataGenerator;

/**
 * Room Database chính của ứng dụng
 */
@Database(
    entities = {
        Phong.class,
        KhachThue.class,
        DichVu.class,
        HoaDon.class,
        ChiTietHoaDon.class,
        ThuChi.class,
        HopDong.class,
        User.class,
        SuCo.class,
        ThongBao.class
    },
    version = 9,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    // DAOs
    public abstract PhongDao phongDao();
    public abstract KhachThueDao khachThueDao();
    public abstract DichVuDao dichVuDao();
    public abstract HoaDonDao hoaDonDao();
    public abstract ChiTietHoaDonDao chiTietHoaDonDao();
    public abstract ThuChiDao thuChiDao();
    public abstract HopDongDao hopDongDao();
    public abstract UserDao userDao();
    public abstract SuCoDao suCoDao();
    public abstract ThongBaoDao thongBaoDao();
    
    // Singleton instance
    private static volatile AppDatabase INSTANCE;
    
    // Executor service cho background operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = 
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    
    /**
     * Get singleton instance của database
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "quan_ly_tro_database"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(sRoomDatabaseCallback)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Callback để khởi tạo dữ liệu mặc định khi database được tạo
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            
            // Khởi tạo dữ liệu mặc định trong background thread
            databaseWriteExecutor.execute(() -> {
                DataGenerator.insertDummyData(INSTANCE);
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Không tự động nạp dữ liệu mẫu ở đây nữa để tránh ghi đè dữ liệu người dùng đã xóa
        }
    };
}
