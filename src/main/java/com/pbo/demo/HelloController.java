package com.pbo.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @GetMapping("/")
    @ResponseBody
    public String haloDunia() {
        return "Hello, World! Selamat datang di Spring Boot PBO_TRPL 001.";
    }


}