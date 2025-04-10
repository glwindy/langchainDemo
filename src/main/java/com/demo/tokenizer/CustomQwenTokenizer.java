package com.demo.tokenizer;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.Tokenizer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CustomQwenTokenizer implements Tokenizer {

    public int countTokens(String text) {
        // 这里简单地按空格分割文本作为示例，实际中需要根据通义千问的分词规则调整
        return text.split("\\s+").length;
    }

    public String decode(List<Integer> tokenIds) {
        // 简单示例，实际需要根据通义千问编码规则实现
        StringBuilder result = new StringBuilder();
        for (Integer tokenId : tokenIds) {
            result.append(tokenId).append(" ");
        }
        return result.toString().trim();
    }

    public List<Integer> encode(String text) {
        // 简单示例，实际需要根据通义千问编码规则实现
        String[] tokens = text.split("\\s+");
        Integer[] tokenIds = new Integer[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            tokenIds[i] = i;
        }
        return Arrays.asList(tokenIds);
    }

    /**
     * 估算文本中的 token 数量
     * @param text 输入的文本
     * @return token 数量
     */
    @Override
    public int estimateTokenCountInText(String text) {
        return countTokens(text);
    }


    /**
     * 估算消息中的 token 数量
     * @param message 聊天消息
     * @return token 数量
     */
    @Override
    public int estimateTokenCountInMessage(ChatMessage message) {
        return countTokens(message.toString());
    }


    /**
     * 估算消息集合中的 token 总数量
     * @param iterable 聊天消息集合
     * @return token 总数量
     */
    @Override
    public int estimateTokenCountInMessages(Iterable<ChatMessage> iterable) {
        int totalTokens = 0;
        Iterator<ChatMessage> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            ChatMessage message = iterator.next();
            totalTokens += estimateTokenCountInMessage(message);
        }
        return totalTokens;
    }

    /**
     * 估算工具规范集合中的 token 总数量
     * @param iterable 工具规范集合
     * @return token 总数量
     */
    @Override
    public int estimateTokenCountInToolSpecifications(Iterable<ToolSpecification> iterable) {
        int totalTokens = 0;
        Iterator<ToolSpecification> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            ToolSpecification toolSpecification = iterator.next();
            // 假设工具规范的名称和描述需要计算 token 数量
            totalTokens += countTokens(toolSpecification.name());
            totalTokens += countTokens(toolSpecification.description());
        }
        return totalTokens;
    }


    /**
     * 估算工具执行请求集合中的 token 总数量
     * @param iterable 工具执行请求集合
     * @return token 总数量
     */
    @Override
    public int estimateTokenCountInToolExecutionRequests(Iterable<ToolExecutionRequest> iterable) {
        int totalTokens = 0;
        Iterator<ToolExecutionRequest> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            ToolExecutionRequest request = iterator.next();
            // 假设工具执行请求的名称和参数需要计算 token 数量
            totalTokens += countTokens(request.name());
            totalTokens += countTokens(request.arguments());
        }
        return totalTokens;
    }
}