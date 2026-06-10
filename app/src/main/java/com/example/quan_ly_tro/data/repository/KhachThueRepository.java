package com.example.quan_ly_tro.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.dao.KhachThueDao;
import com.example.quan_ly_tro.data.database.entity.KhachThue;

import java.util.List;

/**
 * Repository cho KhachThue
 */
public class KhachThueRepository {
    
    private final KhachThueDao khachThueDao;
    private final LiveData<List<KhachThue>> allKhachThue;
    
    public KhachThueRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        khachThueDao = db.khachThueDao();
        allKhachThue = khachThueDao.getAllKhachThue();
    }
    
    public LiveData<List<KhachThue>> getAllKhachThue() {
        return allKhachThue;
    }
    
    public LiveData<KhachThue> getKhachThueById(int id) {
        return khachThueDao.getKhachThueById(id);
    }
    
    public LiveData<List<KhachThue>> getKhachThueByPhong(int phongId) {
        return khachThueDao.getKhachThueByPhong(phongId);
    }
    
    public LiveData<List<KhachThue>> getKhachThueDangThue() {
        return khachThueDao.getKhachThueDangThue();
    }
    
    public LiveData<List<KhachThue>> getKhachThueDaRa() {
        return khachThueDao.getKhachThueDaRa();
    }
    
    public LiveData<Integer> getTongSoKhachDangThue() {
        return khachThueDao.getTongSoKhachDangThue();
    }
    
    public LiveData<List<KhachThue>> searchKhachThue(String keyword) {
        return khachThueDao.searchKhachThue(keyword);
    }
    
    public void insert(KhachThue khachThue) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            khachThueDao.insert(khachThue);
        });
    }
    
    public void update(KhachThue khachThue) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            khachThueDao.update(khachThue);
        });
    }
    
    public void delete(KhachThue khachThue) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            khachThueDao.delete(khachThue);
        });
    }
    
    public void deleteById(int id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            khachThueDao.deleteById(id);
        });
    }
}
