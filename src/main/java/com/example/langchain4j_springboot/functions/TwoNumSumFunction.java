package com.example.langchain4j_springboot.functions;


import java.util.Date;
import java.util.function.Function;

public class TwoNumSumFunction implements Function<TwoNumSumFunction.Request, TwoNumSumFunction.Response> {
    @Override
    public TwoNumSumFunction.Response apply(Request request) {
        double sum = request.num1() + request.num2();
        System.out.println(new Date() + " " + request.num1() + "与" + request.num2() + "的和为：" + sum);
        return new Response(request.num1() + "与" + request.num2() + "的和为：" + sum);
    }

    // 密封类 负责告诉GPT要提取哪些关键信息，接受GPT提取后的信息
    public record Request(double num1, double num2) {
    }

    // 密封类 负责告诉GPT要返回哪些信息
    public record Response(String message) {
    }
}
