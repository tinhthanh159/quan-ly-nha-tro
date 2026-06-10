package com.example.quan_ly_tro.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.dao.ThuChiDao;
import com.example.quan_ly_tro.data.database.entity.ThuChi;

import java.util.List;

/**
 * Repository cho ThuChi
 */
public class ThuChiRepository {
    
    private final ThuChiDao thuChiDao;
    private final LiveData<List<ThuChi>> allThuChi;
    
    public ThuChiRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        thuChiDao = db.thuChiDao();
        allThuChi = thuChiDao.getAllThuChi();
    }
    
    public LiveData<List<ThuChi>> getAllThuChi() {
        return allThuChi;
    }
    
    public LiveData<ThuChi> getThuChiById(int id) {
        return thuChiDao.getThuChiById(id);
    }
    
    public LiveData<List<ThuChi>> getThuChiByLoai(String loai) {
        return thuChiDao.getThuChiByLoai(loai);
    }
    
    public LiveData<List<ThuChi>> getAllThu() {
        return thuChiDao.getAllThu();
    }
    
    public LiveData<List<ThuChi>> getAllChi() {
        return thuChiDao.getAllChi();
    }
    
    public LiveData<List<ThuChi>> getThuChiByPhong(int phongId) {
        return thuChiDao.getThuChiByPhong(phongId);
    }
    
    public LiveData<Double> getTongThu() {
        return thuChiDao.getTongThu();
    }
    
    public LiveData<Double> getTongChi() {
        return thuChiDao.getTongChi();
    }
    
    public LiveData<Double> getSoDu() {
        return thuChiDao.getSoDu();
    }
    
    public LiveData<Double> getTongThuTrongKhoang(long startTime, long endTime) {
        return thuChiDao.getTongThuTrongKhoang(startTime, endTime);
    }
    
    public LiveData<Double> getTongChiTrongKhoang(long startTime, long endTime) {
        return thuChiDao.getTongChiTrongKhoang(startTime, endTime);
    }
    
    public void insert(ThuChi thuChi) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            thuChiDao.insert(thuChi);
        });
    }
    
    public void update(ThuChi thuChi) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            thuChiDao.update(thuChi);
        });
    }
    
    public void delete(ThuChi thuChi) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            thuChiDao.delete(thuChi);
        });
    }
    
    public void deleteById(int id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            thuChiDao.deleteById(id);
        });
    }
}
