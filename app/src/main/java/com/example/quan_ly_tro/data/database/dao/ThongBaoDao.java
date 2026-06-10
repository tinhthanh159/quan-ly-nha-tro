package com.example.quan_ly_tro.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_tro.data.database.entity.ThongBao;

import java.util.List;

@Dao
public interface ThongBaoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ThongBao thongBao);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertIgnore(ThongBao thongBao);

    @Update
    void update(ThongBao thongBao);

    @Delete
    void delete(ThongBao thongBao);

    @Query("SELECT * FROM thong_bao ORDER BY ngayTao DESC")
    LiveData<List<ThongBao>> getAllThongBao();

    @Query("SELECT * FROM thong_bao ORDER BY ngayTao DESC")
    List<ThongBao> getAllThongBaoSync();

    @Query("DELETE FROM thong_bao")
    void deleteAll();
}
