package com.example.langchain4j_springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端页面控制器
 */
@Controller
public class FrontendController {

    /**
     * 返回主页面
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * 返回追踪应用页面
     */
    @GetMapping("/traces")
    public String traces() {
        return "forward:/index.html";
    }
}