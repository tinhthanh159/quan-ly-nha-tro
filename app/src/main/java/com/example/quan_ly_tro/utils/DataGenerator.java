package com.example.quan_ly_tro.utils;

import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.dao.DichVuDao;
import com.example.quan_ly_tro.data.database.dao.HoaDonDao;
import com.example.quan_ly_tro.data.database.dao.KhachThueDao;
import com.example.quan_ly_tro.data.database.dao.PhongDao;
import com.example.quan_ly_tro.data.database.dao.ThuChiDao;
import com.example.quan_ly_tro.data.database.entity.DichVu;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.database.entity.ThuChi;
import com.example.quan_ly_tro.data.database.entity.ThongBao;

import java.util.UUID;

/**
 * Utility class để tạo dữ liệu giả ĐỊNH DANH (không ngẫu nhiên) cho database
 */
public class DataGenerator {

    public static void insertDummyData(AppDatabase db) {
        // Đã tắt tính năng sinh dữ liệu mẫu theo yêu cầu.
        // Người dùng sẽ tự thêm dữ liệu trực tiếp trên giao diện.
    }

    private static void createDummyNotifications(com.example.quan_ly_tro.data.database.dao.ThongBaoDao dao) {
        String[][] data = {
            {"Nội quy nhà trọ", "Vui lòng giữ trật tự chung sau 23h. Cửa chính sẽ khóa vào lúc 23h30 hàng ngày."},
            {"Thông báo tiền phòng", "Vui lòng hoàn tất đóng tiền phòng tháng này trước ngày 10."},
            {"Bảo trì thiết bị", "Hành lang khu A sẽ được sơn lại vào cuối tuần này (Thứ 7 & CN). Mong các bạn thông cảm vì sự bất tiện."}
        };

        for (String[] item : data) {
            ThongBao tb = new ThongBao();
            tb.setTieuDe(item[0]);
            tb.setNoiDung(item[1]);
            tb.setNgayTao(System.currentTimeMillis());
            tb.setNguoiGui("Ban Quản Lý");
            dao.insert(tb);
        }
    }
}
