package com.example.product_service.controller;

import com.example.product_service.config.AppConfig;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/products/config")
public class ConfigTestController {

    @Autowired
    private AppConfig appConfig;

    @Value("${server.port}")
    private String port;

    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping("/info")
    public Map<String, String> getConfigInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("service", serviceName);
        info.put("port", port);
        info.put("message", appConfig.getMessage());
        info.put("note", "This config comes from Config Server!");
        return info;
    }
}
