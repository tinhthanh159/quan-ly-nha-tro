package com.example.quan_ly_tro.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_tro.data.database.entity.HopDong;

import java.util.List;

/**
 * DAO cho thao tác với bảng hợp đồng
 */
@Dao
public interface HopDongDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HopDong hopDong);
    
    @Update
    void update(HopDong hopDong);
    
    @Delete
    void delete(HopDong hopDong);
    
    @Query("SELECT * FROM hop_dong ORDER BY ngayTao DESC")
    LiveData<List<HopDong>> getAllHopDong();

    @Query("SELECT * FROM hop_dong ORDER BY ngayTao DESC")
    List<HopDong> getAllHopDongSync();
    
    @Query("SELECT * FROM hop_dong WHERE id = :id")
    LiveData<HopDong> getHopDongById(int id);
    
    @Query("SELECT * FROM hop_dong WHERE id = :id")
    HopDong getHopDongByIdSync(int id);
    
    @Query("SELECT * FROM hop_dong WHERE phongId = :phongId ORDER BY ngayTao DESC LIMIT 1")
    LiveData<HopDong> getHopDongByPhongId(int phongId);
    
    @Query("SELECT * FROM hop_dong WHERE khachThueId = :khachThueId ORDER BY ngayTao DESC")
    LiveData<List<HopDong>> getHopDongByKhachThueId(int khachThueId);
    
    @Query("SELECT * FROM hop_dong WHERE trangThai = :trangThai ORDER BY ngayTao DESC")
    LiveData<List<HopDong>> getHopDongByTrangThai(String trangThai);
    
    @Query("SELECT COUNT(*) FROM hop_dong WHERE trangThai = :trangThai")
    LiveData<Integer> countByTrangThai(String trangThai);

    @Query("DELETE FROM hop_dong")
    void deleteAll();
}
