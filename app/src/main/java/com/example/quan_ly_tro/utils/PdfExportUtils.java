package com.example.quan_ly_tro.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.quan_ly_tro.data.database.entity.HoaDon;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class để xuất báo cáo PDF
 */
public class PdfExportUtils {
    
    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 40;
    
    /**
     * Xuất báo cáo tổng hợp
     */
    public static void exportBaoCaoTongHop(Context context, 
                                            List<Phong> phongList,
                                            List<KhachThue> khachThueList,
                                            List<HoaDon> hoaDonList,
                                            double tongThu,
                                            double tongChi) {
        PdfDocument document = new PdfDocument();
        
        // Page 1: Tổng quan
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        
        Paint titlePaint = new Paint();
        titlePaint.setTextSize(24);
        titlePaint.setFakeBoldText(true);
        
        Paint headerPaint = new Paint();
        headerPaint.setTextSize(16);
        headerPaint.setFakeBoldText(true);
        
        Paint textPaint = new Paint();
        textPaint.setTextSize(12);
        
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1);
        
        int y = MARGIN + 30;
        
        // Title
        canvas.drawText("BÁO CÁO QUẢN LÝ NHÀ TRỌ", MARGIN, y, titlePaint);
        y += 20;
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        canvas.drawText("Ngày xuất: " + sdf.format(new Date()), MARGIN, y, textPaint);
        y += 40;
        
        // Divider
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
        y += 30;
        
        // Thống kê phòng
        canvas.drawText("1. THỐNG KÊ PHÒNG", MARGIN, y, headerPaint);
        y += 25;
        
        int tongPhong = phongList != null ? phongList.size() : 0;
        int phongTrong = 0, phongDangThue = 0, phongDangSua = 0;
        if (phongList != null) {
            for (Phong p : phongList) {
                if (Phong.TRANG_THAI_TRONG.equals(p.getTrangThai())) phongTrong++;
                else if (Phong.TRANG_THAI_DANG_THUE.equals(p.getTrangThai())) phongDangThue++;
                else if (Phong.TRANG_THAI_DANG_SUA.equals(p.getTrangThai())) phongDangSua++;
            }
        }
        
        canvas.drawText("• Tổng số phòng: " + tongPhong, MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("• Phòng trống: " + phongTrong, MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("• Phòng đang thuê: " + phongDangThue, MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("• Phòng đang sửa: " + phongDangSua, MARGIN + 20, y, textPaint);
        y += 20;
        
        if (tongPhong > 0) {
            int tyLeLapDay = (phongDangThue * 100) / tongPhong;
            canvas.drawText("• Tỷ lệ lấp đầy: " + tyLeLapDay + "%", MARGIN + 20, y, textPaint);
        }
        y += 35;
        
        // Thống kê khách thuê
        canvas.drawText("2. THỐNG KÊ KHÁCH THUÊ", MARGIN, y, headerPaint);
        y += 25;
        
        int tongKhach = khachThueList != null ? khachThueList.size() : 0;
        int khachDangThue = 0;
        if (khachThueList != null) {
            for (KhachThue kt : khachThueList) {
                if (kt.isDangThue()) khachDangThue++;
            }
        }
        
        canvas.drawText("• Tổng số khách: " + tongKhach, MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("• Khách đang thuê: " + khachDangThue, MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("• Khách đã trả: " + (tongKhach - khachDangThue), MARGIN + 20, y, textPaint);
        y += 35;
        
        // Thống kê hóa đơn
        canvas.drawText("3. THỐNG KÊ HÓA ĐƠN", MARGIN, y, headerPaint);
        y += 25;
        
        int tongHoaDon = hoaDonList != null ? hoaDonList.size() : 0;
        int hoaDonDaThu = 0, hoaDonChuaThu = 0;
        double tienDaThu = 0, tienChuaThu = 0;
        if (hoaDonList != null) {
            for (HoaDon hd : hoaDonList) {
                if (HoaDon.TRANG_THAI_DA_THANH_TOAN.equals(hd.getTrangThai())) {
                    hoaDonDaThu++;
                    tienDaThu += hd.getTongTien();
                } else {
                    hoaDonChuaThu++;
                    tienChuaThu += hd.getTongTien();
                }
            }
        }
        
        canvas.drawText("• Tổng số hóa đơn: " + tongHoaDon, MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("• Đã thanh toán: " + hoaDonDaThu + " (" + FormatUtils.formatCurrency(tienDaThu) + ")", MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("• Chưa thanh toán: " + hoaDonChuaThu + " (" + FormatUtils.formatCurrency(tienChuaThu) + ")", MARGIN + 20, y, textPaint);
        y += 35;
        
        // Thống kê thu chi
        canvas.drawText("4. THỐNG KÊ THU CHI", MARGIN, y, headerPaint);
        y += 25;
        
        canvas.drawText("• Tổng thu: " + FormatUtils.formatCurrency(tongThu), MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("• Tổng chi: " + FormatUtils.formatCurrency(tongChi), MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("• Lợi nhuận: " + FormatUtils.formatCurrency(tongThu - tongChi), MARGIN + 20, y, textPaint);
        y += 40;
        
        // Divider
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
        y += 30;
        
        // Footer
        textPaint.setTextSize(10);
        canvas.drawText("Báo cáo được tạo tự động bởi ứng dụng Quản lý Nhà trọ", MARGIN, y, textPaint);
        
        document.finishPage(page);
        
        // Save file
        String fileName = "BaoCao_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        
        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            
            Toast.makeText(context, "Đã xuất báo cáo: " + fileName, Toast.LENGTH_LONG).show();
            
            // Open PDF
            openPdf(context, file);
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi xuất báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Xuất hóa đơn đơn lẻ
     */
    public static void exportHoaDon(Context context, HoaDon hoaDon, String tenPhong, String tenKhach) {
        PdfDocument document = new PdfDocument();
        
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        
        Paint titlePaint = new Paint();
        titlePaint.setTextSize(28);
        titlePaint.setFakeBoldText(true);
        
        Paint headerPaint = new Paint();
        headerPaint.setTextSize(14);
        headerPaint.setFakeBoldText(true);
        
        Paint textPaint = new Paint();
        textPaint.setTextSize(12);
        
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1);
        
        int y = MARGIN + 40;
        
        // Title
        canvas.drawText("HÓA ĐƠN TIỀN PHÒNG", MARGIN + 150, y, titlePaint);
        y += 40;
        
        // Info
        canvas.drawText("Tháng: " + hoaDon.getThangNam(), MARGIN, y, headerPaint);
        y += 25;
        canvas.drawText("Phòng: " + tenPhong, MARGIN, y, textPaint);
        y += 20;
        canvas.drawText("Khách thuê: " + (tenKhach != null ? tenKhach : "---"), MARGIN, y, textPaint);
        y += 20;
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        canvas.drawText("Ngày tạo: " + sdf.format(new Date(hoaDon.getNgayTao())), MARGIN, y, textPaint);
        y += 30;
        
        // Divider
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
        y += 25;
        
        // Chi tiết
        canvas.drawText("CHI TIẾT HÓA ĐƠN", MARGIN, y, headerPaint);
        y += 25;
        
        canvas.drawText("• Tiền phòng: " + FormatUtils.formatCurrency(hoaDon.getTienPhong()), MARGIN + 20, y, textPaint);
        y += 20;
        
        double tienDichVu = hoaDon.getTongTien() - hoaDon.getTienPhong();
        canvas.drawText("• Tiền dịch vụ (điện, nước,...): " + FormatUtils.formatCurrency(tienDichVu), MARGIN + 20, y, textPaint);
        y += 30;
        
        // Divider
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
        y += 25;
        
        // Tổng
        titlePaint.setTextSize(18);
        canvas.drawText("TỔNG CỘNG: " + FormatUtils.formatCurrency(hoaDon.getTongTien()), MARGIN, y, titlePaint);
        y += 30;
        
        // Trạng thái
        String trangThai = HoaDon.TRANG_THAI_DA_THANH_TOAN.equals(hoaDon.getTrangThai()) 
                ? "ĐÃ THANH TOÁN" : "CHƯA THANH TOÁN";
        canvas.drawText("Trạng thái: " + trangThai, MARGIN, y, headerPaint);
        
        if (hoaDon.getNgayThanhToan() != null && hoaDon.getNgayThanhToan() > 0) {
            y += 20;
            canvas.drawText("Ngày thanh toán: " + sdf.format(new Date(hoaDon.getNgayThanhToan())), MARGIN, y, textPaint);
        }
        
        document.finishPage(page);
        
        // Save file
        String fileName = "HoaDon_" + hoaDon.getThangNam().replace("/", "-") + "_" + tenPhong + ".pdf";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        
        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            
            Toast.makeText(context, "Đã xuất: " + fileName, Toast.LENGTH_LONG).show();
            
            // Open PDF
            openPdf(context, file);
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi xuất hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private static void openPdf(Context context, File file) {
        try {
            // Sửa lại authority cho đúng với AndroidManifest.xml (.fileprovider chứ không phải .provider)
            Uri uri = FileProvider.getUriForFile(context, 
                    context.getPackageName() + ".fileprovider", file);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            try {
                // Sử dụng Chooser để người dùng có thể chọn app đọc PDF
                context.startActivity(Intent.createChooser(intent, "Mở báo cáo với:"));
            } catch (android.content.ActivityNotFoundException e) {
                // Fallback: Nếu không có app đọc PDF, cho phép chia sẻ file qua Zalo/Drive/Email
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(shareIntent, "Máy chưa cài ứng dụng đọc PDF. Bạn có muốn gửi file qua:"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi khi mở file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
