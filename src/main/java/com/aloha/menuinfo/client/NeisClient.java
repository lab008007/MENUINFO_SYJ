package com.aloha.menuinfo.client;

import com.aloha.menuinfo.client.dto.NeisResponse;
import com.aloha.menuinfo.client.dto.MealServiceDietInfoDto;
import com.aloha.menuinfo.client.dto.SchoolInfoDto;
import com.aloha.menuinfo.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class NeisClient {
    private final RestTemplate restTemplate;
    
    @Value("${neis.api.school-key}")
    private String schoolApiKey;
    
    @Value("${neis.api.meal-key}")
    private String mealApiKey;
    
    @Value("${neis.api.base-url}")
    private String baseUrl;
    
    public MealServiceDietInfoDto getMealInfo(String officeCode, String schoolCode, String date) {
        try {
            String url = buildMealServiceUrl(officeCode, schoolCode, date);
            log.debug("Requesting meal info from URL: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<NeisResponse<MealServiceDietInfoDto>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<NeisResponse<MealServiceDietInfoDto>>() {}
            );
            
            log.debug("API Response Status: {}", responseEntity.getStatusCode());
            log.debug("API Response Body: {}", responseEntity.getBody());
            
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                String errorMessage = String.format("NEIS API 서버 오류 (상태 코드: %s)", responseEntity.getStatusCode());
                log.error(errorMessage);
                throw new ApiException(errorMessage, "API_ERROR");
            }
            
            NeisResponse<MealServiceDietInfoDto> response = responseEntity.getBody();
            if (response == null) {
                throw new ApiException("NEIS API 응답이 없습니다", "NO_RESPONSE");
            }
            
            log.debug("Response mealServiceDietInfo: {}", response.getMealServiceDietInfo());
            
            if (response.getMealServiceDietInfo() == null || response.getMealServiceDietInfo().isEmpty()) {
                throw new ApiException("급식 정보가 없습니다", "NO_MEAL_INFO");
            }
            
            MealServiceDietInfoDto firstRow = response.getFirstRow();
            log.debug("First row data: {}", firstRow);
            
            return firstRow;
            
        } catch (HttpServerErrorException.InternalServerError e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("NEIS API 서버 내부 오류: {}", errorBody);
            if (errorBody.contains("해당하는 데이터가 없습니다")) {
                throw new ApiException("급식 정보가 없습니다", "NO_MEAL_INFO");
            }
            throw new ApiException("NEIS 서버가 일시적으로 응��하지 않습니다. 잠시 후 다시 시도해주세요.", "SERVER_ERROR", e);
        } catch (HttpServerErrorException e) {
            String errorMessage = String.format("NEIS API 서버 오류 (상태 코드: %s, 응답: %s)", 
                e.getStatusCode(), e.getResponseBodyAsString());
            log.error(errorMessage);
            throw new ApiException("NEIS 서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", "SERVER_ERROR", e);
        } catch (RestClientException e) {
            log.error("NEIS API 호출 실패: {}", e.getMessage());
            throw new ApiException("NEIS API 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.", "CONNECTION_ERROR", e);
        }
    }
    
    private String buildMealServiceUrl(String officeCode, String schoolCode, String date) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("hub/mealServiceDietInfo")
            .queryParam("KEY", mealApiKey)
            .queryParam("Type", "json")
            .queryParam("pIndex", 1)
            .queryParam("pSize", 100)
            .queryParam("ATPT_OFCDC_SC_CODE", officeCode)
            .queryParam("SD_SCHUL_CODE", schoolCode)
            .queryParam("MLSV_YMD", date)
            .build(true)
            .toUriString();
    }
    
    public SchoolInfoDto getSchoolInfo(String schoolName) {
        try {
            String url = buildSchoolInfoUrl(schoolName);
            log.debug("API 청 URL: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            
            ResponseEntity<NeisResponse<SchoolInfoDto>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<NeisResponse<SchoolInfoDto>>() {}
            );
            
            log.debug("API 응답 상태 코드: {}", responseEntity.getStatusCode());
            log.debug("API 응답 본문: {}", responseEntity.getBody());
            
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                String errorMessage = String.format("NEIS API 서버 오류 (상태 코드: %s)", responseEntity.getStatusCode());
                log.error(errorMessage);
                throw new ApiException(errorMessage, "API_ERROR");
            }
            
            NeisResponse<SchoolInfoDto> response = responseEntity.getBody();
            if (response == null) {
                throw new ApiException("NEIS API 응답이 없습니다", "NO_RESPONSE");
            }
            validateSchoolResponse(response);
            return response.getFirstRow();
            
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("NEIS API 서버 내부 오류: {}", e.getResponseBodyAsString());
            throw new ApiException("NEIS 서버가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해주세요.", "SERVER_ERROR", e);
        } catch (HttpServerErrorException e) {
            String errorMessage = String.format("NEIS API 서버 오류 (상태 코드: %s, 응답: %s)", 
                e.getStatusCode(), e.getResponseBodyAsString());
            log.error(errorMessage);
            throw new ApiException("NEIS 서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", "SERVER_ERROR", e);
        } catch (RestClientException e) {
            log.error("NEIS API 호출 실패: {}", e.getMessage());
            throw new ApiException("NEIS API 서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.", "CONNECTION_ERROR", e);
        }
    }
    
    private String buildSchoolInfoUrl(String schoolName) {
        log.debug("학교명 검색 URL 생성: {}", schoolName);
        try {
            String encodedSchoolName = URLEncoder.encode(schoolName, StandardCharsets.UTF_8.toString());
            return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("hub/schoolInfo")
                .queryParam("KEY", schoolApiKey)
                .queryParam("Type", "json")
                .queryParam("pIndex", 1)
                .queryParam("pSize", 100)
                .queryParam("SCHUL_NM", encodedSchoolName)
                .build(false)
                .toUriString();
        } catch (UnsupportedEncodingException e) {
            throw new ApiException("학교명 인코딩 실패", "ENCODING_ERROR", e);
        }
    }
    
    private void validateSchoolResponse(NeisResponse<SchoolInfoDto> response) {
        if (response == null) {
            throw new ApiException("NEIS API 응답이 없습니다", "NO_RESPONSE");
        }
        if (response.getSchoolInfo() == null || response.getSchoolInfo().isEmpty() || 
            response.getSchoolInfo().get(0).getRow() == null || 
            response.getSchoolInfo().get(0).getRow().isEmpty()) {
            throw new ApiException("학교 정보가 없습니다", "NO_SCHOOL_INFO");
        }
    }
    
    public SchoolInfoDto getSchoolInfoByCode(String officeCode, String schoolCode) {
        String url = buildSchoolInfoByCodeUrl(officeCode, schoolCode);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        NeisResponse<SchoolInfoDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, 
            new ParameterizedTypeReference<NeisResponse<SchoolInfoDto>>() {})
            .getBody();
        if (response == null) {
            throw new ApiException("NEIS API 응답이 없습니다", "NO_RESPONSE");
        }
        return response.getFirstRow();
    }
    
    private String buildSchoolInfoByCodeUrl(String officeCode, String schoolCode) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("hub/schoolInfo")
            .queryParam("KEY", schoolApiKey)
            .queryParam("Type", "json")
            .queryParam("ATPT_OFCDC_SC_CODE", officeCode)
            .queryParam("SD_SCHUL_CODE", schoolCode)
            .build(true)
            .toUriString();
    }
}
