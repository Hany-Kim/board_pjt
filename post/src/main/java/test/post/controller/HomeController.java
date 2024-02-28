package test.post.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/") // '/'기본 주소 요청이 오면
    public String index(){ // 함수 호출 후

        return "index"; // index.html을 찾아감
    }
}
