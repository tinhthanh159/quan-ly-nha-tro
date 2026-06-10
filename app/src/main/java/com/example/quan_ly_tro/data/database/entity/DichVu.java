package com.example.quan_ly_tro.data.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho các dịch vụ (Điện, Nước, Wifi, etc.)
 */
@Entity(tableName = "dich_vu")
public class DichVu {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String tenDichVu;        // Tên: Điện, Nước, Wifi...
    private double donGia;           // Đơn giá
    private String donViTinh;        // Đơn vị: kWh, m³, tháng
    private boolean batBuoc;         // Dịch vụ bắt buộc hay không
    private boolean tinhTheoChiso;   // Tính theo chỉ số (điện/nước) hay cố định
    
    // Dịch vụ mặc định constants
    public static final String DICH_VU_DIEN = "Điện";
    public static final String DICH_VU_NUOC = "Nước";
    public static final String DICH_VU_WIFI = "Wifi";
    public static final String DICH_VU_RAC = "Rác";
    public static final String DICH_VU_GIU_XE = "Giữ xe";
    
    public DichVu() {
        this.batBuoc = false;
        this.tinhTheoChiso = false;
    }
    
    // Constructor với tham số
    @Ignore
    public DichVu(String tenDichVu, double donGia, String donViTinh, boolean batBuoc, boolean tinhTheoChiso) {
        this.tenDichVu = tenDichVu;
        this.donGia = donGia;
        this.donViTinh = donViTinh;
        this.batBuoc = batBuoc;
        this.tinhTheoChiso = tinhTheoChiso;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public boolean isBatBuoc() {
        return batBuoc;
    }

    public void setBatBuoc(boolean batBuoc) {
        this.batBuoc = batBuoc;
    }

    public boolean isTinhTheoChiso() {
        return tinhTheoChiso;
    }

    public void setTinhTheoChiso(boolean tinhTheoChiso) {
        this.tinhTheoChiso = tinhTheoChiso;
    }
}
