package com.example.langchain4j_springboot.controller.springai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Date;

@RestController
@RequestMapping("/spring-ai")
class SpringAiController {

    private static final Logger logger = LoggerFactory.getLogger(SpringAiController.class);

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private OpenAiImageModel openaiImageModel;

//    public SpringAiController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }

    @GetMapping("/chat")
    String chat(@RequestParam(value = "message", defaultValue = "你是谁？", required = true) String message) {
        logger.info("请求/spring-ai/chat，请求内容：{}", message);
        return this.chatClient
                .prompt()   // 提示词
                .user(message)  // 用户输入信息
                .call() // 请求大模型
                .content(); // 返回文本
    }

    // 流式输出
    @GetMapping(value = "/stream-chat", produces = "text/stream; charset = UTF-8")
    Flux<String> streamChat(@RequestParam(value = "message", defaultValue = "你是谁？", required = true) String message) {
        logger.info("请求/spring-ai/stream-chat，请求内容：{}", message);
        Flux<String> output = chatClient.prompt()
                .user("message")
                .stream()
                .content();
        return output;
    }

    // 文生图
    @GetMapping("/text2image")
    String text2image(@RequestParam(value = "message", defaultValue = "画一只棕色的可爱小猫", required = true) String message) {
        logger.info("请求/spring-ai/text2image，请求内容：{}", message);
        ImageResponse response = openaiImageModel.call(
                new ImagePrompt(message,
                        OpenAiImageOptions.builder()
                                .quality("hd")
                                .N(4)
                                .height(1024)
                                .width(1024).build())

        );
        return response.getResult().getOutput().getUrl();
    }


}