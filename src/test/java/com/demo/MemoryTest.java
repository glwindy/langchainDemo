package com.demo;

import com.demo.tokenizer.CustomQwenTokenizer;
import dev.langchain4j.chain.ConversationalChain;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 多轮对话测试
 */
public class MemoryTest {

    @Test
    public void MessageChatMemoryTest() {
        // 初始化 OpenAI 模型
        ChatLanguageModel model = QwenChatModel
                .builder()
                //.baseUrl("https://dashscope.aliyuncs.com")
                .apiKey("sk-a4e39e86dfbf49f79e9ca1ab6e745730")
                .modelName("qwen-max")
                .build();
        // 创建一个 MessageWindowChatMemory，最多保留 10 条消息
        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);

        // 创建对话链
        ConversationalChain chain = ConversationalChain.builder()
                .chatLanguageModel(model)
                .chatMemory(memory)
                .build();

        // 进行 10 轮对话
        for (int i = 1; i <= 10; i++) {
            String userInput = "这是第 " + i + " 轮对话。";
            System.out.println("用户输入: " + userInput);

            // 发送用户消息并获取 AI 回复
            String aiResponse = chain.execute(userInput);
            System.out.println("AI 回复: " + aiResponse);
            System.out.println();
        }

        // 打印对话历史
        List<ChatMessage> history = memory.messages();
        System.out.println("对话历史:");
        for (ChatMessage message : history) {
            if (message instanceof UserMessage) {
                System.out.println("用户: " + ((UserMessage) message).singleText());
            } else if (message instanceof AiMessage) {
                System.out.println("AI: " + ((AiMessage) message).text());
            }
        }
    }


    @Test
    public void tokenChatMemoryTest() {
        // 初始化 OpenAI 模型
        ChatLanguageModel model = QwenChatModel
                .builder()
                //.baseUrl("https://dashscope.aliyuncs.com")
                .apiKey("sk-a4e39e86dfbf49f79e9ca1ab6e745730")
                .modelName("qwen-max")
                .build();

        // 设置最大 token 数
        int maxTokens = 200;
        // 创建通义千问的 Tokenizer
        Tokenizer tokenizer = new CustomQwenTokenizer();

        // 创建 TokenWindowChatMemory，设置最大 token 数并指定 token 计数器，若对话历史中token超出最大值，会自动移除较早的消息，
        // 直到token数降到上限以下
        TokenWindowChatMemory memory = TokenWindowChatMemory.builder()
                .maxTokens(maxTokens, tokenizer)
                //.tokenCounter(model::countTokens)
                .build();

        // 创建对话链
        ConversationalChain chain = ConversationalChain.builder()
                .chatLanguageModel(model)
                .chatMemory(memory)
                .build();

        // 模拟多轮对话
        String[] userInputs = {
                "介绍下华为最mate系列手机，要3款即可",
                "它的价格分别是多少",
                "适合什么样的人群"
        };
//        String[] userInputs = {
//                "介绍一下人工智能",
//                "人工智能有哪些应用领域",
//                "深度学习在人工智能中的作用是什么"
//        };

        for (String userInput : userInputs) {
            System.out.println("用户输入: " + userInput);
            // 发送用户消息并获取 AI 回复
            String aiResponse = chain.execute(userInput);
            System.out.println("AI 回复: " + aiResponse);
            System.out.println();
        }

        // 打印对话历史
        List<ChatMessage> history = memory.messages();
        System.out.println("对话历史:");
        for (ChatMessage message : history) {
            if (message instanceof UserMessage) {
                System.out.println("用户: " + ((UserMessage) message).singleText());
            } else if (message instanceof AiMessage) {
                System.out.println("AI: " + ((AiMessage) message).text());
            }
        }
    }

}
