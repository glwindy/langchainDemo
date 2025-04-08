package com.demo;

import com.demo.config.AiConfig;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;

public class TestDemo {

    @Test
    public void testEmbedding() {
        // 向量模型
        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey("sk-a4e39e86dfbf49f79e9ca1ab6e745730")
                .build();

        // 文本向量化
        Response<Embedding> embed = embeddingModel.embed("你好，我是平果果");
        System.out.println(embed.content().toString());
        System.out.println(embed.content().vector().length);
    }


    @Test
    public void testEmbeddingStore() {
        // -----embedding阶段-------------
        InMemoryEmbeddingStore<TextSegment> embeddingStore
                = new InMemoryEmbeddingStore<>();

        // 创建向量模型
        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey("sk-a4e39e86dfbf49f79e9ca1ab6e745730")
                .build();

        // 利用向量模型进行向量化，然后存储向量到向量数据库
        TextSegment segment1 = TextSegment.from("""
                预定航班：
                - 通过我们的网站或移动应用程序预定。
                - 预定时需要全额付款。
                - 确保个人信息（姓名、ID）准确行，因为更正可能会产生25的费用
                """);
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        embeddingStore.add(embedding1, segment1);

        // 利用向量模型进行向量化，然后存储向量到向量数据库
        TextSegment segment2 = TextSegment.from("""
                取消预定：
                - 最晚再航班起飞前48小时取消。
                - 取消费用：经济舱75￥，豪华经济舱50￥，商务舱25￥。
                - 退款将在7个工作日内处理。
                """);
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        embeddingStore.add(embedding2, segment2);

        // -----检索阶段-------------

        // 需要查询的内容，向量化
        Embedding queryEmbedding = embeddingModel.embed("退票要多少钱").content();

        // 去向量数据库查询
        // 构建查询条件
        EmbeddingSearchRequest build = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(1)
                .minScore(0.7)
                .build();
        EmbeddingSearchResult<TextSegment> segmentEmbeddingSearchResult = embeddingStore.search(build);
        segmentEmbeddingSearchResult.matches().forEach(embeddingMatch -> {
            System.out.println(embeddingMatch.score());
            System.out.println(embeddingMatch.embedded().text());
        });
    }

    @Test
    public void testEmbeddingLLM() {
        // -----embedding阶段-------------
        InMemoryEmbeddingStore<TextSegment> embeddingStore
                = new InMemoryEmbeddingStore<>();

        // 创建向量模型
        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey("sk-a4e39e86dfbf49f79e9ca1ab6e745730")
                .build();

        // 利用向量模型进行向量化，然后存储向量到向量数据库
        TextSegment segment1 = TextSegment.from("""
                预定航班：
                - 通过我们的网站或移动应用程序预定。
                - 预定时需要全额付款。
                - 确保个人信息（姓名、ID）准确行，因为更正可能会产生25的费用
                """);
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        embeddingStore.add(embedding1, segment1);

        // 利用向量模型进行向量化，然后存储向量到向量数据库
        TextSegment segment2 = TextSegment.from("""
                取消预定：
                - 最晚再航班起飞前48小时取消。
                - 取消费用：经济舱75￥，豪华经济舱50￥，商务舱25￥。
                - 退款将在7个工作日内处理。
                """);
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        embeddingStore.add(embedding2, segment2);

        //-----检索增强阶段-----
        ChatLanguageModel model = QwenChatModel.builder()
                .apiKey("sk-a4e39e86dfbf49f79e9ca1ab6e745730")
                .modelName("qwen-max")
                .build();

        // 创建内容检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever
                .builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(1)
                .minScore(0.7)
                .build();

        AiConfig.Assistant assistant = AiServices.builder(AiConfig.Assistant.class)
                .chatLanguageModel(model)
                .contentRetriever(contentRetriever)
                .build();

        System.out.println(assistant.chat("退费费用"));
    }
}
