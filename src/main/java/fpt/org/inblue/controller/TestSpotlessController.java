package fpt.org.inblue.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController public class TestSpotlessController {
@GetMapping("/api/test-spotless")
public String test(){
int a=1;int b=2;
if(a==1){return "Hello";}else{return "World";}
}
}
