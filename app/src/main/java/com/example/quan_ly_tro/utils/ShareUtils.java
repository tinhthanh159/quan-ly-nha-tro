package com.example.quan_ly_tro.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.quan_ly_tro.R;
import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class để chia sẻ thông tin qua SMS, Zalo, và các ứng dụng khác
 */
public class ShareUtils {

    // TODO: Chủ trọ cần điền thông tin ngân hàng của mình ở đây
    private static final String BANK_ID = "BIDV"; // Tên viết tắt hoặc BIN của ngân hàng (VD: MB, VCB, VTB)
    private static final String ACCOUNT_NO = "5210379567"; // Số tài khoản
    private static final String ACCOUNT_NAME = "BUI QUANG TINH"; // Tên chủ tài khoản (Viết hoa không dấu)
    
    /**
     * Hiển thị Dialog mã VietQR và gửi qua Zalo
     */
    public static void showVietQRDialog(Context context, HoaDon hoaDon, Phong phong) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_qr_code, null);
        
        ImageView ivQrCode = dialogView.findViewById(R.id.iv_qr_code);
        TextView tvRoomName = dialogView.findViewById(R.id.tv_room_name);
        TextView tvRoomInfo = dialogView.findViewById(R.id.tv_room_info);
        MaterialButton btnShare = dialogView.findViewById(R.id.btn_share);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
        
        btnSave.setVisibility(View.GONE); // Ẩn nút lưu vì chỉ cần share qua Zalo
        btnShare.setText("Gửi qua Zalo");
        
        tvRoomName.setText("Thanh toán Hóa đơn " + (phong != null ? "P." + phong.getSoPhong() : ""));
        tvRoomInfo.setText("Tổng tiền: " + FormatUtils.formatCurrency(hoaDon.getTongTien()));
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create();
                
        // Tạo nội dung chuyển khoản: "P01 thanh toan thang 05"
        String addInfo = (phong != null ? "P" + phong.getSoPhong() : "") + " thanh toan thang " + hoaDon.getThangNam().replace("/", "");
        
        // Gọi API VietQR để lấy ảnh mã QR
        loadVietQrImage(ivQrCode, hoaDon.getTongTien(), addInfo);

        btnShare.setOnClickListener(v -> {
            sendInvoiceZaloWithVietQR(context, hoaDon, phong, addInfo);
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private static void loadVietQrImage(ImageView imageView, double amount, String addInfo) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        
        executor.execute(() -> {
            try {
                String encodedInfo = URLEncoder.encode(addInfo, "UTF-8");
                String encodedName = URLEncoder.encode(ACCOUNT_NAME, "UTF-8");
                String urlStr = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%.0f&addInfo=%s&accountName=%s", 
                                              BANK_ID, ACCOUNT_NO, amount, encodedInfo, encodedName);
                
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                
                handler.post(() -> {
                    if (myBitmap != null) {
                        imageView.setImageBitmap(myBitmap);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Gửi hóa đơn qua Zalo kèm link VietQR
     */
    private static void sendInvoiceZaloWithVietQR(Context context, HoaDon hoaDon, Phong phong, String addInfo) {
        String message = createInvoiceMessage(hoaDon, phong);
        
        try {
            String encodedInfo = URLEncoder.encode(addInfo, "UTF-8");
            String encodedName = URLEncoder.encode(ACCOUNT_NAME, "UTF-8");
            String vietQrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%.0f&addInfo=%s&accountName=%s", 
                                          BANK_ID, ACCOUNT_NO, hoaDon.getTongTien(), encodedInfo, encodedName);
            
            message += "\n\n💳 THANH TOÁN CHUYỂN KHOẢN:\n";
            message += "Ngân hàng: " + BANK_ID + "\n";
            message += "STK: " + ACCOUNT_NO + "\n";
            message += "Chủ TK: " + ACCOUNT_NAME + "\n";
            message += "Nội dung: " + addInfo + "\n";
            message += "\n👉 Nhấn vào link sau để lấy mã QR quét thanh toán tự động:\n" + vietQrUrl;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isAppInstalled(context, "com.zing.zalo")) {
            Intent zaloIntent = new Intent(Intent.ACTION_SEND);
            zaloIntent.setType("text/plain");
            zaloIntent.setPackage("com.zing.zalo");
            zaloIntent.putExtra(Intent.EXTRA_TEXT, message);
            try {
                context.startActivity(zaloIntent);
            } catch (Exception e) {
                shareGeneral(context, message);
            }
        } else {
            Toast.makeText(context, "Zalo chưa được cài đặt", Toast.LENGTH_SHORT).show();
            shareGeneral(context, message);
        }
    }
    
    /**
     * Gửi hóa đơn qua SMS
     */
    public static void sendInvoiceSms(Context context, HoaDon hoaDon, 
                                       Phong phong, KhachThue khachThue) {
        if (khachThue == null || khachThue.getSoDienThoai() == null) {
            Toast.makeText(context, "Không có số điện thoại khách thuê", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String message = createInvoiceMessage(hoaDon, phong);
        
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:" + khachThue.getSoDienThoai()));
        smsIntent.putExtra("sms_body", message);
        
        try {
            context.startActivity(smsIntent);
        } catch (Exception e) {
            Toast.makeText(context, "Không thể mở ứng dụng SMS", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Gửi hóa đơn qua Zalo (nếu đã cài đặt)
     */
    public static void sendInvoiceZalo(Context context, HoaDon hoaDon, 
                                        Phong phong, KhachThue khachThue) {
        // Fallback for older method call
        String addInfo = (phong != null ? "P" + phong.getSoPhong() : "") + " thanh toan thang " + hoaDon.getThangNam().replace("/", "");
        sendInvoiceZaloWithVietQR(context, hoaDon, phong, addInfo);
    }
    
    /**
     * Chia sẻ qua các ứng dụng khác (general share)
     */
    public static void shareGeneral(Context context, String message) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
    }
    
    /**
     * Chia sẻ hóa đơn qua bất kỳ ứng dụng nào
     */
    public static void shareInvoice(Context context, HoaDon hoaDon, Phong phong) {
        String addInfo = (phong != null ? "P" + phong.getSoPhong() : "") + " thanh toan thang " + hoaDon.getThangNam().replace("/", "");
        sendInvoiceZaloWithVietQR(context, hoaDon, phong, addInfo);
    }
    
    /**
     * Tạo nội dung tin nhắn hóa đơn
     */
    private static String createInvoiceMessage(HoaDon hoaDon, Phong phong) {
        StringBuilder sb = new StringBuilder();
        sb.append("📝 THÔNG BÁO HÓA ĐƠN\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n");
        sb.append("Phòng: ").append(phong != null ? phong.getSoPhong() : "N/A").append("\n");
        sb.append("Kỳ thanh toán: ").append(hoaDon.getThangNam()).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n");
        sb.append("💰 TỔNG TIỀN: ").append(FormatUtils.formatCurrency(hoaDon.getTongTien())).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n");
        
        if (HoaDon.TRANG_THAI_CHUA_THANH_TOAN.equals(hoaDon.getTrangThai())) {
            sb.append("⚠️ Trạng thái: CHƯA THANH TOÁN\n");
            sb.append("Vui lòng thanh toán sớm. Xin cảm ơn!");
        } else {
            sb.append("✅ Trạng thái: ĐÃ THANH TOÁN\n");
            sb.append("Cảm ơn quý khách!");
        }
        
        return sb.toString();
    }
    
    /**
     * Kiểm tra app đã được cài đặt hay chưa
     */
    private static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Chia sẻ thông tin phòng
     */
    public static void shareRoomInfo(Context context, Phong phong) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏠 THÔNG TIN PHÒNG TRỌ\n");
        sb.append("━━━━━━━━━━━━━━━━━━\n");
        sb.append("Phòng: ").append(phong.getSoPhong()).append("\n");
        sb.append("Loại: ").append(phong.getLoaiPhong()).append("\n");
        sb.append("Diện tích: ").append(phong.getDienTich()).append(" m²\n");
        sb.append("Giá thuê: ").append(FormatUtils.formatCurrency(phong.getGiaThue())).append("/tháng\n");
        sb.append("Trạng thái: ").append(phong.getTrangThai()).append("\n");
        
        if (phong.getMoTa() != null && !phong.getMoTa().isEmpty()) {
            sb.append("Mô tả: ").append(phong.getMoTa()).append("\n");
        }
        
        shareGeneral(context, sb.toString());
    }
}
