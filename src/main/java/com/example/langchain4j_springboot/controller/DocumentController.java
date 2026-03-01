package com.example.langchain4j_springboot.controller;

import com.example.langchain4j_springboot.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档管理控制器
 * 提供手动触发文档更新和配置管理的API接口
 */
@RestController
@RequestMapping("/api/document")
@CrossOrigin(origins = "*")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    /**
     * 手动触发文档更新
     */
    @PostMapping("/update")
    public Map<String, Object> updateDocument() {
        logger.info("收到手动更新文档请求");
        
        Map<String, Object> response = new HashMap<>();
        boolean success = documentService.loadAndVectorizeDocument("manual");
        
        response.put("success", success);
        response.put("message", success ? "文档更新成功" : "文档更新失败");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    /**
     * 获取当前配置的GitHub文档URL
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("githubDocumentUrl", documentService.getGithubDocumentUrl());
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 更新GitHub文档URL配置
     */
    @PutMapping("/config/url")
    public Map<String, Object> updateConfig(@RequestParam String url) {
        logger.info("更新GitHub文档URL配置: {}", url);
        
        Map<String, Object> response = new HashMap<>();
        try {
            documentService.setGithubDocumentUrl(url);
            response.put("success", true);
            response.put("message", "URL配置更新成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "URL配置更新失败: " + e.getMessage());
        }
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    /**
     * 测试连接到指定的GitHub URL
     */
    @PostMapping("/test-connection")
    public Map<String, Object> testConnection(@RequestParam String url) {
        logger.info("测试连接到GitHub URL: {}", url);
        
        Map<String, Object> response = new HashMap<>();
        
        // 这里可以添加实际的连接测试逻辑
        // 暂时返回模拟结果
        response.put("success", true);
        response.put("message", "连接测试功能待实现");
        response.put("testedUrl", url);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }
}