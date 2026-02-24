package fpt.org.inblue.controller;

import fpt.org.inblue.constants.ApiPath;
import fpt.org.inblue.model.dto.response.CVParserResponse;
import fpt.org.inblue.model.enums.PythonService;
import fpt.org.inblue.service.PythonApiClient;
import fpt.org.inblue.service.RedisTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static fpt.org.inblue.constants.ApiPath.CV_API;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
public class TestController {

    private final RedisTestService redisTestService;
    private final PythonApiClient pythonApiClient;

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

    @PostMapping(value = "/python-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CVParserResponse testPythonApi(@RequestParam("file") MultipartFile file) {

        CVParserResponse response =
                pythonApiClient.callApi(
                        PythonService.LLM,
                        ApiPath.CV_API,
                        HttpMethod.POST,
                        file,
                        CVParserResponse.class);


        return response;
    }
}











