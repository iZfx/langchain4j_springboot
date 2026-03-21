package com.example.langchain4j_springboot.config;


import com.example.langchain4j_springboot.adapter.SpringAiOllamaEmbeddingAdapter;
import com.example.langchain4j_springboot.functions.LocationNameFunction;
import com.example.langchain4j_springboot.functions.TwoNumSumFunction;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Primary;

import java.util.function.Function;

/**
 * JDK动态代理与AiServices的理解<p>
 * 核心概念解析<p>
 * 1. 接口定义<p>
 * 你定义的 Assistant 接口只是声明了方法契约<p>
 * 接口中没有构造方法，只有方法签名<p>
 * AiServices 会基于这个接口创建动态代理实例<p>
 * 2. AiServices的作用<p>
 * AiServices.builder(Assistant.class) 指定要代理的接口类型<p>
 * 通过链式调用配置各种组件（如 chatModel、contentRetriever 等）<p>
 * 最终 build() 方法返回接口的代理实现<p>
 * 3. 动态代理机制<p>
 * 当调用 assistant.chat(message) 时<p>
 * 代理对象拦截方法调用<p>
 * 根据方法签名和配置的组件执行相应的AI功能<p>
 * 无需手动实现接口方法<p>
 * 4. 关键区别<p>
 * 你不是在构造方法中实现功能<p>
 * 而是通过 AiServices 配置代理行为<p>
 * 接口方法的实现由框架动态生成<p>
 * 总结<p>
 * AiServices 本质上是一个代理工厂，它根据接口定义和配置参数，动态生成具有AI能力的代理对象，实现了面向接口编程的简洁性<p>
 */
@Configuration
public class AiConfig {

    public interface Assistant {
        @SystemMessage("""
                你是“行迹 - 运动热力图”app助手，请你以友好，充满活力，幽默的方式来回复，回复的回答内容尽量言简意赅，不啰嗦，不浮夸；
                你可以一开始提供一些引导式的问题来帮助用户提问，比如：如何生成AppleWatch、IGPSPORT/迹驰、迈金/顽鹿运动、行者、两步路的运动轨迹热力图。
                """)
        String chat(String message);


        // 流式响应
        @SystemMessage("""
                你是“行迹 - 运动热力图”app助手，请你以友好，充满活力，幽默的方式来回复，回复的回答内容尽量言简意赅，不啰嗦，不浮夸；
                你可以一开始提供一些引导式的问题来帮助用户提问，比如：如何生成AppleWatch、IGPSPORT/迹驰、迈金/顽鹿运动、行者、两步路的运动轨迹热力图；
                """)
        TokenStream stream(String message);
    }

    @Bean
    public EmbeddingStore embeddingStore() {
        return new InMemoryEmbeddingStore();
    }

    @Bean
    @Primary
    public ChatModel chatModel() {
        // 手动创建 LangChain4j 的 Ollama ChatModel
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen2:0.5b")
                .build();
    }

    @Bean
    @Primary
    public StreamingChatModel streamingChatModel() {
        // 手动创建 LangChain4j 的 Ollama StreamingChatModel
        return OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen2:0.5b")
                .build();
    }

    @Bean
    @Primary
    public OllamaEmbeddingModel langChain4jOllamaEmbeddingModel() {
        // 手动创建 LangChain4j 的 Ollama EmbeddingModel
        return OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("nomic-embed-text")
                .build();
    }

//    @Bean
//    public EmbeddingModel embeddingModel(OllamaEmbeddingModel ollamaEmbeddingModel) {
//        // 使用适配器将 Spring AI 的 OllamaEmbeddingModel 转换为 LangChain4j 的 EmbeddingModel
//        return new SpringAiOllamaEmbeddingAdapter(ollamaEmbeddingModel);
//    }

    @Bean
    public Assistant assistant(ChatModel ollamaChatModel,
                               StreamingChatModel ollamaStreamingChatModel,
                               EmbeddingStore embeddingStore,
                               OllamaEmbeddingModel ollamaEmbeddingModel) {
        // 对话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
    
        // 内容检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(ollamaEmbeddingModel) // 使用适配器
                .maxResults(5) // 最相似的 5 个结果
                .minScore(0.6) // 只找相似度在 0.6 以上的内容
                .build();
    
        Assistant assistant = AiServices.builder(Assistant.class)
                .contentRetriever(contentRetriever)
                .chatModel(ollamaChatModel)
                .streamingChatModel(ollamaStreamingChatModel)
                .chatMemory(chatMemory)
                .build();
    
        return  assistant;
    }

//    @Bean
//    ChatClient chatClient() {
//        // 使用 @Primary 标记的 ChatModel
//        return ChatClient.builder(chatModel()).build().defaultSystem("你是一个风趣幽默的问答机器人。");
//    }

    // 配置Function对应的bean
    @Bean
    @Description("某个地方有几个叫什么名字的人")  // 功能描述
    Function<LocationNameFunction.Request, LocationNameFunction.Response> locationNameFunction() {
        return new LocationNameFunction();
    }

    @Bean
    @Tool("两个数求和")  // langchain4j功能描述
    @Description("两个数求和")  // spring-ai功能描述
    Function<TwoNumSumFunction.Request, TwoNumSumFunction.Response> twoNumSumFunction() {
        return new TwoNumSumFunction();
    }

    public interface AssistantUnique {

        String chat(@MemoryId int memoryId, @UserMessage String userMessage);
        // 流式响应
        TokenStream stream(@MemoryId int memoryId, @UserMessage String userMessage);
    }

    @Bean
    public AssistantUnique assistantUnique(ChatModel ollamaChatModel,
                                           StreamingChatModel ollamaStreamingChatModel) {

        AssistantUnique assistant = AiServices.builder(AssistantUnique.class)
                .chatModel(ollamaChatModel)
                .streamingChatModel(ollamaStreamingChatModel)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder().maxMessages(10)
                                .id(memoryId).build()
                )
                .build();

        return assistant;
    }
}
