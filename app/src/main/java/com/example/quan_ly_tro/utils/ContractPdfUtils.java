package com.example.quan_ly_tro.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.quan_ly_tro.data.database.entity.HopDong;
import com.example.quan_ly_tro.data.database.entity.KhachThue;
import com.example.quan_ly_tro.data.database.entity.Phong;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class để tạo hợp đồng PDF
 */
public class ContractPdfUtils {
    
    private static final int PAGE_WIDTH = 595; // A4
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 40;
    
    /**
     * Tạo và xuất hợp đồng thuê phòng dạng PDF có kèm chữ ký
     */
    public static void generateContractWithSignature(Context context, HopDong hopDong, 
                                                     Phong phong, KhachThue khachThue,
                                                     String tenChuTro, android.graphics.Bitmap signature) {
        PdfDocument document = new PdfDocument();
        
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(20);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setFakeBoldText(true);
        
        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.BLACK);
        headerPaint.setTextSize(14);
        headerPaint.setFakeBoldText(true);
        
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(12);
        
        Paint linePaint = new Paint();
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(1);
        
        int y = MARGIN + 30;
        
        // Header
        canvas.drawText("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", PAGE_WIDTH / 2, y, titlePaint);
        y += 25;
        titlePaint.setTextSize(14);
        canvas.drawText("Độc lập - Tự do - Hạnh phúc", PAGE_WIDTH / 2, y, titlePaint);
        y += 10;
        canvas.drawLine(PAGE_WIDTH / 2 - 80, y, PAGE_WIDTH / 2 + 80, y, linePaint);
        y += 40;
        
        // Title
        titlePaint.setTextSize(18);
        canvas.drawText("HỢP ĐỒNG THUÊ PHÒNG TRỌ", PAGE_WIDTH / 2, y, titlePaint);
        y += 40;
        
        // Date
        textPaint.setTextAlign(Paint.Align.CENTER);
        SimpleDateFormat sdf = new SimpleDateFormat("'Ngày' dd 'tháng' MM 'năm' yyyy", Locale.getDefault());
        canvas.drawText(sdf.format(new Date()), PAGE_WIDTH / 2, y, textPaint);
        y += 30;
        
        textPaint.setTextAlign(Paint.Align.LEFT);
        
        // Bên cho thuê
        canvas.drawText("BÊN CHO THUÊ (Bên A):", MARGIN, y, headerPaint);
        y += 25;
        canvas.drawText("Ông/Bà: " + tenChuTro, MARGIN + 20, y, textPaint);
        y += 40;
        
        // Bên thuê
        canvas.drawText("BÊN THUÊ (Bên B):", MARGIN, y, headerPaint);
        y += 25;
        canvas.drawText("Ông/Bà: " + khachThue.getHoTen(), MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("Số CCCD/CMND: " + (khachThue.getCccd() != null ? khachThue.getCccd() : "..............."), MARGIN + 20, y, textPaint);
        y += 20;
        canvas.drawText("Số điện thoại: " + (khachThue.getSoDienThoai() != null ? khachThue.getSoDienThoai() : "..............."), MARGIN + 20, y, textPaint);
        y += 40;
        
        // Nội dung
        canvas.drawText("NỘI DUNG HỢP ĐỒNG:", MARGIN, y, headerPaint);
        y += 30;
        
        canvas.drawText("Điều 1: BÊN A đồng ý cho BÊN B thuê:", MARGIN, y, textPaint);
        y += 20;
        canvas.drawText("     - Phòng số: " + (phong.getSoPhong() != null ? phong.getSoPhong() : "---"), MARGIN, y, textPaint);
        y += 20;
        canvas.drawText("     - Loại phòng: " + (phong.getLoaiPhong() != null ? phong.getLoaiPhong() : "---"), MARGIN, y, textPaint);
        y += 20;
        canvas.drawText("     - Diện tích: " + phong.getDienTich() + " m²", MARGIN, y, textPaint);
        y += 30;
        
        canvas.drawText("Điều 2: Giá thuê và thanh toán:", MARGIN, y, textPaint);
        y += 20;
        canvas.drawText("     - Giá thuê: " + FormatUtils.formatCurrency(hopDong.getGiaThue()) + "/tháng", MARGIN, y, textPaint);
        y += 20;
        canvas.drawText("     - Tiền đặt cọc: " + FormatUtils.formatCurrency(hopDong.getTienCoc()), MARGIN, y, textPaint);
        y += 30;
        
        canvas.drawText("Điều 3: Thời hạn hợp đồng:", MARGIN, y, textPaint);
        y += 20;
        canvas.drawText("     - Từ ngày: " + hopDong.getNgayBatDau(), MARGIN, y, textPaint);
        y += 20;
        canvas.drawText("     - Đến ngày: " + hopDong.getNgayKetThuc(), MARGIN, y, textPaint);
        y += 40;
        
        // Điều khoản bổ sung
        if (hopDong.getNoiDung() != null && !hopDong.getNoiDung().isEmpty()) {
            canvas.drawText("Điều khoản bổ sung: " + hopDong.getNoiDung(), MARGIN, y, textPaint);
            y += 40;
        }
        
        // Signatures
        y = PAGE_HEIGHT - 180;
        canvas.drawText("BÊN CHO THUÊ (Bên A)", MARGIN + 50, y, headerPaint);
        canvas.drawText("BÊN THUÊ (Bên B)", PAGE_WIDTH - MARGIN - 150, y, headerPaint);
        
        y += 20;
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("(Ký và ghi rõ họ tên)", MARGIN + 100, y, textPaint);
        canvas.drawText("(Ký và ghi rõ họ tên)", PAGE_WIDTH - MARGIN - 100, y, textPaint);
        
        // 1. Chữ ký chủ trọ (Ký sẵn - dùng font chữ cách điệu)
        Paint signPaint = new Paint();
        signPaint.setColor(Color.BLUE);
        signPaint.setTextSize(24);
        signPaint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.ITALIC));
        signPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(tenChuTro, MARGIN + 100, y + 50, signPaint);
        
        // 2. Vẽ chữ ký khách thuê (từ SignatureView)
        if (signature != null) {
            android.graphics.Rect destRect = new android.graphics.Rect(
                PAGE_WIDTH - MARGIN - 180, y + 10, 
                PAGE_WIDTH - MARGIN - 20, y + 90
            );
            canvas.drawBitmap(signature, null, destRect, null);
        }
        
        y += 110;
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(tenChuTro, MARGIN + 100, y, textPaint);
        canvas.drawText(khachThue.getHoTen(), PAGE_WIDTH - MARGIN - 100, y, textPaint);
        
        document.finishPage(page);
        
        // Save file
        String fileName = "HopDong_" + phong.getSoPhong() + "_" + System.currentTimeMillis() + ".pdf";
        File docsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Contracts");
        if (!docsDir.exists()) {
            docsDir.mkdirs();
        }
        
        File file = new File(docsDir, fileName);
        
        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            
            // Share/open file
            Uri uri = FileProvider.getUriForFile(context, 
                    context.getPackageName() + ".fileprovider", file);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            try {
                context.startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                // Fallback: Nếu không có app đọc PDF, cho phép chia sẻ file qua Zalo/Drive/Email
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(shareIntent, "Máy chưa cài ứng dụng đọc PDF. Bạn có muốn gửi file qua:"));
            }
            
            Toast.makeText(context, "Đã tạo hợp đồng: " + fileName, Toast.LENGTH_SHORT).show();
            
        } catch (IOException e) {
            Toast.makeText(context, "Lỗi tạo PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Tạo và xuất hợp đồng thuê phòng dạng PDF
     */
    public static void generateContract(Context context, HopDong hopDong, 
                                         Phong phong, KhachThue khachThue,
                                         String tenChuTro) {
        generateContractWithSignature(context, hopDong, phong, khachThue, tenChuTro, null);
    }
}
