package com.example.quan_ly_tro.ui.khachthue;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.repository.KhachThueRepository;
import com.example.quan_ly_tro.data.repository.PhongRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel cho quản lý Khách thuê với tìm kiếm
 */
public class KhachThueViewModel extends AndroidViewModel {
    
    private final KhachThueRepository khachThueRepository;
    private final PhongRepository phongRepository;
    private final LiveData<List<KhachThue>> allKhachThue;
    private final LiveData<List<Phong>> allPhong;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MediatorLiveData<List<KhachThue>> filteredKhachThue = new MediatorLiveData<>();
    
    public KhachThueViewModel(@NonNull Application application) {
        super(application);
        khachThueRepository = new KhachThueRepository(application);
        phongRepository = new PhongRepository(application);
        allKhachThue = khachThueRepository.getAllKhachThue();
        allPhong = phongRepository.getAllPhong();
        
        searchQuery.setValue("");
        
        // Combine search
        filteredKhachThue.addSource(allKhachThue, list -> applySearch(list));
        filteredKhachThue.addSource(searchQuery, query -> applySearch(allKhachThue.getValue()));
    }
    
    private void applySearch(List<KhachThue> khachThueList) {
        if (khachThueList == null) {
            filteredKhachThue.setValue(new ArrayList<>());
            return;
        }
        
        String query = searchQuery.getValue();
        if (query == null || query.isEmpty()) {
            filteredKhachThue.setValue(khachThueList);
            return;
        }
        
        List<KhachThue> result = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (KhachThue kt : khachThueList) {
            boolean matchesName = kt.getHoTen() != null 
                    && kt.getHoTen().toLowerCase().contains(lowerQuery);
            boolean matchesPhone = kt.getSoDienThoai() != null 
                    && kt.getSoDienThoai().contains(query);
            boolean matchesCccd = kt.getCccd() != null 
                    && kt.getCccd().contains(query);
            
            if (matchesName || matchesPhone || matchesCccd) {
                result.add(kt);
            }
        }
        
        filteredKhachThue.setValue(result);
    }
    
    public LiveData<List<KhachThue>> getAllKhachThue() {
        return allKhachThue;
    }
    
    public LiveData<List<KhachThue>> getFilteredKhachThue() {
        return filteredKhachThue;
    }
    
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }
    
    public LiveData<List<KhachThue>> getKhachThueDangThue() {
        return khachThueRepository.getKhachThueDangThue();
    }
    
    public LiveData<KhachThue> getKhachThueById(int id) {
        return khachThueRepository.getKhachThueById(id);
    }
    
    public LiveData<List<KhachThue>> getKhachThueByPhong(int phongId) {
        return khachThueRepository.getKhachThueByPhong(phongId);
    }
    
    public LiveData<List<Phong>> getAllPhong() {
        return allPhong;
    }
    
    public void insert(KhachThue khachThue) {
        khachThueRepository.insert(khachThue);
        
        // Cập nhật trạng thái phòng thành "Đang thuê"
        if (khachThue.getPhongId() != null) {
            updatePhongStatus(khachThue.getPhongId(), Phong.TRANG_THAI_DANG_THUE);
        }
    }
    
    public void update(KhachThue khachThue) {
        khachThueRepository.update(khachThue);
    }
    
    public void delete(KhachThue khachThue) {
        khachThueRepository.delete(khachThue);
    }
    
    public void traPhong(KhachThue khachThue) {
        khachThue.setDangThue(false);
        khachThue.setNgayRa(System.currentTimeMillis());
        khachThueRepository.update(khachThue);
        
        // Cập nhật trạng thái phòng thành "Trống"
        if (khachThue.getPhongId() != null) {
            updatePhongStatus(khachThue.getPhongId(), Phong.TRANG_THAI_TRONG);
        }
    }
    
    private void updatePhongStatus(int phongId, String trangThai) {
        phongRepository.getPhongById(phongId).observeForever(phong -> {
            if (phong != null) {
                phong.setTrangThai(trangThai);
                phongRepository.update(phong);
            }
        });
    }
}
