package com.alibaba.cloud.ai.example.chat.deepseek.advisors;

import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * 自定义 ai Advisor，打印请求和响应日志
 */
public class CustomLoggerAdvisor extends SimpleLoggerAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(CustomLoggerAdvisor.class);


    private final Function<AdvisedRequest, String> requestToString;

    private final Function<ChatResponse, String> responseToString;

    private int order;

    public CustomLoggerAdvisor() {
        this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, 0);
    }

    public CustomLoggerAdvisor(int order) {
        this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, order);
    }

    public CustomLoggerAdvisor(Function<AdvisedRequest, String> requestToString,
                               Function<ChatResponse, String> responseToString, int order) {
        this.requestToString = requestToString;
        this.responseToString = responseToString;
        this.order = order;
    }

    private AdvisedRequest before(AdvisedRequest request) {
        if (StringUtils.isNotBlank(request.systemText())) {
            logger.info("request: {}", request.systemText());
        }
        if (StringUtils.isNotBlank(request.userText())) {
            logger.info("request: {}", request.userText());
        }
//        logger.info("request: {}", this.requestToString.apply(request));
        return request;
    }

    private void observeAfter(AdvisedResponse advisedResponse) {
        logger.info("response: {}", advisedResponse.response().getResult().getOutput().getText());
//        logger.info("response: {}", this.responseToString.apply(advisedResponse.response()));
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {

        advisedRequest = before(advisedRequest);

        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);

        observeAfter(advisedResponse);

        return advisedResponse;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {

        advisedRequest = before(advisedRequest);

        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);

        return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }
}
