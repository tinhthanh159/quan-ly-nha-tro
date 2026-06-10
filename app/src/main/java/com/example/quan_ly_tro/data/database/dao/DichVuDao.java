package com.example.quan_ly_tro.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_tro.data.database.entity.DichVu;

import java.util.List;

/**
 * DAO cho thao tác với bảng DichVu
 */
@Dao
public interface DichVuDao {
    
    @Insert
    long insert(DichVu dichVu);
    
    @Insert
    void insertAll(List<DichVu> dichVuList);
    
    @Update
    void update(DichVu dichVu);
    
    @Delete
    void delete(DichVu dichVu);
    
    @Query("SELECT * FROM dich_vu ORDER BY tenDichVu ASC")
    LiveData<List<DichVu>> getAllDichVu();
    
    @Query("SELECT * FROM dich_vu ORDER BY tenDichVu ASC")
    List<DichVu> getAllDichVuSync();
    
    @Query("SELECT * FROM dich_vu WHERE id = :id")
    LiveData<DichVu> getDichVuById(int id);
    
    @Query("SELECT * FROM dich_vu WHERE id = :id")
    DichVu getDichVuByIdSync(int id);
    
    @Query("SELECT * FROM dich_vu WHERE batBuoc = 1")
    LiveData<List<DichVu>> getDichVuBatBuoc();
    
    @Query("SELECT * FROM dich_vu WHERE tinhTheoChiso = 1")
    LiveData<List<DichVu>> getDichVuTheoChiSo();
    
    @Query("SELECT COUNT(*) FROM dich_vu")
    int getCount();
    
    @Query("DELETE FROM dich_vu WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM dich_vu")
    void deleteAll();
}
