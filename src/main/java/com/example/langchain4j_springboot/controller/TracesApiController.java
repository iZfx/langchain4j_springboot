package com.example.langchain4j_springboot.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 运动轨迹相关的API接口
 */
@RestController
@RequestMapping("/api/traces")
@CrossOrigin(origins = "*")
public class TracesApiController {

    /**
     * 获取运动统计数据
     */
    @RequestMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDistance", "1,248 km");
        stats.put("totalTime", "86小时32分钟");
        stats.put("totalTraces", 142);
        stats.put("citiesVisited", 23);
        stats.put("achievementRate", "96%");
        return stats;
    }

    /**
     * 获取热力图数据
     */
    @RequestMapping("/heatmap-data")
    public Map<String, Object> getHeatmapData() {
        Map<String, Object> heatmapData = new HashMap<>();
        // 模拟热力图数据
        heatmapData.put("points", new double[][]{
            {39.9042, 116.4074, 0.8},
            {31.2304, 121.4737, 0.6},
            {23.1291, 113.2644, 0.7},
            {30.5728, 104.0668, 0.5},
            {18.8024, 121.5345, 0.4},
            {24.1771, 120.6052, 0.3}
        });
        return heatmapData;
    }
}