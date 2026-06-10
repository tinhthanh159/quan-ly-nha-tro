package com.example.quan_ly_tro.data.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho khách thuê trọ
 */
@Entity(tableName = "khach_thue")
public class KhachThue {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String cloudId;          // ID duy nhất để đồng bộ giữa các máy (UUID)
    private Integer phongId;         // FK đến Phong (nullable khi chưa thuê)
    private String hoTen;            // Họ và tên
    private String cccd;             // Căn cước công dân
    private String soDienThoai;      // Số điện thoại
    private String email;            // Email
    private String queQuan;          // Quê quán
    private String ngheNghiep;       // Nghề nghiệp
    private long ngayVao;            // Ngày bắt đầu thuê
    private Long ngayRa;             // Ngày trả phòng (nullable)
    private boolean dangThue;        // Đang thuê hay đã trả
    private String ghiChu;           // Ghi chú thêm
    private long ngayTao;            // Timestamp tạo
    
    public KhachThue() {
        this.dangThue = true;
        this.ngayTao = System.currentTimeMillis();
        this.ngayVao = System.currentTimeMillis();
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

    public Integer getPhongId() {
        return phongId;
    }

    public void setPhongId(Integer phongId) {
        this.phongId = phongId;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQueQuan() {
        return queQuan;
    }

    public void setQueQuan(String queQuan) {
        this.queQuan = queQuan;
    }

    public String getNgheNghiep() {
        return ngheNghiep;
    }

    public void setNgheNghiep(String ngheNghiep) {
        this.ngheNghiep = ngheNghiep;
    }

    public long getNgayVao() {
        return ngayVao;
    }

    public void setNgayVao(long ngayVao) {
        this.ngayVao = ngayVao;
    }

    public Long getNgayRa() {
        return ngayRa;
    }

    public void setNgayRa(Long ngayRa) {
        this.ngayRa = ngayRa;
    }

    public boolean isDangThue() {
        return dangThue;
    }

    public void setDangThue(boolean dangThue) {
        this.dangThue = dangThue;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public long getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(long ngayTao) {
        this.ngayTao = ngayTao;
    }
}
