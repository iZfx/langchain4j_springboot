package com.example.langchain4j_springboot;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class Langchain4jSpringbootApplication {

    public static void main(String[] args) {
        System.out.println(new Date() + " 开始启动主程序！");

        SpringApplication.run(Langchain4jSpringbootApplication.class, args);
    }

    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(QwenEmbeddingModel qwenEmbeddingModel,
                                                       EmbeddingStore embeddingStore) throws URISyntaxException {
        System.out.println(new Date() + " rag开始向量化！");

        // 读取
        return args -> {
            Document document = ClassPathDocumentLoader.loadDocument("rag/terms-of-service.txt", new TextDocumentParser());

            DocumentByLineSplitter splitter = new DocumentByLineSplitter(
                    100,
                    10
            );
            List<TextSegment> segments = splitter.split(document);

            // 向量化
            List<Embedding> embeddings = qwenEmbeddingModel.embedAll(segments).content();

            // 存入
            embeddingStore.addAll(embeddings,segments);
            System.out.println(new Date() + " rag向量化完成！");

        };
    }

}
