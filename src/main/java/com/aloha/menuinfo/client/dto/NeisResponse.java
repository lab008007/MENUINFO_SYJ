package com.aloha.menuinfo.client.dto;

import lombok.Data;
import java.util.List;
import com.aloha.menuinfo.exception.ApiException;

@Data
public class NeisResponse<T> {
    private List<ResponseWrapper<T>> mealServiceDietInfo;
    private List<ResponseWrapper<T>> schoolInfo;

    @Data
    public static class ResponseWrapper<T> {
        private List<Head> head;
        private List<T> row;
    }

    @Data
    public static class Head {
        private Integer list_total_count;
        private Result RESULT;
    }

    @Data
    public static class Result {
        private String CODE;
        private String MESSAGE;
    }

    public T getFirstRow() {
        try {
            if (mealServiceDietInfo != null && !mealServiceDietInfo.isEmpty()) {
                ResponseWrapper<T> wrapper = mealServiceDietInfo.get(0);
                if (wrapper != null && wrapper.getRow() != null && !wrapper.getRow().isEmpty()) {
                    return wrapper.getRow().get(0);
                }
            }
            if (schoolInfo != null && !schoolInfo.isEmpty()) {
                ResponseWrapper<T> wrapper = schoolInfo.get(0);
                if (wrapper != null && wrapper.getRow() != null && !wrapper.getRow().isEmpty()) {
                    return wrapper.getRow().get(0);
                }
            }
            return null;
        } catch (Exception e) {
            throw new ApiException("응답 데이터 처리 중 오류가 발생했습니다", "RESPONSE_PARSING_ERROR", e);
        }
    }
}