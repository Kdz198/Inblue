package fpt.org.inblue.utils;

import java.util.concurrent.ThreadLocalRandom;

public class HelperUtil {
    public static long generateUniqueOrderCode() {
        long timestamp = System.currentTimeMillis() % 10000000L; // Lấy 9 số cuối của timestamp
        int randomSuffix = ThreadLocalRandom.current().nextInt(100, 999); // Thêm 3 số ngẫu nhiên
        return Long.parseLong(timestamp + "" + randomSuffix);
    }

    /**
     * Lấy 3 chữ số đầu tiên của orderCode dưới dạng String
     * @param orderCode mã số từ PayOS (ví dụ: 100234567)
     * @return 3 số đầu (ví dụ: "100"), hoặc chuỗi gốc nếu độ dài < 3
     */
    public static String getPrefix(String orderCode) {
        if (orderCode == null) {
            return "";
        }

        // 2. Chuyển Long sang String

        // 3. Lấy 3 ký tự đầu tiên
        if (orderCode.length() >= 3) {
            return orderCode.substring(0, 3);
        }

        return orderCode; // Trả về chuỗi gốc nếu không đủ 3 ký tự
    }
}
