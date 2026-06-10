package com.example.quan_ly_tro.ui.phong;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.repository.PhongRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel cho quản lý Phòng với tìm kiếm và lọc
 */
public class PhongViewModel extends AndroidViewModel {
    
    private final PhongRepository repository;
    private final LiveData<List<Phong>> allPhong;
    private final MutableLiveData<String> filterTrangThai = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MediatorLiveData<List<Phong>> filteredPhong = new MediatorLiveData<>();
    
    public PhongViewModel(@NonNull Application application) {
        super(application);
        repository = new PhongRepository(application);
        allPhong = repository.getAllPhong();
        
        // Mặc định
        filterTrangThai.setValue("");
        searchQuery.setValue("");
        
        // Combine filter và search
        filteredPhong.addSource(allPhong, phongList -> applyFilters(phongList));
        filteredPhong.addSource(filterTrangThai, filter -> applyFilters(allPhong.getValue()));
        filteredPhong.addSource(searchQuery, query -> applyFilters(allPhong.getValue()));
    }
    
    private void applyFilters(List<Phong> phongList) {
        if (phongList == null) {
            filteredPhong.setValue(new ArrayList<>());
            return;
        }
        
        String filter = filterTrangThai.getValue();
        String query = searchQuery.getValue();
        
        List<Phong> result = new ArrayList<>();
        for (Phong phong : phongList) {
            // Filter by trạng thái
            boolean matchesFilter = (filter == null || filter.isEmpty()) 
                    || phong.getTrangThai().equals(filter);
            
            // Search by số phòng
            boolean matchesSearch = (query == null || query.isEmpty())
                    || phong.getSoPhong().toLowerCase().contains(query.toLowerCase());
            
            if (matchesFilter && matchesSearch) {
                result.add(phong);
            }
        }
        
        filteredPhong.setValue(result);
    }
    
    public LiveData<List<Phong>> getAllPhong() {
        return allPhong;
    }
    
    public LiveData<List<Phong>> getFilteredPhong() {
        return filteredPhong;
    }
    
    public void setFilter(String trangThai) {
        filterTrangThai.setValue(trangThai);
    }
    
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }
    
    public LiveData<Phong> getPhongById(int id) {
        return repository.getPhongById(id);
    }
    
    public void insert(Phong phong) {
        repository.insert(phong);
    }
    
    public void update(Phong phong) {
        repository.update(phong);
    }
    
    public void delete(Phong phong) {
        repository.delete(phong);
    }
    
    public void deleteById(int id) {
        repository.deleteById(id);
    }
}
