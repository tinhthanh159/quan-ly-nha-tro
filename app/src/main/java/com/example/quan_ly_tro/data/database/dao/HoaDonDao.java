package com.example.quan_ly_tro.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_tro.data.database.entity.HoaDon;

import java.util.List;

/**
 * DAO cho thao tác với bảng HoaDon
 */
@Dao
public interface HoaDonDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HoaDon hoaDon);
    
    @Update
    void update(HoaDon hoaDon);
    
    @Delete
    void delete(HoaDon hoaDon);
    
    @Query("SELECT * FROM hoa_don ORDER BY ngayTao DESC")
    LiveData<List<HoaDon>> getAllHoaDon();
    
    @Query("SELECT * FROM hoa_don ORDER BY ngayTao DESC")
    List<HoaDon> getAllHoaDonSync();
    
    @Query("SELECT * FROM hoa_don WHERE id = :id")
    LiveData<HoaDon> getHoaDonById(int id);
    
    @Query("SELECT * FROM hoa_don WHERE id = :id")
    HoaDon getHoaDonByIdSync(int id);
    
    @Query("SELECT * FROM hoa_don WHERE phongId = :phongId ORDER BY ngayTao DESC")
    LiveData<List<HoaDon>> getHoaDonByPhong(int phongId);
    
    @Query("SELECT * FROM hoa_don WHERE thangNam = :thangNam ORDER BY phongId ASC")
    LiveData<List<HoaDon>> getHoaDonByThangNam(String thangNam);
    
    @Query("SELECT * FROM hoa_don WHERE trangThai = 'Chưa thanh toán' ORDER BY ngayTao ASC")
    LiveData<List<HoaDon>> getHoaDonChuaThanhToan();
    
    @Query("SELECT * FROM hoa_don WHERE trangThai = 'Đã thanh toán' ORDER BY ngayThanhToan DESC")
    LiveData<List<HoaDon>> getHoaDonDaThanhToan();
    
    @Query("SELECT * FROM hoa_don WHERE (:phongId = 0 OR phongId = :phongId) AND (:trangThai = '' OR trangThai = :trangThai) ORDER BY ngayTao DESC")
    LiveData<List<HoaDon>> getFilteredHoaDon(int phongId, String trangThai);

    @Query("SELECT COUNT(*) FROM hoa_don WHERE trangThai = 'Chưa thanh toán'")
    LiveData<Integer> getSoHoaDonChuaThanhToan();
    
    @Query("SELECT SUM(tongTien) FROM hoa_don WHERE trangThai = 'Chưa thanh toán'")
    LiveData<Double> getTongTienChuaThu();
    
    @Query("SELECT SUM(tongTien) FROM hoa_don WHERE trangThai = 'Đã thanh toán' AND thangNam = :thangNam")
    LiveData<Double> getDoanhThuThang(String thangNam);
    
    @Query("SELECT SUM(tongTien) FROM hoa_don WHERE trangThai = 'Đã thanh toán'")
    LiveData<Double> getTongDoanhThu();
    
    @Query("SELECT * FROM hoa_don WHERE trangThai = 'Chưa thanh toán' ORDER BY ngayTao ASC")
    List<HoaDon> getHoaDonChuaThanhToanSync();
    
    @Query("DELETE FROM hoa_don WHERE id = :id")
    void deleteById(int id);
    
    @Query("SELECT EXISTS(SELECT 1 FROM hoa_don WHERE phongId = :phongId AND thangNam = :thangNam)")
    boolean hoaDonExists(int phongId, String thangNam);

    @Query("DELETE FROM hoa_don")
    void deleteAll();
}
