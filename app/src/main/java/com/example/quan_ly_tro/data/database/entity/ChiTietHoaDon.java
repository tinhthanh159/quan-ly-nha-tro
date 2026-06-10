package com.example.quan_ly_tro.data.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho chi tiết hóa đơn (từng dịch vụ trong hóa đơn)
 */
@Entity(tableName = "chi_tiet_hoa_don",
        foreignKeys = {
            @ForeignKey(
                entity = HoaDon.class,
                parentColumns = "id",
                childColumns = "hoaDonId",
                onDelete = ForeignKey.CASCADE
            ),
            @ForeignKey(
                entity = DichVu.class,
                parentColumns = "id",
                childColumns = "dichVuId",
                onDelete = ForeignKey.CASCADE
            )
        },
        indices = {@Index("hoaDonId"), @Index("dichVuId")})
public class ChiTietHoaDon {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int hoaDonId;            // FK đến HoaDon
    private int dichVuId;            // FK đến DichVu
    private String tenDichVu;        // Cache tên dịch vụ
    private Integer chiSoCu;         // Chỉ số cũ (cho điện/nước)
    private Integer chiSoMoi;        // Chỉ số mới
    private int soLuong;             // Số lượng (= chiSoMoi - chiSoCu hoặc 1)
    private double donGia;           // Đơn giá tại thời điểm lập hóa đơn
    private double thanhTien;        // Thành tiền = soLuong * donGia
    
    public ChiTietHoaDon() {
        this.soLuong = 1;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHoaDonId() {
        return hoaDonId;
    }

    public void setHoaDonId(int hoaDonId) {
        this.hoaDonId = hoaDonId;
    }

    public int getDichVuId() {
        return dichVuId;
    }

    public void setDichVuId(int dichVuId) {
        this.dichVuId = dichVuId;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public Integer getChiSoCu() {
        return chiSoCu;
    }

    public void setChiSoCu(Integer chiSoCu) {
        this.chiSoCu = chiSoCu;
    }

    public Integer getChiSoMoi() {
        return chiSoMoi;
    }

    public void setChiSoMoi(Integer chiSoMoi) {
        this.chiSoMoi = chiSoMoi;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }
    
    // Helper method để tính thành tiền
    public void tinhThanhTien() {
        if (chiSoCu != null && chiSoMoi != null) {
            this.soLuong = chiSoMoi - chiSoCu;
        }
        this.thanhTien = this.soLuong * this.donGia;
    }
}
