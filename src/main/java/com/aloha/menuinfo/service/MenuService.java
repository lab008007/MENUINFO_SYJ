package com.aloha.menuinfo.service;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import com.aloha.menuinfo.client.NeisClient;
import com.aloha.menuinfo.client.dto.MealServiceDietInfoDto;
import com.aloha.menuinfo.model.MealInfo;
import com.aloha.menuinfo.exception.ApiException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {
    private final NeisClient neisClient;
    
    @Retryable(
        value = {HttpServerErrorException.class, RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2),
        recover = "handleRetryFailure"
    )
    @Cacheable(value = "meals", key = "#schoolCode + '-' + #officeCode + '-' + #date", 
               unless = "#result == null || #result.isEmpty()")
    public List<MealInfo> getMealsByDate(String schoolCode, String officeCode, String date) {
        String requestId = UUID.randomUUID().toString();
        try {
            validateParameters(schoolCode, officeCode, date);
            
            log.debug("[요청ID: {}] 급식 정보 조회 시작 - 학교코드: {}, 교육청코드: {}, 날짜: {}", 
                     requestId, schoolCode, officeCode, date);
            
            MealServiceDietInfoDto mealDto = neisClient.getMealInfo(officeCode, schoolCode, date);
            
            if (mealDto == null) {
                log.debug("[요청ID: {}] 급식 정보가 없습니다", requestId);
                return Collections.emptyList();
            }
            
            MealInfo mealInfo = convertToMealInfo(mealDto);
            List<MealInfo> meals = new ArrayList<>();
            meals.add(mealInfo);
            
            log.debug("[요청ID: {}] 급식 정보 조회 완료: {}", requestId, mealInfo);
            return meals;
            
        } catch (HttpServerErrorException.InternalServerError e) {
            handleInternalServerError(e, requestId);
            throw new ApiException(
                String.format("NEIS 서버가 일시적으로 불안정합니다 (요청ID: %s). 잠시 후 다시 시도해주세요.", requestId),
                "SERVER_ERROR"
            );
        } catch (HttpServerErrorException e) {
            log.error("[요청ID: {}] NEIS API 서버 오류: {} - {}", 
                     requestId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ApiException(
                String.format("NEIS 서버 연결에 문제가 있습니다 (요청ID: %s). 잠시 후 다시 시도해주세요.", requestId),
                "API_ERROR"
            );
        } catch (RestClientException e) {
            log.error("[요청ID: {}] NEIS API 호출 실패: {}", requestId, e.getMessage());
            throw new ApiException(
                String.format("서버 연결에 실패했습니다 (요청ID: %s). 잠시 후 다시 시도해주세요.", requestId),
                "CONNECTION_ERROR"
            );
        } catch (Exception e) {
            log.error("[요청ID: {}] 예상치 못한 오류 발생: {}", requestId, e.getMessage(), e);
            throw new ApiException(
                String.format("서비스 처리 중 오류가 발생했습니다 (요청ID: %s).", requestId),
                "UNKNOWN_ERROR"
            );
        }
    }
    
    @Recover
    public List<MealInfo> handleRetryFailure(Exception e, String schoolCode, String officeCode, String date) {
        String requestId = UUID.randomUUID().toString();
        log.error("[요청ID: {}] 재시도 실패 - 학교코드: {}, 교육청코드: {}, 날짜: {}", 
                 requestId, schoolCode, officeCode, date, e);
        throw new ApiException(
            String.format("서버 연결 재시도 실패 (요청ID: %s). 잠시 후 다시 시도해주세요.", requestId),
            "RETRY_FAILED"
        );
    }
    
    private void validateParameters(String schoolCode, String officeCode, String date) {
        if (schoolCode == null || schoolCode.trim().isEmpty() ||
            officeCode == null || officeCode.trim().isEmpty() ||
            date == null || date.trim().isEmpty()) {
            throw new ApiException("필수 파라미터가 누락되었습니다", "INVALID_PARAMETER");
        }
    }
    
    private void handleInternalServerError(HttpServerErrorException.InternalServerError e, String requestId) {
        String errorBody = e.getResponseBodyAsString();
        log.error("[요청ID: {}] NEIS 서버 내부 오류: {}", requestId, errorBody);
        
        if (errorBody.contains("Worker") && errorBody.contains("error occurred")) {
            throw new ApiException(
                String.format("NEIS 서버가 일시적으로 불안정합니다 (요청ID: %s). 잠시 후 다시 시도해주세요.", requestId),
                "SERVER_ERROR"
            );
        }
    }
    
    private MealInfo convertToMealInfo(MealServiceDietInfoDto dto) {
        MealInfo mealInfo = new MealInfo();
        mealInfo.setMealCode(dto.getMMEAL_SC_CODE());
        mealInfo.setMealName(dto.getMMEAL_SC_NM());
        mealInfo.setMenuName(dto.getDDISH_NM());
        mealInfo.setOriginInfo(dto.getORPLC_INFO());
        mealInfo.setCalInfo(dto.getCAL_INFO());
        mealInfo.setNutritionInfo(dto.getNTR_INFO());
        mealInfo.setSchoolName(dto.getSCHUL_NM());
        mealInfo.setMealDate(dto.getMLSV_YMD());
        
        mealInfo.parseAllergyInfo(dto.getDDISH_NM());
        
        return mealInfo;
    }
} 