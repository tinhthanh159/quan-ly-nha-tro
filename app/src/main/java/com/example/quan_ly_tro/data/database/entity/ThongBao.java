package com.example.quan_ly_tro.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho thông báo chung từ chủ trọ
 */
@Entity(tableName = "thong_bao")
public class ThongBao {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String tieuDe;
    private String noiDung;
    private long ngayTao;
    private String nguoiGui; // Tên chủ trọ
    private boolean isRead = false; // Trạng thái đã đọc (local)
    
    public ThongBao() {}
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    
    public long getNgayTao() { return ngayTao; }
    public void setNgayTao(long ngayTao) { this.ngayTao = ngayTao; }
    
    public String getNguoiGui() { return nguoiGui; }
    public void setNguoiGui(String nguoiGui) { this.nguoiGui = nguoiGui; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
