package com.example.quan_ly_tro.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho Sự cố/Yêu cầu sửa chữa từ khách thuê
 */
@Entity(tableName = "su_co")
public class SuCo {
    
    public static final String STATUS_PENDING = "CHỜ XỬ LÝ";
    public static final String STATUS_IN_PROGRESS = "ĐANG SỬA";
    public static final String STATUS_DONE = "ĐÃ XONG";
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String cloudId; // ID duy nhất để đồng bộ giữa các máy (UUID)
    private int phongId;
    private String tenPhong;
    private String tieuDe;
    private String moTa;
    private String hinhAnh;
    private String trangThai;
    private long ngayTao;
    private Long ngayHoanThanh;
    
    public SuCo() {}
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCloudId() { return cloudId; }
    public void setCloudId(String cloudId) { this.cloudId = cloudId; }
    
    public int getPhongId() { return phongId; }
    public void setPhongId(int phongId) { this.phongId = phongId; }
    
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    
    public long getNgayTao() { return ngayTao; }
    public void setNgayTao(long ngayTao) { this.ngayTao = ngayTao; }
    
    public Long getNgayHoanThanh() { return ngayHoanThanh; }
    public void setNgayHoanThanh(Long ngayHoanThanh) { this.ngayHoanThanh = ngayHoanThanh; }
}
