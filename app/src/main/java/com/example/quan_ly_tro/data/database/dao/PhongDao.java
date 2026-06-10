package com.example.quan_ly_tro.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_tro.data.database.entity.Phong;

import java.util.List;

/**
 * DAO cho thao tác với bảng Phong
 */
@Dao
public interface PhongDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Phong phong);
    
    @Update
    void update(Phong phong);
    
    @Delete
    void delete(Phong phong);
    
    @Query("SELECT * FROM phong ORDER BY soPhong ASC")
    LiveData<List<Phong>> getAllPhong();
    
    @Query("SELECT * FROM phong ORDER BY soPhong ASC")
    List<Phong> getAllPhongSync();
    
    @Query("SELECT * FROM phong WHERE id = :id")
    LiveData<Phong> getPhongById(int id);
    
    @Query("SELECT * FROM phong WHERE id = :id")
    Phong getPhongByIdSync(int id);
    
    @Query("SELECT * FROM phong WHERE trangThai = :trangThai ORDER BY soPhong ASC")
    LiveData<List<Phong>> getPhongByTrangThai(String trangThai);
    
    @Query("SELECT COUNT(*) FROM phong")
    LiveData<Integer> getTongSoPhong();
    
    @Query("SELECT COUNT(*) FROM phong WHERE trangThai = :trangThai")
    LiveData<Integer> getSoPhongByTrangThai(String trangThai);
    
    @Query("SELECT COUNT(*) FROM phong WHERE trangThai = 'Trống'")
    LiveData<Integer> getSoPhongTrong();
    
    @Query("SELECT COUNT(*) FROM phong WHERE trangThai = 'Đang thuê'")
    LiveData<Integer> getSoPhongDangThue();
    
    @Query("SELECT * FROM phong WHERE soPhong LIKE '%' || :keyword || '%' ORDER BY soPhong ASC")
    LiveData<List<Phong>> searchPhong(String keyword);
    
    @Query("DELETE FROM phong WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM phong")
    void deleteAll();
}
