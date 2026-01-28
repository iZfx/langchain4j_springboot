package com.example.langchain4j_springboot.controller.springai;


import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/spring-ai")
public class FunctionCallController {

    @Autowired
    private ChatModel chatModel;

    @GetMapping("/function-call")
    public String functionCall(@RequestParam(value = "message", defaultValue = "京北市有几个叫小明的人", required = false) String message) {
        System.out.println(new Date() + " 请求/spring-ai/function-call，请求内容：" + message);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .toolNames("locationNameFunction")
                .model("gpt-3.5-turbo-0613")
                .build();

        ChatResponse chatResponse = chatModel.call(new Prompt(message,  options));

        return chatResponse.getResult().getOutput().getText();

    }
}
