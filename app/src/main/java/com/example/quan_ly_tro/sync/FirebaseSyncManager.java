package com.example.quan_ly_tro.sync;

import android.content.Context;
import android.util.Log;

import com.example.quan_ly_tro.data.database.AppDatabase;
import com.example.quan_ly_tro.data.database.entity.ChiTietHoaDon;
import com.example.quan_ly_tro.data.database.entity.DichVu;
import com.example.quan_ly_tro.data.database.entity.HopDong;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.example.quan_ly_tro.data.database.entity.SuCo;
import com.example.quan_ly_tro.data.database.entity.ThongBao;
import com.example.quan_ly_tro.data.database.entity.ThuChi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class để đồng bộ dữ liệu Tuyệt đối giữa Chủ và Khách
 */
public class FirebaseSyncManager {
    
    private static final String TAG = "FirebaseSyncManager";
    
    private final Context context;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final AppDatabase database;
    
    private boolean isSyncing = false;
    
    public FirebaseSyncManager(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.database = AppDatabase.getDatabase(context);
    }
    
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
    
    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }
    
    // --- ĐỒNG BỘ CHIỀU LÊN (Dành cho Admin hoặc Khách sửa dữ liệu chung) ---

    public void syncToCloud(OnSyncCallback callback) {
        if (!isLoggedIn()) return;
        
        String userId = getCurrentUserId();
        String email = getCurrentUserEmail();
        
        // CHỈ ADMIN tinhdev9 MỚI ĐƯỢC SYNC TOÀN BỘ LÊN KHO QUẢN LÝ
        if (email == null || !email.equalsIgnoreCase("tinhdev9@gmail.com")) {
            // Khách thuê chỉ sync các kênh chung (SuCo, ThongBao, HopDong)
            syncSharedChannelsOnly(userId, callback);
            return;
        }

        performAdminSync(userId, callback);
    }

    private void performAdminSync(String adminUid, OnSyncCallback callback) {
        if (isSyncing) return;
        isSyncing = true;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // 1. Sync Phòng
                List<Phong> phongs = database.phongDao().getAllPhongSync();
                for (Phong p : phongs) {
                    if (p.getCloudId() == null || p.getCloudId().isEmpty()) {
                        p.setCloudId(java.util.UUID.randomUUID().toString());
                        database.phongDao().update(p);
                    }
                    syncPhongToCloud(adminUid, p);
                }

                // 2. Sync Khách
                List<KhachThue> khachs = database.khachThueDao().getAllKhachThueSync();
                for (KhachThue k : khachs) {
                    if (k.getCloudId() == null || k.getCloudId().isEmpty()) {
                        k.setCloudId(java.util.UUID.randomUUID().toString());
                        database.khachThueDao().update(k);
                    }
                    syncKhachThueToCloud(adminUid, k);
                }

                // 3. Sync Hóa đơn
                List<HoaDon> hoadons = database.hoaDonDao().getAllHoaDonSync();
                for (HoaDon h : hoadons) {
                    if (h.getCloudId() == null || h.getCloudId().isEmpty()) {
                        h.setCloudId(java.util.UUID.randomUUID().toString());
                        database.hoaDonDao().update(h);
                    }
                    syncHoaDonToCloud(adminUid, h);
                }

                // 4. Sync các kênh chung
                List<ThongBao> tbs = database.thongBaoDao().getAllThongBaoSync();
                for (ThongBao tb : tbs) syncThongBaoToCloud(adminUid, tb);

                List<SuCo> scs = database.suCoDao().getAllSuCoSync();
                for (SuCo sc : scs) syncSuCoToCloud(adminUid, sc);

                List<HopDong> hds = database.hopDongDao().getAllHopDongSync();
                for (HopDong hd : hds) syncHopDongToCloud(adminUid, hd);

                isSyncing = false;
                if (callback != null) callback.onSuccess("Đã đồng bộ toàn bộ dữ liệu lên Cloud.");
            } catch (Exception e) {
                isSyncing = false;
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    private void syncSharedChannelsOnly(String userId, OnSyncCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Khách chỉ đẩy Sự cố, Hợp đồng (chữ ký) và Thông báo (nếu có)
            List<SuCo> scs = database.suCoDao().getAllSuCoSync();
            for (SuCo sc : scs) syncSuCoToCloud(userId, sc);

            List<HopDong> hds = database.hopDongDao().getAllHopDongSync();
            for (HopDong hd : hds) syncHopDongToCloud(userId, hd);

            if (callback != null) callback.onSuccess("Đã cập nhật dữ liệu phản hồi.");
        });
    }

    private void syncPhongToCloud(String adminUid, Phong p) {
        String docId = p.getCloudId();
        if (docId == null || docId.isEmpty()) {
            docId = "phong_" + p.getId();
        }
        firestore.collection("management_data").document(adminUid).collection("phong")
                .document(docId).set(p, SetOptions.merge());
    }

    private void syncKhachThueToCloud(String adminUid, KhachThue kt) {
        String docId = kt.getCloudId();
        if (docId == null || docId.isEmpty()) {
            docId = "khach_" + kt.getId();
        }
        // Lưu hồ sơ
        firestore.collection("management_data").document(adminUid).collection("khach_thue")
                .document(docId).set(kt, SetOptions.merge());
        
        // Tạo cầu nối qua Email
        if (kt.getEmail() != null) {
            Map<String, Object> bridge = new HashMap<>();
            bridge.put("landlordUid", adminUid);
            firestore.collection("tenant_bridge").document(kt.getEmail().toLowerCase()).set(bridge);
        }
    }

    private void syncHoaDonToCloud(String adminUid, HoaDon hd) {
        String docId = hd.getCloudId();
        if (docId == null || docId.isEmpty()) {
            docId = "hoadon_" + hd.getId();
        }
        firestore.collection("management_data").document(adminUid).collection("hoa_don")
                .document(docId).set(hd, SetOptions.merge());
    }

    private void syncThongBaoToCloud(String userId, ThongBao tb) {
        String docId = "msg_" + tb.getNgayTao();
        firestore.collection("shared_thong_bao").document(docId).set(tb, SetOptions.merge());
    }

    private void syncSuCoToCloud(String userId, SuCo sc) {
        String docId = sc.getCloudId();
        if (docId == null || docId.isEmpty()) docId = userId + "_" + sc.getId();
        firestore.collection("shared_su_co").document(docId).set(sc, SetOptions.merge());
    }

    private void syncHopDongToCloud(String userId, HopDong hd) {
        String docId = hd.getCloudId();
        if (docId == null || docId.isEmpty()) docId = "contract_" + hd.getNgayTao();
        firestore.collection("shared_hop_dong").document(docId).set(hd, SetOptions.merge());
    }

    // --- ĐỒNG BỘ CHIỀU XUỐNG (PULL) ---

    public void pullFromCloud(OnSyncCallback callback) {
        String email = getCurrentUserEmail();
        if (email == null) {
            if (callback != null) callback.onError("Chưa đăng nhập");
            return;
        }

        // A. Tải các kênh chung trước (Dành cho mọi người)
        pullSharedChannels();

        // B. Tải dữ liệu quản lý (Phân biệt Admin và Khách)
        if (email.equalsIgnoreCase("tinhdev9@gmail.com")) {
            pullManagementData(getCurrentUserId(), callback);
        } else {
            // Dò tìm kho của chủ trọ cho Khách
            firestore.collection("tenant_bridge").document(email.toLowerCase())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String adminUid = doc.getString("landlordUid");
                            pullManagementData(adminUid, callback);
                        } else {
                            if (callback != null) callback.onError("Không tìm thấy dữ liệu thuê của bạn.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onError("Lỗi kết nối: " + e.getMessage());
                    });
        }
    }

    private void pullSharedChannels() {
        // Pull Thông báo
        firestore.collection("shared_thong_bao").get().addOnSuccessListener(snaps -> {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                for (com.google.firebase.firestore.DocumentSnapshot d : snaps) {
                    ThongBao tb = d.toObject(ThongBao.class);
                    if (tb != null) database.thongBaoDao().insertIgnore(tb);
                }
            });
        });
        // Pull Sự cố
        firestore.collection("shared_su_co").get().addOnSuccessListener(snaps -> {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                for (com.google.firebase.firestore.DocumentSnapshot d : snaps) {
                    SuCo sc = d.toObject(SuCo.class);
                    if (sc != null) database.suCoDao().insert(sc);
                }
            });
        });
        // Pull Hợp đồng
        firestore.collection("shared_hop_dong").get().addOnSuccessListener(snaps -> {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                for (com.google.firebase.firestore.DocumentSnapshot d : snaps) {
                    HopDong hd = d.toObject(HopDong.class);
                    if (hd != null) database.hopDongDao().insert(hd);
                }
            });
        });
    }

    private void pullManagementData(String targetUid, OnSyncCallback callback) {
        if (targetUid == null) {
            if (callback != null) callback.onError("ID chủ trọ không hợp lệ");
            return;
        }

        // BƯỚC 1: TẢI PHÒNG (Gốc của các bảng khác)
        firestore.collection("management_data").document(targetUid).collection("phong").get()
            .addOnSuccessListener(phongSnaps -> {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        for (com.google.firebase.firestore.DocumentSnapshot d : phongSnaps) {
                            Phong p = d.toObject(Phong.class);
                            if (p != null) database.phongDao().insert(p);
                        }
                        
                        // BƯỚC 2: TẢI KHÁCH THUÊ (Sau khi đã có phòng)
                        firestore.collection("management_data").document(targetUid).collection("khach_thue").get()
                            .addOnSuccessListener(khachSnaps -> {
                                AppDatabase.databaseWriteExecutor.execute(() -> {
                                    try {
                                        for (com.google.firebase.firestore.DocumentSnapshot d : khachSnaps) {
                                            KhachThue k = d.toObject(KhachThue.class);
                                            if (k != null) database.khachThueDao().insert(k);
                                        }

                                        // BƯỚC 3: TẢI HÓA ĐƠN (Sau khi có phòng và khách)
                                        firestore.collection("management_data").document(targetUid).collection("hoa_don").get()
                                            .addOnSuccessListener(hoaDonSnaps -> {
                                                AppDatabase.databaseWriteExecutor.execute(() -> {
                                                    try {
                                                        for (com.google.firebase.firestore.DocumentSnapshot d : hoaDonSnaps) {
                                                            HoaDon h = d.toObject(HoaDon.class);
                                                            if (h != null) database.hoaDonDao().insert(h);
                                                        }
                                                        if (callback != null) callback.onSuccess("Đã đồng bộ dữ liệu mới nhất.");
                                                    } catch (Exception e) {
                                                        if (callback != null) callback.onError("Lỗi lưu hóa đơn: " + e.getMessage());
                                                    }
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                if (callback != null) callback.onError("Lỗi tải hóa đơn: " + e.getMessage());
                                            });

                                    } catch (Exception e) {
                                        if (callback != null) callback.onError("Lỗi lưu khách thuê: " + e.getMessage());
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onError("Lỗi tải khách thuê: " + e.getMessage());
                            });

                    } catch (Exception e) {
                        if (callback != null) callback.onError("Lỗi lưu phòng: " + e.getMessage());
                    }
                });
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError("Lỗi tải danh sách phòng: " + e.getMessage());
            });
    }

    public void syncSingleHopDong(HopDong hd) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> syncHopDongToCloud(userId, hd));
    }

    public void syncSingleKhachThue(KhachThue kt) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> syncKhachThueToCloud(userId, kt));
    }

    public void syncSinglePhong(Phong p) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> syncPhongToCloud(userId, p));
    }

    public void syncSingleHoaDon(HoaDon hd) {
        String userId = getCurrentUserId();
        if (userId == null) return;
        AppDatabase.databaseWriteExecutor.execute(() -> syncHoaDonToCloud(userId, hd));
    }

    public void deleteSuCoFromCloud(SuCo sc) {
        String docId = sc.getCloudId();
        if (docId == null) return;
        firestore.collection("shared_su_co").document(docId).delete();
    }

    public void deleteThongBaoFromCloud(ThongBao tb) {
        firestore.collection("shared_thong_bao").document("msg_" + tb.getNgayTao()).delete();
    }

    public void clearRentalData() {
        // Thực hiện xóa đồng bộ (Synchronous-like in executor)
        database.phongDao().deleteAll();
        database.khachThueDao().deleteAll();
        database.hoaDonDao().deleteAll();
        database.chiTietHoaDonDao().deleteAll();
        database.dichVuDao().deleteAll();
        database.thuChiDao().deleteAll();
        database.hopDongDao().deleteAll();
        database.suCoDao().deleteAll();
        database.thongBaoDao().deleteAll();
    }

    public interface OnSyncCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
