package com.example.langchain4j_springboot.adapter;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.springframework.ai.ollama.OllamaEmbeddingModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI OllamaEmbeddingModel 到 LangChain4j EmbeddingModel 的适配器
 */
public class SpringAiOllamaEmbeddingAdapter implements EmbeddingModel {

    private final OllamaEmbeddingModel delegate;

    public SpringAiOllamaEmbeddingAdapter(OllamaEmbeddingModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        List<Embedding> embeddings = new ArrayList<>();
        
        for (TextSegment segment : textSegments) {
            // 调用 Spring AI 的 embedding 方法
            float[] embedding = delegate.embed(segment.text());
            embeddings.add(Embedding.from(embedding));
        }
        
        return Response.from(embeddings);
    }

    @Override
    public int dimension() {
        // nomic-embed-text 的维度是 768
        return 768;
    }
}
