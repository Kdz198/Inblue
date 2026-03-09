package fpt.org.inblue.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTestService {


    private final RedisTemplate<String, Object> redisTemplate;

    public void testLog () {
        throw new RuntimeException("Đây là lỗi test log");
    }
    // Lưu nguyên con Object vào Hash
    // Mỗi field ứng với 1 thuộc tính của Object - giúp update mà ko cần lấy nguyên object ra
    public void saveFoodAsHash(String id, Food food) {
        String key = "food:" + id;

        // Cách thủ công nhưng dễ hiểu nhất: Nhét từng field vào Hash
        // (Thực tế có thể dùng ObjectMapper convert sang Map rồi putAll)
        redisTemplate.opsForHash().put(key, "name", food.name);
        redisTemplate.opsForHash().put(key, "cate", food.cate);
        redisTemplate.opsForHash().put(key, "country", food.country);

        System.out.println("Đã lưu Food vào Hash: " + key);
    }

    public void updateSingleField(String id, String fieldName, String newValue) {
        String key = "food:" + id;
        redisTemplate.opsForHash().put(key, fieldName, newValue);
        System.out.println("Đã update field [" + fieldName + "] thành: " + newValue);
    }

    public Object getFoodHash(String id) {
        String key = "food:" + id;
        return redisTemplate.opsForHash().entries(key);
    }

    public record Food(String name, String cate, String country) {}
}