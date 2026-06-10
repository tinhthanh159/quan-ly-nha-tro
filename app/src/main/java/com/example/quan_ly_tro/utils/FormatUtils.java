package com.example.quan_ly_tro.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class cho các hàm format thường dùng
 */
public class FormatUtils {
    
    private static final DecimalFormat currencyFormat = new DecimalFormat("#,###");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
    private static final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM/yyyy", new Locale("vi", "VN"));
    
    /**
     * Format số tiền với dấu phẩy phân cách hàng nghìn
     */
    public static String formatCurrency(double amount) {
        return currencyFormat.format(amount) + " đ";
    }
    
    /**
     * Format số tiền không có đơn vị
     */
    public static String formatNumber(double number) {
        return currencyFormat.format(number);
    }
    
    /**
     * Format timestamp thành ngày/tháng/năm
     */
    public static String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp thành tháng/năm
     */
    public static String formatMonthYear(long timestamp) {
        return monthYearFormat.format(new Date(timestamp));
    }
    
    /**
     * Lấy tháng/năm hiện tại
     */
    public static String getCurrentMonthYear() {
        return monthYearFormat.format(new Date());
    }
    
    /**
     * Lấy tháng hiện tại dưới dạng text
     */
    public static String getCurrentMonthText() {
        Calendar cal = Calendar.getInstance();
        return "Tháng " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
    }
    
    /**
     * Parse currency string về double
     */
    public static double parseCurrency(String text) {
        try {
            String cleanText = text.replaceAll("[^\\d]", "");
            return Double.parseDouble(cleanText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Format diện tích
     */
    public static String formatArea(double area) {
        if (area == (int) area) {
            return (int) area + " m²";
        }
        return String.format(Locale.getDefault(), "%.1f m²", area);
    }
}
