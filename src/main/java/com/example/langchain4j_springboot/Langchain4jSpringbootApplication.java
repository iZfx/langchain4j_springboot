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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(Langchain4jSpringbootApplication.class);

    public static void main(String[] args) {
        logger.info("开始启动主程序！");

        SpringApplication.run(Langchain4jSpringbootApplication.class, args);
    }

    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(QwenEmbeddingModel qwenEmbeddingModel,
                                                       EmbeddingStore embeddingStore) throws URISyntaxException {
        logger.info("rag向量化开始！");

        // 读取
        return args -> {
            Document document = ClassPathDocumentLoader.loadDocument("rag/terms-of-service.txt", new TextDocumentParser());

            DocumentByLineSplitter splitter = new DocumentByLineSplitter(
                    100,
                    10
            );
            List<TextSegment> segments = splitter.split(document);

            // 将TextSegment转为Embedding并存入EmbeddingStore
            List<Embedding> embeddings = qwenEmbeddingModel.embedAll(segments).content();

            // 批量存储
            embeddingStore.addAll(embeddings, segments);

            logger.info("rag向量化完成！");
        };
    }
}