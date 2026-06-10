package com.example.quan_ly_tro.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.quan_ly_tro.data.repository.HoaDonRepository;
import com.example.quan_ly_tro.data.repository.KhachThueRepository;
import com.example.quan_ly_tro.data.repository.PhongRepository;
import com.example.quan_ly_tro.utils.FormatUtils;

/**
 * ViewModel cho Dashboard
 */
public class DashboardViewModel extends AndroidViewModel {
    
    private final PhongRepository phongRepository;
    private final KhachThueRepository khachThueRepository;
    private final HoaDonRepository hoaDonRepository;
    
    private final LiveData<Integer> tongSoPhong;
    private final LiveData<Integer> soPhongTrong;
    private final LiveData<Integer> soPhongDangThue;
    private final LiveData<Integer> soHoaDonChuaThanhToan;
    private final LiveData<Double> doanhThuThang;
    private final LiveData<Double> tongTienChuaThu;
    private final LiveData<Integer> tongSoKhachDangThue;
    
    public DashboardViewModel(@NonNull Application application) {
        super(application);
        
        phongRepository = new PhongRepository(application);
        khachThueRepository = new KhachThueRepository(application);
        hoaDonRepository = new HoaDonRepository(application);
        
        tongSoPhong = phongRepository.getTongSoPhong();
        soPhongTrong = phongRepository.getSoPhongTrong();
        soPhongDangThue = phongRepository.getSoPhongDangThue();
        soHoaDonChuaThanhToan = hoaDonRepository.getSoHoaDonChuaThanhToan();
        doanhThuThang = hoaDonRepository.getDoanhThuThang(FormatUtils.getCurrentMonthYear());
        tongTienChuaThu = hoaDonRepository.getTongTienChuaThu();
        tongSoKhachDangThue = khachThueRepository.getTongSoKhachDangThue();
    }
    
    public LiveData<Double> getTongTienChuaThu() {
        return tongTienChuaThu;
    }

    public LiveData<Integer> getTongSoPhong() {
        return tongSoPhong;
    }
    
    public LiveData<Integer> getSoPhongTrong() {
        return soPhongTrong;
    }
    
    public LiveData<Integer> getSoPhongDangThue() {
        return soPhongDangThue;
    }
    
    public LiveData<Integer> getSoHoaDonChuaThanhToan() {
        return soHoaDonChuaThanhToan;
    }
    
    public LiveData<Double> getDoanhThuThang() {
        return doanhThuThang;
    }
    
    public LiveData<Integer> getTongSoKhachDangThue() {
        return tongSoKhachDangThue;
    }
}
