package com.example.quan_ly_tro.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity đại diện cho người dùng hệ thống (Chủ trọ hoặc Khách thuê)
 */
@Entity(tableName = "users")
public class User {
    
    public static final String ROLE_LANDLORD = "LANDLORD";
    public static final String ROLE_TENANT = "TENANT";
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String uid; // Firebase UID
    private String email;
    private String hoTen;
    private String soDienThoai;
    private String role; // LANDLORD hoặc TENANT
    private Integer phongId; // Chỉ dành cho TENANT
    
    public User() {}
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Integer getPhongId() { return phongId; }
    public void setPhongId(Integer phongId) { this.phongId = phongId; }
}
