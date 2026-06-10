package com.example.quan_ly_tro.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * Utility class để tạo và xử lý QR Code
 */
public class QrCodeUtils {
    
    private static final int QR_SIZE = 512;
    
    /**
     * Tạo QR Code bitmap từ nội dung
     * @param content Nội dung để encode
     * @return Bitmap QR Code hoặc null nếu lỗi
     */
    public static Bitmap generateQrCode(String content) {
        return generateQrCode(content, QR_SIZE);
    }
    
    /**
     * Tạo QR Code bitmap với kích thước tùy chỉnh
     * @param content Nội dung để encode
     * @param size Kích thước (width = height)
     * @return Bitmap QR Code hoặc null nếu lỗi
     */
    public static Bitmap generateQrCode(String content, int size) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    size,
                    size
            );
            
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[y * width + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
            
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
            
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Tạo nội dung QR Code cho phòng
     * @param phongId ID phòng
     * @param tenPhong Tên phòng
     * @param giaThue Giá thuê
     * @param trangThai Trạng thái
     * @return JSON string chứa thông tin phòng
     */
    public static String createRoomQrContent(int phongId, String tenPhong, double giaThue, String trangThai) {
        return String.format(
                "{\"type\":\"ROOM\",\"id\":%d,\"name\":\"%s\",\"price\":%.0f,\"status\":\"%s\"}",
                phongId, tenPhong, giaThue, trangThai
        );
    }
    
    /**
     * Tạo nội dung QR Code cho hóa đơn
     * @param hoaDonId ID hóa đơn
     * @param tenPhong Tên phòng
     * @param thangNam Tháng/năm
     * @param tongTien Tổng tiền
     * @return JSON string chứa thông tin hóa đơn
     */
    public static String createInvoiceQrContent(int hoaDonId, String tenPhong, String thangNam, double tongTien) {
        return String.format(
                "{\"type\":\"INVOICE\",\"id\":%d,\"room\":\"%s\",\"period\":\"%s\",\"total\":%.0f}",
                hoaDonId, tenPhong, thangNam, tongTien
        );
    }
}
