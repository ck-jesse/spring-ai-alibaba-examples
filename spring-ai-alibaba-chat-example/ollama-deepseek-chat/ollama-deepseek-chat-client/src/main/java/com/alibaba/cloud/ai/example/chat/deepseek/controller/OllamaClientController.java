/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.example.chat.deepseek.controller;

import com.alibaba.cloud.ai.example.chat.deepseek.advisors.CustomLoggerAdvisor;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@RestController
@RequestMapping("/ollama/chat-client")
public class OllamaClientController {
    private static final Logger logger = LoggerFactory.getLogger(OllamaClientController.class);
    private static final String DEFAULT_PROMPT = "你好，介绍下你自己！请用中文回答。";

    private final ChatClient ollamaiChatClient;


    public OllamaClientController(ChatModel chatModel) {
        // 构造时，可以设置 ChatClient 的参数
        // {@link org.springframework.ai.chat.client.ChatClient};
        this.ollamaiChatClient = ChatClient.builder(chatModel)
                // 实现 Chat Memory 的 Advisor
                // 在使用 Chat Memory 时，需要指定对话 ID，以便 Spring AI 处理上下文。
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new CustomLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(OllamaOptions.builder().withTopP(0.7).build()).build();
    }

    /**
     * ChatClient 简单调用
     */
    @GetMapping("/simple/chat")
    public String simpleChat() {
        ChatClient.CallResponseSpec responseSpec = ollamaiChatClient.prompt(DEFAULT_PROMPT).call();
        String content = responseSpec.content();
        logger.info("AI Response: {}", content);
        return content;
    }


    /**
     * ChatClient 流式调用
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        Flux<String> responseFlux = ollamaiChatClient.prompt(DEFAULT_PROMPT).stream().content();
        responseFlux.subscribe(content -> logger.info("AI Stream Response: {}", content));
        return responseFlux;
    }

    @GetMapping("/simple/chat1")
    public String simpleChat(String content) {
        if (Strings.isEmpty(content)) {
            content = DEFAULT_PROMPT;
        }
        ChatClient.CallResponseSpec responseSpec = ollamaiChatClient.prompt(content).call();
        String responseContent = responseSpec.content();
        logger.info("AI Response: {}", responseContent);
        return responseContent;
    }

    @GetMapping("/stream/chat1")
    public Flux<String> streamChat(String content, HttpServletResponse response) {
        if (Strings.isEmpty(content)) {
            content = DEFAULT_PROMPT;
        }
        response.setCharacterEncoding("UTF-8");
        ChatClient.StreamResponseSpec responseSpec = ollamaiChatClient.prompt(content).stream();

        // 在Reactor中，一个Flux对象只能被订阅一次。如果你尝试对同一个Flux对象多次调用subscribe方法，会导致错误。
        // 为了解决这个问题，你可以使用share()或cache()方法来允许多次订阅。

        // cache()方法会缓存Flux中的数据，允许多个订阅者订阅同一个数据流。
        // 但是，cache()会将所有数据缓存到内存中，因此在处理大量数据时可能会导致内存问题。
        // Flux<String> responseFlux = responseSpec.content().cache();
        // 推荐使用share()方法。share()允许多个订阅者订阅同一个数据流，而不会缓存所有数据。
        Flux<String> responseFlux = responseSpec.content().share();

        // 记录流式结果到日志
        // responseFlux.subscribe(s -> logger.info("AI Stream Response: {}", s));

        // 分批处理流式结果（通过自定义CustomLoggerAdvisor来记录日志或写入数据库）
        /*responseFlux.buffer(20) // 每10条数据分一批
                .subscribe(batch -> {
                    StringBuilder context = new StringBuilder();
                    // 记录每一批数据到日志
                    // 你可以在这里保存 batch 到数据库、文件或其他存储中
                    batch.forEach(s -> context.append(s));
                    logger.info("Batch of Responses: {}", context);
                });*/

        // collectList()方法会将所有的流式数据收集到一个List中，这可能会占用较多的内存，特别是在处理大量数据时。
        // 获取流式结果
        /*
        responseFlux.collectList().subscribe(list -> {
            for (String s : list) {
                think.append(s);
            }
            System.out.println(think.toString());
        });*/

        // 返回流式结果
        return responseFlux;
    }

}
