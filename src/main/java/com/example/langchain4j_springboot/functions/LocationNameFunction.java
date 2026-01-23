package com.example.langchain4j_springboot.functions;

import java.util.function.Function;

public class LocationNameFunction implements Function<LocationNameFunction.Request, LocationNameFunction.Response> {

    // 接受GPT提取后的信息(自动调用该方法)
    @Override
    public Response apply(Request request) {

        if (request == null || request.location().equals("") || request.name().equals("")) {
            return new Response("请提供有效的参数");
        }

        // 真实调用Function对应的第三方功能接口
        // ...

        return new Response("返回的函数结果：京北市有5个叫小明的人");  // 返回示例
    }

    // 密封类 负责告诉GPT要提取哪些关键信息，接受GPT提取后的信息
    public record Request(String location, String name) {
    }

    // 密封类 负责告诉GPT要返回哪些信息
    public record Response(String message) {
    }
}
