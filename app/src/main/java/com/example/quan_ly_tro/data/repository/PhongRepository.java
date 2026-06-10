package com.example.quan_ly_tro.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.dao.PhongDao;
import com.example.quan_ly_tro.data.database.entity.Phong;

import java.util.List;

/**
 * Repository cho Phong - Abstract layer giữa ViewModel và Database
 */
public class PhongRepository {
    
    private final PhongDao phongDao;
    private final LiveData<List<Phong>> allPhong;
    
    public PhongRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        phongDao = db.phongDao();
        allPhong = phongDao.getAllPhong();
    }
    
    // Lấy tất cả phòng
    public LiveData<List<Phong>> getAllPhong() {
        return allPhong;
    }
    
    // Lấy phòng theo ID
    public LiveData<Phong> getPhongById(int id) {
        return phongDao.getPhongById(id);
    }
    
    // Lấy phòng theo trạng thái
    public LiveData<List<Phong>> getPhongByTrangThai(String trangThai) {
        return phongDao.getPhongByTrangThai(trangThai);
    }
    
    // Tìm kiếm phòng
    public LiveData<List<Phong>> searchPhong(String keyword) {
        return phongDao.searchPhong(keyword);
    }
    
    // Thống kê
    public LiveData<Integer> getTongSoPhong() {
        return phongDao.getTongSoPhong();
    }
    
    public LiveData<Integer> getSoPhongTrong() {
        return phongDao.getSoPhongTrong();
    }
    
    public LiveData<Integer> getSoPhongDangThue() {
        return phongDao.getSoPhongDangThue();
    }
    
    public LiveData<Integer> getSoPhongByTrangThai(String trangThai) {
        return phongDao.getSoPhongByTrangThai(trangThai);
    }
    
    // Thêm phòng mới
    public void insert(Phong phong) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            phongDao.insert(phong);
        });
    }
    
    // Cập nhật phòng
    public void update(Phong phong) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            phongDao.update(phong);
        });
    }
    
    // Xóa phòng
    public void delete(Phong phong) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            phongDao.delete(phong);
        });
    }
    
    // Xóa theo ID
    public void deleteById(int id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            phongDao.deleteById(id);
        });
    }
}
