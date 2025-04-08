package com.demo.controller;

import com.demo.config.AiConfig;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    QwenChatModel qwenChatModel;

    @Autowired
    QwenStreamingChatModel qwenStreamingChatModel;

    // 记忆对话
    @Autowired
    AiConfig.Assistant assistant;

    // 区分用户的记忆对话
    @Autowired
    AiConfig.AssistantUnique assistantUnique;

    /**
     * 普通对话
     * @param message 输入
     * @return
     */
    @RequestMapping("/chat")
    public String test(@RequestParam(defaultValue="你是谁") String message) {
        String chat = qwenChatModel.chat(message);
        return chat;
    }


    /**
     * 流式输出
     * @param message 输入
     * @return
     */
    @RequestMapping(value = "stream", produces = "text/stream;charset=UTF-8")
    public Flux<String> streamChat(@RequestParam(defaultValue="你是谁") String message) {
        // chat 方法：该方法主要用于模拟对话场景。在聊天应用中，它能结合历史对话内容，理解对话上下文，进而生成符合语境的回复，保证对话的连贯性与逻辑性。比如智能客服、聊天机器人等场景就会用到它。
        // generate 方法：主要用于生成文本内容。它更侧重于按照给定的提示信息，生成一篇相对独立的文本，像文章、故事、摘要等，而不太依赖对话的历史上下文。
        Flux<String> flux = Flux.create(fluxSink ->{
            qwenStreamingChatModel.chat(message, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String s) {
                    fluxSink.next(s);
                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    fluxSink.complete();
                }

                @Override
                public void onError(Throwable throwable) {
                    fluxSink.error(throwable);
                }
            });
        });

        return flux;
    }


    /**
     * 多轮对话
     * @param message 提问1
     * @param message2 提问2
     * @return
     */
    @RequestMapping("manyChat")
    public String manyChat(@RequestParam(defaultValue="你好，我是平果果") String message,
                           @RequestParam(defaultValue="我叫什么") String message2) {
        UserMessage userMessage1 = UserMessage.userMessage(message);
        ChatRequest chatRequest1 = ChatRequest
                .builder()
                .messages(userMessage1)
                .build();
        ChatResponse response1 = qwenChatModel.chat(chatRequest1);
        AiMessage aiMessage1 = response1.aiMessage(); // 大模型的第一次响应
        System.out.println(aiMessage1.text());
        System.out.println("----------------");

        ChatRequest chatRequest2 = ChatRequest
                .builder()
                .messages(userMessage1, aiMessage1, UserMessage.userMessage(message2))
                .build();
        ChatResponse response2 = qwenChatModel.chat(chatRequest2);
        AiMessage aiMessage2 = response2.aiMessage(); // 大模型的第二次响应
        System.out.println(aiMessage2.text());

        return message + "\n" +
                aiMessage1.text() + "\n" +
                message2 + "\n" +
                aiMessage2.text();
    }

    /**
     * ChatMemory记忆对话
     * @param message 用户输入
     * @return
     */
    @RequestMapping("/memoryChat")
    public String memoryChat(@RequestParam(defaultValue="你好，我是平果果") String message) {
        return assistant.chat(message);
    }

    @RequestMapping(value = "memoryStream", produces = "text/stream;charset=UTF-8")
    public Flux<String> memoryStreamChat(@RequestParam(defaultValue="我是谁") String message) {
        // 都存在同一个memory中，调用了memoryChat再调用该方法，可以识别上一个问题的内容
        TokenStream stream = assistant.stream(message);

        return Flux.create(sink -> {
            stream.onPartialResponse(sink::next)
                    .onCompleteResponse(c -> sink.complete())
                    .onError(sink::error);
                });
    }


    @RequestMapping("/memoryIdChat")
    public String memoryIdChat(@RequestParam(defaultValue="你好，我是平果果") String message,
                               @RequestParam(defaultValue="123456") int userId) {
        return assistantUnique.chat(userId, message);
    }

}
