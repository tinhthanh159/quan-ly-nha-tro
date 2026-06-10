package com.example.quan_ly_tro.data.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho hợp đồng thuê phòng
 */
@Entity(tableName = "hop_dong")
public class HopDong {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String cloudId;      // ID duy nhất để đồng bộ giữa các máy
    private int phongId;
    private int khachThueId;
    
    private String ngayBatDau;   // Format: dd/MM/yyyy
    private String ngayKetThuc;  // Format: dd/MM/yyyy
    
    private double tienCoc;       // Số tiền đặt cọc
    private double giaThue;       // Giá thuê tại thời điểm ký
    
    private String noiDung;       // Nội dung/điều khoản bổ sung
    private String trangThai;     // ACTIVE, EXPIRED, TERMINATED
    
    private long ngayTao;         // Timestamp
    
    // Trạng thái constants
    public static final String TRANG_THAI_ACTIVE = "Đang hiệu lực";
    public static final String TRANG_THAI_EXPIRED = "Hết hạn";
    public static final String TRANG_THAI_CANCELLED = "Đã hủy";
    public static final String TRANG_THAI_WAITING_FOR_TENANT = "Chờ khách ký";
    
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
    
    public int getPhongId() {
        return phongId;
    }
    
    public void setPhongId(int phongId) {
        this.phongId = phongId;
    }
    
    public int getKhachThueId() {
        return khachThueId;
    }
    
    public void setKhachThueId(int khachThueId) {
        this.khachThueId = khachThueId;
    }
    
    public String getNgayBatDau() {
        return ngayBatDau;
    }
    
    public void setNgayBatDau(String ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }
    
    public String getNgayKetThuc() {
        return ngayKetThuc;
    }
    
    public void setNgayKetThuc(String ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }
    
    public double getTienCoc() {
        return tienCoc;
    }
    
    public void setTienCoc(double tienCoc) {
        this.tienCoc = tienCoc;
    }
    
    public double getGiaThue() {
        return giaThue;
    }
    
    public void setGiaThue(double giaThue) {
        this.giaThue = giaThue;
    }
    
    public String getNoiDung() {
        return noiDung;
    }
    
    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }
    
    public String getTrangThai() {
        return trangThai;
    }
    
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    public long getNgayTao() {
        return ngayTao;
    }
    
    public void setNgayTao(long ngayTao) {
        this.ngayTao = ngayTao;
    }
}
