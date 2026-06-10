package com.example.quan_ly_tro.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_tro.data.database.entity.ChiTietHoaDon;

import java.util.List;

/**
 * DAO cho thao tác với bảng ChiTietHoaDon
 */
@Dao
public interface ChiTietHoaDonDao {
    
    @Insert
    long insert(ChiTietHoaDon chiTiet);
    
    @Insert
    void insertAll(List<ChiTietHoaDon> chiTietList);
    
    @Update
    void update(ChiTietHoaDon chiTiet);
    
    @Delete
    void delete(ChiTietHoaDon chiTiet);
    
    @Query("SELECT * FROM chi_tiet_hoa_don WHERE hoaDonId = :hoaDonId")
    LiveData<List<ChiTietHoaDon>> getChiTietByHoaDon(int hoaDonId);
    
    @Query("SELECT * FROM chi_tiet_hoa_don WHERE hoaDonId = :hoaDonId")
    List<ChiTietHoaDon> getChiTietByHoaDonSync(int hoaDonId);
    
    @Query("DELETE FROM chi_tiet_hoa_don WHERE hoaDonId = :hoaDonId")
    void deleteByHoaDon(int hoaDonId);
    
    @Query("SELECT SUM(thanhTien) FROM chi_tiet_hoa_don WHERE hoaDonId = :hoaDonId")
    double getTongTienDichVu(int hoaDonId);

    @Query("DELETE FROM chi_tiet_hoa_don")
    void deleteAll();
}
