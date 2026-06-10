package com.example.quan_ly_tro.data.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho các khoản thu/chi
 */
@Entity(tableName = "thu_chi",
        foreignKeys = @ForeignKey(
            entity = Phong.class,
            parentColumns = "id",
            childColumns = "phongId",
            onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index("phongId")})
public class ThuChi {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String loai;             // "THU" hoặc "CHI"
    private String danhMuc;          // Danh mục: Tiền thuê, Sửa chữa, Bảo trì...
    private double soTien;           // Số tiền
    private String moTa;             // Mô tả chi tiết
    private long ngayGiaoDich;       // Ngày giao dịch
    private Integer phongId;         // FK đến Phong (nullable, nếu liên quan đến phòng cụ thể)
    private long ngayTao;            // Timestamp tạo
    
    // Loại giao dịch constants
    public static final String LOAI_THU = "THU";
    public static final String LOAI_CHI = "CHI";
    
    // Danh mục thu
    public static final String DANH_MUC_TIEN_THUE = "Tiền thuê phòng";
    public static final String DANH_MUC_TIEN_COC = "Tiền cọc";
    public static final String DANH_MUC_TIEN_DICH_VU = "Tiền dịch vụ";
    public static final String DANH_MUC_THU_KHAC = "Thu khác";
    
    // Danh mục chi
    public static final String DANH_MUC_SUA_CHUA = "Sửa chữa";
    public static final String DANH_MUC_BAO_TRI = "Bảo trì";
    public static final String DANH_MUC_THIET_BI = "Mua thiết bị";
    public static final String DANH_MUC_DIEN_NUOC = "Điện nước chung";
    public static final String DANH_MUC_CHI_KHAC = "Chi khác";
    
    public ThuChi() {
        this.ngayGiaoDich = System.currentTimeMillis();
        this.ngayTao = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public String getDanhMuc() {
        return danhMuc;
    }

    public void setDanhMuc(String danhMuc) {
        this.danhMuc = danhMuc;
    }

    public double getSoTien() {
        return soTien;
    }

    public void setSoTien(double soTien) {
        this.soTien = soTien;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public long getNgayGiaoDich() {
        return ngayGiaoDich;
    }

    public void setNgayGiaoDich(long ngayGiaoDich) {
        this.ngayGiaoDich = ngayGiaoDich;
    }

    public Integer getPhongId() {
        return phongId;
    }

    public void setPhongId(Integer phongId) {
        this.phongId = phongId;
    }

    public long getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(long ngayTao) {
        this.ngayTao = ngayTao;
    }
    
    // Helper methods
    public boolean isThu() {
        return LOAI_THU.equals(this.loai);
    }
    
    public boolean isChi() {
        return LOAI_CHI.equals(this.loai);
    }
}
