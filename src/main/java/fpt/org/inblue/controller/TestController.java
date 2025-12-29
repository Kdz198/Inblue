package fpt.org.inblue.controller;

import fpt.org.inblue.service.RedisTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
public class TestController {

    private final RedisTestService redisTestService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Inblue!";
    }

    @GetMapping("/test")
    public String test() {
        return "This is a test endpoint for CI/CD.";
    }

    @GetMapping("/status")
    public String status() {
        return "Application is running smoothly.";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping ("/health")
    public String health() {
        return "OK";
    }

    @PostMapping("/food-test-hash")
    public Object testFoodHash(
            @RequestParam String id,
            @RequestParam String name,
            @RequestParam String cate,
            @RequestParam String country,
            @RequestParam String fieldToUpdate,
            @RequestParam String newValue
    ) {
        RedisTestService.Food food = new RedisTestService.Food(name, cate, country);

        redisTestService.saveFoodAsHash(id, food);

        if (newValue != null && !newValue.isEmpty() && fieldToUpdate != null && !fieldToUpdate.isEmpty()) {
            redisTestService.updateSingleField(id, fieldToUpdate, newValue);
        }

        return redisTestService.getFoodHash(id);
    }
}
