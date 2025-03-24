package com.alibaba.cloud.ai.application.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查
 *
 * @author chenck
 * @date 2024/5/6 13:53
 */
@RestController
@Slf4j
public class HealthController {

    @JsonIgnore
    @GetMapping({"/health", "/"})
    public String health() {
        String result = "hello, the server of ddd is ready to go.";
        return result;
    }
}
