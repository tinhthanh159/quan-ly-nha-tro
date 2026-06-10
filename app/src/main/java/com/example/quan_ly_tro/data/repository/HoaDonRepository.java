package com.example.quan_ly_tro.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.dao.ChiTietHoaDonDao;
import com.example.quan_ly_tro.data.database.dao.HoaDonDao;
import com.example.quan_ly_tro.data.database.entity.ChiTietHoaDon;
import com.example.quan_ly_tro.data.database.entity.HoaDon;

import java.util.List;

/**
 * Repository cho HoaDon
 */
public class HoaDonRepository {
    
    private final HoaDonDao hoaDonDao;
    private final ChiTietHoaDonDao chiTietHoaDonDao;
    private final LiveData<List<HoaDon>> allHoaDon;
    
    public HoaDonRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        hoaDonDao = db.hoaDonDao();
        chiTietHoaDonDao = db.chiTietHoaDonDao();
        allHoaDon = hoaDonDao.getAllHoaDon();
    }
    
    public LiveData<List<HoaDon>> getAllHoaDon() {
        return allHoaDon;
    }
    
    public LiveData<HoaDon> getHoaDonById(int id) {
        return hoaDonDao.getHoaDonById(id);
    }
    
    public LiveData<List<HoaDon>> getHoaDonByPhong(int phongId) {
        return hoaDonDao.getHoaDonByPhong(phongId);
    }
    
    public LiveData<List<HoaDon>> getHoaDonByThangNam(String thangNam) {
        return hoaDonDao.getHoaDonByThangNam(thangNam);
    }
    
    public LiveData<List<HoaDon>> getHoaDonChuaThanhToan() {
        return hoaDonDao.getHoaDonChuaThanhToan();
    }
    
    public LiveData<List<HoaDon>> getHoaDonDaThanhToan() {
        return hoaDonDao.getHoaDonDaThanhToan();
    }
    
    public LiveData<List<HoaDon>> getFilteredHoaDon(int phongId, String trangThai) {
        return hoaDonDao.getFilteredHoaDon(phongId, trangThai);
    }

    public LiveData<Integer> getSoHoaDonChuaThanhToan() {
        return hoaDonDao.getSoHoaDonChuaThanhToan();
    }
    
    public LiveData<Double> getTongTienChuaThu() {
        return hoaDonDao.getTongTienChuaThu();
    }
    
    public LiveData<Double> getDoanhThuThang(String thangNam) {
        return hoaDonDao.getDoanhThuThang(thangNam);
    }
    
    public LiveData<Double> getTongDoanhThu() {
        return hoaDonDao.getTongDoanhThu();
    }
    
    // Chi tiết hóa đơn
    public LiveData<List<ChiTietHoaDon>> getChiTietByHoaDon(int hoaDonId) {
        return chiTietHoaDonDao.getChiTietByHoaDon(hoaDonId);
    }
    
    public void insert(HoaDon hoaDon) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            hoaDonDao.insert(hoaDon);
        });
    }
    
    public void insertWithChiTiet(HoaDon hoaDon, List<ChiTietHoaDon> chiTietList) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long hoaDonId = hoaDonDao.insert(hoaDon);
            for (ChiTietHoaDon chiTiet : chiTietList) {
                chiTiet.setHoaDonId((int) hoaDonId);
            }
            chiTietHoaDonDao.insertAll(chiTietList);
        });
    }
    
    public void update(HoaDon hoaDon) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            hoaDonDao.update(hoaDon);
        });
    }
    
    public void delete(HoaDon hoaDon) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            hoaDonDao.delete(hoaDon);
        });
    }
    
    public void deleteById(int id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            hoaDonDao.deleteById(id);
        });
    }
    
    public void thanhToan(int hoaDonId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            HoaDon hoaDon = hoaDonDao.getHoaDonByIdSync(hoaDonId);
            if (hoaDon != null) {
                hoaDon.setTrangThai(HoaDon.TRANG_THAI_DA_THANH_TOAN);
                hoaDon.setNgayThanhToan(System.currentTimeMillis());
                hoaDonDao.update(hoaDon);
            }
        });
    }
}
