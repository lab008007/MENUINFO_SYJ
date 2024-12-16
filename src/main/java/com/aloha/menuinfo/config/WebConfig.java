package com.aloha.menuinfo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import java.io.IOException;
import com.aloha.menuinfo.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class WebConfig {
    
    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);
    
    @Value("${neis.api.connect-timeout:5000}")
    private int connectTimeout;
    
    @Value("${neis.api.read-timeout:5000}")
    private int readTimeout;
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
        return restTemplate;
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

    @Component
    public class CustomResponseErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
            return response.getStatusCode().isError();
        }

        @Override
        public void handleError(@NonNull ClientHttpResponse response) throws IOException {
            byte[] body = response.getBody().readAllBytes();
            String errorBody = new String(body, StandardCharsets.UTF_8);
            String requestId = UUID.randomUUID().toString();
            
            log.error("NEIS API 오류 발생 [요청ID: {}]\n상태 코드: {}\n응답 본문: {}", 
                requestId, response.getStatusCode(), errorBody);
            
            if (errorBody.contains("Worker") && errorBody.contains("error occurred")) {
                throw new ApiException(
                    String.format("NEIS 서버가 일시적으로 불안정합니다 (요청ID: %s). 잠시 후 다시 시도해주세요.", requestId),
                    "SERVER_ERROR"
                );
            }
            
            if (errorBody.contains("해당하는 데이터가 없습니다")) {
                throw new ApiException("요청한 정보가 없습니다", "NO_DATA");
            }
            
            if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                throw new ApiException(
                    String.format("일시적인 서버 오류가 발생했습니다 (요청ID: %s). 잠시 후 다시 시도해주세요.", requestId),
                    "SERVER_ERROR"
                );
            }
            
            throw new ApiException(
                String.format("API 서버 오류가 발생했습니다 (상태 코드: %s)", response.getStatusCode()),
                "API_ERROR"
            );
        }
    }
}