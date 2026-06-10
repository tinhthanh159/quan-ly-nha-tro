package com.example.quan_ly_tro.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_tro.data.database.entity.KhachThue;

import java.util.List;

/**
 * DAO cho thao tác với bảng KhachThue
 */
@Dao
public interface KhachThueDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(KhachThue khachThue);
    
    @Update
    void update(KhachThue khachThue);
    
    @Delete
    void delete(KhachThue khachThue);
    
    @Query("SELECT * FROM khach_thue ORDER BY hoTen ASC")
    LiveData<List<KhachThue>> getAllKhachThue();
    
    @Query("SELECT * FROM khach_thue ORDER BY hoTen ASC")
    List<KhachThue> getAllKhachThueSync();
    
    @Query("SELECT * FROM khach_thue WHERE id = :id")
    LiveData<KhachThue> getKhachThueById(int id);
    
    @Query("SELECT * FROM khach_thue WHERE id = :id")
    KhachThue getKhachThueByIdSync(int id);
    
    @Query("SELECT * FROM khach_thue WHERE phongId = :phongId AND dangThue = 1")
    LiveData<List<KhachThue>> getKhachThueByPhong(int phongId);
    
    @Query("SELECT * FROM khach_thue WHERE phongId = :phongId AND dangThue = 1")
    List<KhachThue> getKhachThueByPhongSync(int phongId);
    
    @Query("SELECT * FROM khach_thue WHERE dangThue = 1 ORDER BY hoTen ASC")
    LiveData<List<KhachThue>> getKhachThueDangThue();
    
    @Query("SELECT * FROM khach_thue WHERE dangThue = 0 ORDER BY ngayRa DESC")
    LiveData<List<KhachThue>> getKhachThueDaRa();
    
    @Query("SELECT COUNT(*) FROM khach_thue WHERE dangThue = 1")
    LiveData<Integer> getTongSoKhachDangThue();
    
    @Query("SELECT * FROM khach_thue WHERE hoTen LIKE '%' || :keyword || '%' OR soDienThoai LIKE '%' || :keyword || '%' OR cccd LIKE '%' || :keyword || '%' ORDER BY hoTen ASC")
    LiveData<List<KhachThue>> searchKhachThue(String keyword);
    
    @Query("DELETE FROM khach_thue WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM khach_thue WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    KhachThue getKhachThueByEmailSync(String email);

    @Query("DELETE FROM khach_thue")
    void deleteAll();
}
