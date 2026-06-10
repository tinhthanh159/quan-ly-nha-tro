package com.example.quan_ly_tro.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.dao.DichVuDao;
import com.example.quan_ly_tro.data.database.entity.DichVu;

import java.util.List;

/**
 * Repository cho DichVu
 */
public class DichVuRepository {
    
    private final DichVuDao dichVuDao;
    private final LiveData<List<DichVu>> allDichVu;
    
    public DichVuRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        dichVuDao = db.dichVuDao();
        allDichVu = dichVuDao.getAllDichVu();
    }
    
    public LiveData<List<DichVu>> getAllDichVu() {
        return allDichVu;
    }
    
    public LiveData<DichVu> getDichVuById(int id) {
        return dichVuDao.getDichVuById(id);
    }
    
    public LiveData<List<DichVu>> getDichVuBatBuoc() {
        return dichVuDao.getDichVuBatBuoc();
    }
    
    public LiveData<List<DichVu>> getDichVuTheoChiSo() {
        return dichVuDao.getDichVuTheoChiSo();
    }
    
    public void insert(DichVu dichVu) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            dichVuDao.insert(dichVu);
        });
    }
    
    public void update(DichVu dichVu) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            dichVuDao.update(dichVu);
        });
    }
    
    public void delete(DichVu dichVu) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            dichVuDao.delete(dichVu);
        });
    }
    
    public void deleteById(int id) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            dichVuDao.deleteById(id);
        });
    }
}
