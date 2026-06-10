package com.example.quan_ly_tro.ui.thongke;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.repository.HoaDonRepository;
import com.example.quan_ly_tro.data.repository.KhachThueRepository;
import com.example.quan_ly_tro.data.repository.PhongRepository;
import com.example.quan_ly_tro.data.repository.ThuChiRepository;

import java.util.Calendar;
import java.util.List;

/**
 * ViewModel cho màn hình Thống kê
 */
public class ThongKeViewModel extends AndroidViewModel {
    
    private final PhongRepository phongRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ThuChiRepository thuChiRepository;
    private final KhachThueRepository khachThueRepository;
    
    // Tháng hiện tại được chọn
    private final MutableLiveData<Calendar> selectedMonth = new MutableLiveData<>();
    
    // Statistics data
    private final LiveData<List<Phong>> allPhong;
    private final LiveData<List<KhachThue>> allKhachThue;
    private final LiveData<Integer> soPhongDangThue;
    private final LiveData<Integer> soHoaDonChuaThanhToan;
    private final LiveData<Double> tongTienChuaThu;
    private final LiveData<Double> tongDoanhThu;
    
    public ThongKeViewModel(@NonNull Application application) {
        super(application);
        phongRepository = new PhongRepository(application);
        hoaDonRepository = new HoaDonRepository(application);
        thuChiRepository = new ThuChiRepository(application);
        khachThueRepository = new KhachThueRepository(application);
        
        allPhong = phongRepository.getAllPhong();
        allKhachThue = khachThueRepository.getAllKhachThue();
        soPhongDangThue = phongRepository.getSoPhongByTrangThai(Phong.TRANG_THAI_DANG_THUE);
        soHoaDonChuaThanhToan = hoaDonRepository.getSoHoaDonChuaThanhToan();
        tongTienChuaThu = hoaDonRepository.getTongTienChuaThu();
        tongDoanhThu = hoaDonRepository.getTongDoanhThu();
        
        // Initialize to current month
        selectedMonth.setValue(Calendar.getInstance());
    }
    
    public LiveData<Calendar> getSelectedMonth() {
        return selectedMonth;
    }
    
    public void previousMonth() {
        Calendar current = selectedMonth.getValue();
        if (current != null) {
            current.add(Calendar.MONTH, -1);
            selectedMonth.setValue(current);
        }
    }
    
    public void nextMonth() {
        Calendar current = selectedMonth.getValue();
        if (current != null) {
            current.add(Calendar.MONTH, 1);
            selectedMonth.setValue(current);
        }
    }
    
    public String getFormattedMonth() {
        Calendar cal = selectedMonth.getValue();
        if (cal != null) {
            return String.format("Tháng %02d/%d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
        }
        return "";
    }
    
    public String getThangNamKey() {
        Calendar cal = selectedMonth.getValue();
        if (cal != null) {
            return String.format("%02d/%d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
        }
        return "";
    }
    
    public LiveData<List<Phong>> getAllPhong() {
        return allPhong;
    }
    
    public LiveData<List<KhachThue>> getAllKhachThue() {
        return allKhachThue;
    }
    
    public LiveData<Integer> getSoPhongDangThue() {
        return soPhongDangThue;
    }
    
    public LiveData<Integer> getSoHoaDonChuaThanhToan() {
        return soHoaDonChuaThanhToan;
    }
    
    public LiveData<Double> getTongTienChuaThu() {
        return tongTienChuaThu;
    }
    
    public LiveData<Double> getTongDoanhThu() {
        return tongDoanhThu;
    }
    
    public LiveData<Double> getDoanhThuThang(String thangNam) {
        return hoaDonRepository.getDoanhThuThang(thangNam);
    }
    
    public LiveData<Double> getTongThu() {
        return thuChiRepository.getTongThu();
    }
    
    public LiveData<Double> getTongChi() {
        return thuChiRepository.getTongChi();
    }
    
    public LiveData<List<HoaDon>> getAllHoaDon() {
        return hoaDonRepository.getAllHoaDon();
    }
}
