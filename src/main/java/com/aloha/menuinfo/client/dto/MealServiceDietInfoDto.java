package com.aloha.menuinfo.client.dto;

import lombok.Data;

@Data
public class MealServiceDietInfoDto {
    private String ATPT_OFCDC_SC_CODE;    // 시도교육청코드
    private String ATPT_OFCDC_SC_NM;      // 시도교육청명
    private String SD_SCHUL_CODE;        // 행정표준코드
    private String SCHUL_NM;            // 학교명
    private String MMEAL_SC_CODE;        // 식사코드
    private String MMEAL_SC_NM;          // 식사명
    private String MLSV_YMD;            // 급식일자
    private Integer MLSV_FGR;           // 급식인원수
    private String DDISH_NM;            // 요리명
    private String ORPLC_INFO;          // 원산지정보
    private String CAL_INFO;            // 칼로리정보
    private String NTR_INFO;            // 영양정보
    private String MLSV_FROM_YMD;        // 급식시작일자
    private String MLSV_TO_YMD;          // 급식종료일자
    private String LOAD_DTM;            // 수정일자
} 