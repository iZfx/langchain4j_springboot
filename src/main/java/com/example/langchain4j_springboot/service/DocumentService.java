package com.example.langchain4j_springboot.service;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GitHub文档服务类
 * 负责从GitHub获取文本内容并进行向量化处理
 */
@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Value("${github.document.url:https://raw.githubusercontent.com/iZfx/KnowledgeBase/main/SportHeatMap.md}")
    private String githubDocumentUrl;

    @Value("${local.document.url:rag/SportHeatMapKnowledge.md}")
    private String localDocumentUrl;

    @Value("${document.splitter.max-segment-size:200}")
    private int maxSegmentSize;

    @Value("${document.splitter.min-segment-size:20}")
    private int minSegmentSize;

    private final OllamaEmbeddingModel ollamaEmbeddingModel;
    private final EmbeddingStore embeddingStore;

    @Autowired
    public DocumentService(OllamaEmbeddingModel ollamaEmbeddingModel, EmbeddingStore embeddingStore) {
        this.ollamaEmbeddingModel = ollamaEmbeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /**
     * 从GitHub获取文档内容并进行向量化处理
     * 
     * @param source 来源标识（如"startup"表示启动时，"scheduled"表示定时任务）
     * @return 处理是否成功
     */
    public boolean loadAndVectorizeDocument(String source) {
        try {
            logger.info("[{}] 开始从GitHub获取文档内容: {}", source, githubDocumentUrl);
            
            // 记录开始时间
            LocalDateTime startTime = LocalDateTime.now();
            logger.info("[{}] 开始时间: {}", source, startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            Document document;
            try {
                // 从GitHub URL加载文档
                String documentContent = fetchDocumentFromUrl(githubDocumentUrl);
                if (documentContent == null || documentContent.isEmpty()) {
                    logger.warn("[{}] 从GitHub获取的文档内容为空", source);
                    // return false;
                    // 返回为空抛异常提示
                    throw new RuntimeException(String.format("[%s] 从GitHub获取的文档内容为空", source));
                }

                document = Document.from(documentContent);
            } catch (Exception e) {
                logger.error("[{}] 从GitHub获取文档内容时发生错误: {}", source, e.getMessage(), e);
                logger.info("[{}] 开始从内部获取文档内容: {}", source, localDocumentUrl);
                document = ClassPathDocumentLoader.loadDocument(localDocumentUrl, new TextDocumentParser());
            }


            logger.info("[{}] 成功获取文档，内容长度: {} 字符", source, document.text().length());
            logger.info("[{}] 成功获取文档，内容: \n{}", source, document.text());

            // 清空现有的嵌入存储（可选，根据需求决定是否保留历史数据）
            embeddingStore.removeAll(); // 如果需要完全替换可以取消注释

            // 分割文档
            DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(maxSegmentSize, minSegmentSize);
            List<TextSegment> segments = splitter.split(document);

            logger.info("[{}] 文档分割完成，共 {} 个片段", source, segments.size());

            // 向量化处理
            List<Embedding> embeddings = ollamaEmbeddingModel.embedAll(segments).content();

            // 存储到嵌入存储中
            embeddingStore.addAll(embeddings, segments);

            LocalDateTime endTime = LocalDateTime.now();
            logger.info("[{}] 向量化处理完成！结束时间: {}, 总耗时: {}ms", 
                       source, 
                       endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                       java.time.Duration.between(startTime, endTime).toMillis());

            return true;

        } catch (Exception e) {
            logger.error("[{}] 处理知识库文档时发生错误: {}", source, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取当前配置的GitHub文档URL
     * 
     * @return GitHub文档URL
     */
    public String getGithubDocumentUrl() {
        return githubDocumentUrl;
    }

    /**
     * 设置新的GitHub文档URL（运行时动态配置）
     * 
     * @param url 新的GitHub文档URL
     */
    public void setGithubDocumentUrl(String url) {
        this.githubDocumentUrl = url;
        logger.info("GitHub文档URL已更新为: {}", url);
    }

    /**
     * 从URL获取文档内容
     * 自动处理GitHub网页URL转换为原始文件URL
     * 
     * @param urlString GitHub文件URL（支持网页URL和原始URL）
     * @return 文档内容字符串
     */
    private String fetchDocumentFromUrl(String urlString) {
        try {
            // 自动转换GitHub网页URL为原始文件URL
            String rawUrl = convertToRawUrl(urlString);
            logger.info("转换后的原始URL: {}", rawUrl);
            
            URL url = new URL(rawUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            // 添加User-Agent避免被GitHub阻止
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; LangChain4j Bot)");
            
            int responseCode = connection.getResponseCode();
            logger.info("HTTP响应状态码: {}", responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
                );
                
                String content = reader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));
                
                reader.close();
                connection.disconnect();
                
                // 检查是否获取到了HTML内容
                if (content.trim().startsWith("<") && content.contains("<html")) {
                    logger.warn("获取到的是HTML页面而非纯文本，URL可能不正确: {}", rawUrl);
                    return null;
                }
                
                logger.info("成功获取文本内容，长度: {} 字符", content.length());
                return content;
            } else {
                logger.error("HTTP请求失败，状态码: {}，URL: {}", responseCode, rawUrl);
                return null;
            }
        } catch (Exception e) {
            logger.error("从URL获取文档内容时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 将GitHub网页URL转换为原始文件URL
     * 
     * @param url GitHub网页URL
     * @return 原始文件URL
     */
    private String convertToRawUrl(String url) {
        // 如果已经是原始URL格式，直接返回
        if (url.contains("raw.githubusercontent.com")) {
            return url;
        }
        
        // 转换GitHub网页URL为原始URL
        // https://github.com/user/repo/blob/branch/path/file.txt
        // -> https://raw.githubusercontent.com/user/repo/branch/path/file.txt
        if (url.contains("github.com") && url.contains("/blob/")) {
            return url.replace("github.com", "raw.githubusercontent.com")
                     .replace("/blob/", "/");
        }
        
        return url; // 如果无法识别格式，返回原URL
    }
}