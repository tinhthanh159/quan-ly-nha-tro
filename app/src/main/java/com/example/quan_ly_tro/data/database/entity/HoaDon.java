package com.example.quan_ly_tro.data.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho hóa đơn hàng tháng
 */
@Entity(tableName = "hoa_don")
public class HoaDon {
    
    // Trạng thái thanh toán constants
    public static final String TRANG_THAI_CHUA_THANH_TOAN = "Chưa thanh toán";
    public static final String TRANG_THAI_DA_THANH_TOAN = "Đã thanh toán";
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String cloudId;          // ID duy nhất để đồng bộ giữa các máy (UUID)
    private int phongId;             // FK đến Phong
    private Integer khachThueId;     // FK đến KhachThue (nullable)
    private String thangNam;         // Tháng/Năm: "12/2024"
    private double tienPhong;        // Tiền phòng
    private double tongTienDichVu;   // Tổng tiền dịch vụ
    private double tongTien;         // Tổng cộng = tiền phòng + dịch vụ
    private String trangThai;        // Trạng thái: Chưa thanh toán / Đã thanh toán
    private long ngayTao;            // Ngày tạo hóa đơn
    private Long ngayThanhToan;      // Ngày thanh toán (nullable)
    private String ghiChu;           // Ghi chú
    
    public HoaDon() {
        this.trangThai = TRANG_THAI_CHUA_THANH_TOAN;
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

    public int getPhongId() {
        return phongId;
    }

    public void setPhongId(int phongId) {
        this.phongId = phongId;
    }

    public Integer getKhachThueId() {
        return khachThueId;
    }

    public void setKhachThueId(Integer khachThueId) {
        this.khachThueId = khachThueId;
    }

    public String getThangNam() {
        return thangNam;
    }

    public void setThangNam(String thangNam) {
        this.thangNam = thangNam;
    }

    public double getTienPhong() {
        return tienPhong;
    }

    public void setTienPhong(double tienPhong) {
        this.tienPhong = tienPhong;
    }

    public double getTongTienDichVu() {
        return tongTienDichVu;
    }

    public void setTongTienDichVu(double tongTienDichVu) {
        this.tongTienDichVu = tongTienDichVu;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
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

    public Long getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(Long ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    
    // Helper method để tính tổng tiền
    public void tinhTongTien() {
        this.tongTien = this.tienPhong + this.tongTienDichVu;
    }
}
