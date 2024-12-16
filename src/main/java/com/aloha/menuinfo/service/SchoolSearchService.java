package com.aloha.menuinfo.service;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import com.aloha.menuinfo.client.NeisClient;
import com.aloha.menuinfo.client.dto.SchoolInfoDto;
import com.aloha.menuinfo.exception.ApiException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolSearchService {
    private final NeisClient neisClient;
    
    @Retryable(
        value = {HttpServerErrorException.class, RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Cacheable(value = "schools", key = "#schoolName")
    public SchoolInfoDto findSchool(String schoolName) {
        if (schoolName == null || schoolName.trim().isEmpty()) {
            throw new ApiException("학교명을 입력해주세요", "INVALID_SCHOOL_NAME");
        }
        
        log.debug("학교 검색 시작: {}", schoolName);
        try {
            return neisClient.getSchoolInfo(schoolName);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("NEIS 서버 내부 오류 (재시도 {}/3): {}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException("NEIS 서버가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.", "SERVER_ERROR");
        } catch (HttpServerErrorException e) {
            log.error("NEIS API 서버 오류 (재시도 {}/3): {} - {}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException("NEIS 서버 연결에 문제가 있습니다. 잠시 후 다시 시도해주세요.", "API_ERROR");
        } catch (RestClientException e) {
            log.error("NEIS API 호출 실패 (재시도 {}/3): {}", e.getMessage());
            throw new ApiException("서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.", "CONNECTION_ERROR");
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage());
            throw new ApiException("서비스 처리 중 오류가 발생했습니다.", "UNKNOWN_ERROR");
        }
    }

    @Retryable(
        value = {HttpServerErrorException.class, RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Cacheable(value = "schools", key = "#schoolCode + '-' + #officeCode")
    public SchoolInfoDto findSchoolByCode(String schoolCode, String officeCode) {
        if (schoolCode == null || schoolCode.trim().isEmpty() || 
            officeCode == null || officeCode.trim().isEmpty()) {
            throw new ApiException("학교 코드 정보가 올바르지 않습니다", "INVALID_SCHOOL_CODE");
        }
        
        log.debug("학교 코드로 검색 시작 - 학교코드: {}, 교육청코드: {}", schoolCode, officeCode);
        try {
            return neisClient.getSchoolInfoByCode(officeCode, schoolCode);
        } catch (HttpServerErrorException.InternalServerError e) {
            log.error("NEIS 서버 내부 오류 (재시도 {}/3): {}", e.getStatusCode());
            throw new ApiException("NEIS 서버가 일시적으로 불안정합니다. 잠시 후 다시 시도해주세요.", "SERVER_ERROR");
        } catch (HttpServerErrorException e) {
            log.error("NEIS API 서버 오류 (재시도 {}/3): {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException("NEIS 서버 연결에 문제가 있습니다. 잠시 후 다시 시도해주세요.", "API_ERROR");
        } catch (RestClientException e) {
            log.error("NEIS API 호출 실패 (재시도 {}/3): {}", e.getMessage());
            throw new ApiException("서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.", "CONNECTION_ERROR");
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage());
            throw new ApiException("서비스 처리 중 오류가 발생했습니다.", "UNKNOWN_ERROR");
        }
    }
}
