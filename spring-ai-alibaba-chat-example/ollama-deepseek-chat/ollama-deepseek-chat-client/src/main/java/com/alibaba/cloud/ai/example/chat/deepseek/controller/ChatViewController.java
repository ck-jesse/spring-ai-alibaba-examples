package com.alibaba.cloud.ai.example.chat.deepseek.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatViewController {

    @GetMapping("/")
    public String index() {
        return "chat";
    }
    
    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }
}