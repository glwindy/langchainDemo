package com.demo.config;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    public interface Assistant {
        String chat(String message);

        // 流式响应
        TokenStream stream(String message);
    }

    // 区分不同用户的对话
    public interface AssistantUnique {

        String chat(@MemoryId int memoryId, @UserMessage String userMessage);

        TokenStream stream(@MemoryId int memoryId, @UserMessage String userMessage);
    }

    @Bean
    public Assistant assistant(ChatLanguageModel qwenChatModel,
                               StreamingChatLanguageModel qwenStreamingChatModel) {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10); // 最大存储对话记录数量
        /**
         * 操作步骤：
         * 1、为Assistant动态代理对象 chat
         * 2、对话内容存储到ChatMemory
         * 3、聊天记录从chatMemory取出来
         * 4、放入当前对话中
         */
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .build();
        return assistant;
    }


    @Bean
    public Assistant assistant(ChatLanguageModel qwenChatModel,
                               StreamingChatLanguageModel qwenStreamingChatModel,
                               EmbeddingStore embeddingStore,
                               QwenEmbeddingModel qwenEmbeddingModel) {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10); // 最大存储对话记录数量

        // 内容检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever
                .builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(1)
                .minScore(0.65)
                .build();

        /**
         * 操作步骤：
         * 1、为Assistant动态代理对象 chat
         * 2、对话内容存储到ChatMemory
         * 3、聊天记录从chatMemory取出来
         * 4、放入当前对话中
         */
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .contentRetriever(contentRetriever)
                .build();
        return assistant;
    }


    @Bean
    public AssistantUnique assistantUnique(ChatLanguageModel qwenChatModel,
                                           StreamingChatLanguageModel qwenStreamingChatModel) {
        AssistantUnique assistantUnique = AiServices.builder(AssistantUnique.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder().maxMessages(10)
                                .id(memoryId).build()
                        ).build();

        return assistantUnique;
    }

    @Bean
    public EmbeddingStore embeddingStore() {
        return new InMemoryEmbeddingStore();
    }

}
