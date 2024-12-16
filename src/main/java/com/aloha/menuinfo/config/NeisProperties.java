package com.aloha.menuinfo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "neis.api")
public class NeisProperties {
    private String baseUrl;
    private String schoolKey;
    private String mealKey;
    private int connectTimeout;
    private int readTimeout;
} 