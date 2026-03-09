package fpt.org.inblue.utils;

import java.util.concurrent.ThreadLocalRandom;

public class HelperUtil {
    public static long generateUniqueOrderCode() {
        long timestamp = System.currentTimeMillis() % 1000000000L; // Lấy 9 số cuối của timestamp
        int randomSuffix = ThreadLocalRandom.current().nextInt(100, 999); // Thêm 3 số ngẫu nhiên
        return Long.parseLong(timestamp + "" + randomSuffix);
    }

}
