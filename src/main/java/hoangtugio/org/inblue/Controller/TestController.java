package hoangtugio.org.inblue.Controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Inblue!";
    }

    @GetMapping("/test")
    public String test() {
        return "This is a test endpoint for CI/CD.";
    }
}
