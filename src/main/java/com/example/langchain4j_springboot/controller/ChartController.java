package com.example.langchain4j_springboot.controller;

import com.example.langchain4j_springboot.config.AiConfig;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@RestController
@RequestMapping("/ai")
public class ChartController {

    @Autowired
    private QwenChatModel qwenChatModel;

    @Autowired
    QwenStreamingChatModel qwenStreamingChatModel;

    @Autowired
    private AiConfig.Assistant assistant;

    @RequestMapping("/chat")
    public String chat(@RequestParam(defaultValue = "你是谁？") String message) {
        String chat = qwenChatModel.chat(message);
        return chat;
    }

    @RequestMapping(value = "/stream_chat", produces = "text/stream; charset = UTF-8")
    public Flux<String> streamChat(@RequestParam(defaultValue = "你是谁？") String message) {
        Flux<String> fluxsink = Flux.create(sink -> {
            qwenStreamingChatModel.chat(message, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    sink.next(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    sink.complete();
                }

                @Override
                public void onError(Throwable error) {
                    sink.error(error);
                }
            });
        });
        return fluxsink;
    }

    @RequestMapping(value = "/memory_stream_chat", produces = "text/stream; charset = UTF-8")
    public Flux<String> memoryStreamChat(@RequestParam(defaultValue = "你是谁？") String message) {
        System.out.println("请求/ai/memory_stream_chat，请求内容：" + message);

        TokenStream stream = assistant.stream( message);

        return Flux.create(sink -> {
            stream.onPartialResponse(s -> sink.next(s))
                    .onCompleteResponse(c -> sink.complete())
                    .onError(sink::error)
                    .start();

        });
    }
}
