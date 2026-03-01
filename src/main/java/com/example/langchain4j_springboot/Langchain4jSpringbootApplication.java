package com.example.langchain4j_springboot;

import com.example.langchain4j_springboot.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class Langchain4jSpringbootApplication {

    private static final Logger logger = LoggerFactory.getLogger(Langchain4jSpringbootApplication.class);

    @Autowired
    private DocumentService documentService;

    public static void main(String[] args) {
        logger.info("开始启动主程序！");
        SpringApplication.run(Langchain4jSpringbootApplication.class, args);
    }

    /**
     * 应用启动时执行的向量化任务
     */
    @Bean
    CommandLineRunner ingestDocumentOnStartup() {
        return args -> {
            logger.info("应用启动，开始执行初始文档向量化...");
            boolean success = documentService.loadAndVectorizeDocument("startup");
            if (success) {
                logger.info("应用启动时的文档向量化处理完成！");
            } else {
                logger.warn("应用启动时的文档向量化处理失败！");
            }
        };
    }


}