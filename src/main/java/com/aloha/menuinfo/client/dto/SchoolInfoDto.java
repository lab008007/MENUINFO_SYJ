package com.aloha.menuinfo.client.dto;

import lombok.Data;

@Data
public class SchoolInfoDto {
    private String SD_SCHUL_CODE;    // 행정표준코드
    private String SCHUL_NM;         // 학교명
    private String ATPT_OFCDC_SC_CODE; // 시도교육청코드
    private String ATPT_OFCDC_SC_NM;   // 시도교육청명
    private String SCHUL_KND_SC_NM;  // 학교종류명
    private String ORG_RDNMA;        // 주소
    private String SCHUL_TEL_NO;     // 학교 전화번호
}
