package com.example.quan_ly_tro.ui.thuchi;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.quan_ly_tro.data.database.entity.ThuChi;
import com.example.quan_ly_tro.data.repository.ThuChiRepository;

import java.util.List;

/**
 * ViewModel cho quản lý Thu/Chi
 */
public class ThuChiViewModel extends AndroidViewModel {
    
    private final ThuChiRepository repository;
    private final LiveData<List<ThuChi>> allThuChi;
    private final MutableLiveData<String> filterLoai = new MutableLiveData<>();
    private final LiveData<List<ThuChi>> filteredThuChi;
    
    private final LiveData<Double> tongThu;
    private final LiveData<Double> tongChi;
    private final LiveData<Double> soDu;
    
    public ThuChiViewModel(@NonNull Application application) {
        super(application);
        repository = new ThuChiRepository(application);
        allThuChi = repository.getAllThuChi();
        
        tongThu = repository.getTongThu();
        tongChi = repository.getTongChi();
        soDu = repository.getSoDu();
        
        // Filter theo loại
        filteredThuChi = Transformations.switchMap(filterLoai, loai -> {
            if (loai == null || loai.isEmpty()) {
                return allThuChi;
            } else {
                return repository.getThuChiByLoai(loai);
            }
        });
        
        filterLoai.setValue("");
    }
    
    public LiveData<List<ThuChi>> getAllThuChi() {
        return allThuChi;
    }
    
    public LiveData<List<ThuChi>> getFilteredThuChi() {
        return filteredThuChi;
    }
    
    public void setFilter(String loai) {
        filterLoai.setValue(loai != null ? loai : "");
    }
    
    public LiveData<Double> getTongThu() {
        return tongThu;
    }
    
    public LiveData<Double> getTongChi() {
        return tongChi;
    }
    
    public LiveData<Double> getSoDu() {
        return soDu;
    }
    
    public void insert(ThuChi thuChi) {
        repository.insert(thuChi);
    }
    
    public void update(ThuChi thuChi) {
        repository.update(thuChi);
    }
    
    public void delete(ThuChi thuChi) {
        repository.delete(thuChi);
    }
    
    public LiveData<ThuChi> getThuChiById(int id) {
        return repository.getThuChiById(id);
    }
}
