package com.example.quan_ly_tro.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_tro.data.database.entity.SuCo;

import java.util.List;

@Dao
public interface SuCoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SuCo suCo);

    @Update
    void update(SuCo suCo);

    @Delete
    void delete(SuCo suCo);

    @Query("SELECT * FROM su_co ORDER BY ngayTao DESC")
    LiveData<List<SuCo>> getAllSuCo();

    @Query("SELECT * FROM su_co ORDER BY ngayTao DESC")
    List<SuCo> getAllSuCoSync();

    @Query("SELECT * FROM su_co WHERE phongId = :phongId ORDER BY ngayTao DESC")
    LiveData<List<SuCo>> getSuCoByPhong(int phongId);

    @Query("DELETE FROM su_co")
    void deleteAll();
}
