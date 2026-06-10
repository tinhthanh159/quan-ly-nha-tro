package com.example.quan_ly_tro.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho một phòng trọ
 */
@Entity(tableName = "phong")
public class Phong {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String cloudId;          // ID duy nhất để đồng bộ giữa các máy (UUID)
    private String soPhong;          // Số phòng: P01, P02...
    private String loaiPhong;        // Loại: Đơn, Đôi, Studio
    private double giaThue;          // Giá thuê/tháng
    private double dienTich;         // Diện tích (m²)
    private String trangThai;        // Trống, Đang thuê, Đang sửa
    private String moTa;             // Mô tả thêm
    private long ngayTao;            // Timestamp tạo phòng
    
    // Trạng thái constants
    public static final String TRANG_THAI_TRONG = "Trống";
    public static final String TRANG_THAI_DANG_THUE = "Đang thuê";
    public static final String TRANG_THAI_DANG_SUA = "Đang sửa";
    
    // Loại phòng constants
    public static final String LOAI_PHONG_DON = "Phòng đơn";
    public static final String LOAI_PHONG_DOI = "Phòng đôi";
    public static final String LOAI_PHONG_STUDIO = "Studio";
    
    public Phong() {
        this.trangThai = TRANG_THAI_TRONG;
        this.ngayTao = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public String getSoPhong() {
        return soPhong;
    }

    public void setSoPhong(String soPhong) {
        this.soPhong = soPhong;
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public double getGiaThue() {
        return giaThue;
    }

    public void setGiaThue(double giaThue) {
        this.giaThue = giaThue;
    }

    public double getDienTich() {
        return dienTich;
    }

    public void setDienTich(double dienTich) {
        this.dienTich = dienTich;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public long getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(long ngayTao) {
        this.ngayTao = ngayTao;
    }
}
