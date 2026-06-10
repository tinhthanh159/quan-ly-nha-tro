package com.example.quan_ly_tro.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_tro.data.database.entity.ThuChi;

import java.util.List;

/**
 * DAO cho thao tác với bảng ThuChi
 */
@Dao
public interface ThuChiDao {
    
    @Insert
    long insert(ThuChi thuChi);
    
    @Update
    void update(ThuChi thuChi);
    
    @Delete
    void delete(ThuChi thuChi);
    
    @Query("SELECT * FROM thu_chi ORDER BY ngayGiaoDich DESC")
    LiveData<List<ThuChi>> getAllThuChi();
    
    @Query("SELECT * FROM thu_chi ORDER BY ngayGiaoDich DESC")
    List<ThuChi> getAllThuChiSync();
    
    @Query("SELECT * FROM thu_chi WHERE id = :id")
    LiveData<ThuChi> getThuChiById(int id);
    
    @Query("SELECT * FROM thu_chi WHERE loai = :loai ORDER BY ngayGiaoDich DESC")
    LiveData<List<ThuChi>> getThuChiByLoai(String loai);
    
    @Query("SELECT * FROM thu_chi WHERE loai = 'THU' ORDER BY ngayGiaoDich DESC")
    LiveData<List<ThuChi>> getAllThu();
    
    @Query("SELECT * FROM thu_chi WHERE loai = 'CHI' ORDER BY ngayGiaoDich DESC")
    LiveData<List<ThuChi>> getAllChi();
    
    @Query("SELECT * FROM thu_chi WHERE phongId = :phongId ORDER BY ngayGiaoDich DESC")
    LiveData<List<ThuChi>> getThuChiByPhong(int phongId);
    
    @Query("SELECT SUM(soTien) FROM thu_chi WHERE loai = 'THU'")
    LiveData<Double> getTongThu();
    
    @Query("SELECT SUM(soTien) FROM thu_chi WHERE loai = 'CHI'")
    LiveData<Double> getTongChi();
    
    @Query("SELECT SUM(CASE WHEN loai = 'THU' THEN soTien ELSE -soTien END) FROM thu_chi")
    LiveData<Double> getSoDu();
    
    @Query("SELECT SUM(soTien) FROM thu_chi WHERE loai = 'THU' AND ngayGiaoDich BETWEEN :startTime AND :endTime")
    LiveData<Double> getTongThuTrongKhoang(long startTime, long endTime);
    
    @Query("SELECT SUM(soTien) FROM thu_chi WHERE loai = 'CHI' AND ngayGiaoDich BETWEEN :startTime AND :endTime")
    LiveData<Double> getTongChiTrongKhoang(long startTime, long endTime);
    
    @Query("DELETE FROM thu_chi WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM thu_chi")
    void deleteAll();
}
