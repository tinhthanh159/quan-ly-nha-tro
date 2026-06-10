package com.example.quan_ly_tro.ui.hoadon;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.quan_ly_tro.data.database.entity.DichVu;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.database.entity.ThuChi;
import com.example.quan_ly_tro.data.repository.DichVuRepository;
import com.example.quan_ly_tro.data.repository.HoaDonRepository;
import com.example.quan_ly_tro.data.repository.PhongRepository;
import com.example.quan_ly_tro.data.repository.ThuChiRepository;

import java.util.List;

/**
 * ViewModel cho quản lý Hóa đơn - Hỗ trợ lọc theo phòng và trạng thái
 */
public class HoaDonViewModel extends AndroidViewModel {
    
    private final HoaDonRepository hoaDonRepository;
    private final PhongRepository phongRepository;
    private final DichVuRepository dichVuRepository;
    private final ThuChiRepository thuChiRepository;
    
    private final MutableLiveData<Integer> filterPhongId = new MutableLiveData<>(0);
    private final MutableLiveData<String> filterTrangThai = new MutableLiveData<>("");
    
    private final MediatorLiveData<FilterParams> filterTrigger = new MediatorLiveData<>();
    private final LiveData<List<HoaDon>> filteredHoaDon;
    
    public HoaDonViewModel(@NonNull Application application) {
        super(application);
        hoaDonRepository = new HoaDonRepository(application);
        phongRepository = new PhongRepository(application);
        dichVuRepository = new DichVuRepository(application);
        thuChiRepository = new ThuChiRepository(application);
        
        // Thiết lập trigger kết hợp 2 bộ lọc
        filterTrigger.addSource(filterPhongId, id -> updateTrigger());
        filterTrigger.addSource(filterTrangThai, status -> updateTrigger());
        
        filteredHoaDon = Transformations.switchMap(filterTrigger, params -> {
            if (params == null) return hoaDonRepository.getAllHoaDon();
            return hoaDonRepository.getFilteredHoaDon(params.phongId, params.trangThai);
        });
        
        // Giá trị mặc định
        updateTrigger();
    }
    
    private void updateTrigger() {
        filterTrigger.setValue(new FilterParams(
                filterPhongId.getValue() != null ? filterPhongId.getValue() : 0,
                filterTrangThai.getValue() != null ? filterTrangThai.getValue() : ""
        ));
    }
    
    public LiveData<List<HoaDon>> getFilteredHoaDon() {
        return filteredHoaDon;
    }
    
    public void setFilter(String trangThai) {
        filterTrangThai.setValue(trangThai != null ? trangThai : "");
    }

    public void setFilterPhongId(int phongId) {
        filterPhongId.setValue(phongId);
    }
    
    public LiveData<HoaDon> getHoaDonById(int id) {
        return hoaDonRepository.getHoaDonById(id);
    }
    
    public LiveData<List<Phong>> getAllPhong() {
        return phongRepository.getAllPhong();
    }
    
    public LiveData<List<DichVu>> getAllDichVu() {
        return dichVuRepository.getAllDichVu();
    }
    
    public void insert(HoaDon hoaDon) {
        hoaDonRepository.insert(hoaDon);
    }
    
    public void update(HoaDon hoaDon) {
        hoaDonRepository.update(hoaDon);
    }
    
    public void delete(HoaDon hoaDon) {
        hoaDonRepository.delete(hoaDon);
    }
    
    public void thanhToan(HoaDon hoaDon) {
        hoaDon.setTrangThai(HoaDon.TRANG_THAI_DA_THANH_TOAN);
        hoaDon.setNgayThanhToan(System.currentTimeMillis());
        hoaDonRepository.update(hoaDon);
        
        ThuChi thuChi = new ThuChi();
        thuChi.setLoai(ThuChi.LOAI_THU);
        thuChi.setDanhMuc(ThuChi.DANH_MUC_TIEN_THUE);
        thuChi.setSoTien(hoaDon.getTongTien());
        thuChi.setMoTa("Thu tiền hóa đơn tháng " + hoaDon.getThangNam());
        thuChi.setPhongId(hoaDon.getPhongId());
        thuChi.setNgayGiaoDich(System.currentTimeMillis());
        thuChiRepository.insert(thuChi);
        
        updateTrigger(); // Refresh
    }

    private static class FilterParams {
        final int phongId;
        final String trangThai;
        FilterParams(int phongId, String trangThai) {
            this.phongId = phongId;
            this.trangThai = trangThai;
        }
    }
}
