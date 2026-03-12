package com.example.langchain4j_springboot.scheduler;

import com.example.langchain4j_springboot.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 文档更新定时任务调度器
 * 负责定时执行GitHub文档的自动更新任务
 */
@Component
public class Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);

    @Autowired
    private DocumentService documentService;

    /**
     * 是否跳过定时任务执行
     * true - 跳过执行，false - 正常执行
     *
     * 使用方式
     * 方式1：修改配置文件
     * 在 application.properties 中设置：scheduler.document.update.skip=true  # 跳过定时任务
     * 方式2：环境变量覆盖
     * export SCHEDULER_DOCUMENT_UPDATE_SKIP=true
     * 方式3：启动参数
     * java -jar app.jar --scheduler.document.update.skip=true
     */
    @Value("${scheduler.document.update.skip:true}")
    private boolean skipScheduledUpdate;

    /**
     * 定时任务：每天 0点、4点、8点、12点、16点、20点 执行文档更新
     * cron表达式：秒 分 时 * * ?
     * 0 0 0,4,8,12,16,20 * * ? 表示在指定时间点的0分0秒执行
     */
    @Scheduled(cron = "0 0 0,4,8,12,16,20 * * ?")
    public void scheduledDocumentUpdate() {
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 检查是否跳过定时任务
        if (skipScheduledUpdate) {
            logger.info("定时任务触发 - 当前时间: {}，但已配置跳过执行，本次不执行文档更新业务逻辑", currentTime);
            return;
        }
        
        logger.info("定时任务触发 - 当前时间: {}，开始执行GitHub文档更新", currentTime);

        boolean success = documentService.loadAndVectorizeDocument("scheduled");
        if (success) {
            logger.info("定时任务执行成功 - GitHub文档已更新");
        } else {
            logger.error("定时任务执行失败 - GitHub文档更新失败");
        }
    }
}