package com.aloha.menuinfo.model;

import lombok.Data;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class MealInfo {
    private String mealCode;      // MMEAL_SC_CODE
    private String mealName;      // MMEAL_SC_NM
    private String menuName;      // DDISH_NM
    private String originInfo;    // ORPLC_INFO
    private String calInfo;       // CAL_INFO
    private String nutritionInfo; // NTR_INFO
    private List<String> allergyInfo;  // 알레르기 정보
    private String schoolName;    // 학교명
    private String mealDate;      // 급식일자
    
    private static final Logger log = LoggerFactory.getLogger(MealInfo.class);
    
    public void setMenuName(String menuName) {
        this.menuName = menuName.replace("<br/>", "\n");
    }
    
    // 알레르기 정보 파싱 (예: "(1.2.3)" -> ["1", "2", "3"])
    public void parseAllergyInfo(String menuName) {
        try {
            if (menuName == null) {
                this.allergyInfo = Collections.emptyList();
                return;
            }
            
            Pattern pattern = Pattern.compile("\\((\\d+(\\.\\d+)*)\\)");
            Matcher matcher = pattern.matcher(menuName);
            
            if (matcher.find()) {
                String allergyNumbers = matcher.group(1);
                this.allergyInfo = Arrays.stream(allergyNumbers.split("\\."))
                    .filter(s -> !s.isEmpty())
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
            } else {
                this.allergyInfo = Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("알레르기 정보 파싱 오류: {}", menuName, e);
            this.allergyInfo = Collections.emptyList();
        }
    }
    
    // 날짜 포맷 변환 메서드 추가
    public void setMealDate(String mealDate) {
        try {
            if (mealDate == null || mealDate.trim().isEmpty()) {
                this.mealDate = null;
                return;
            }
            
            if (mealDate.length() == 8) {
                LocalDate parsedDate = LocalDate.parse(mealDate, 
                    DateTimeFormatter.ofPattern("yyyyMMdd"));
                this.mealDate = parsedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (mealDate.length() == 10 && mealDate.contains("-")) {
                // 이미 yyyy-MM-dd 형식인 경우
                this.mealDate = mealDate;
            } else {
                log.warn("잘못된 날짜 형식: {}", mealDate);
                this.mealDate = mealDate;
            }
        } catch (DateTimeParseException e) {
            log.error("날짜 파싱 오류: {}", mealDate, e);
            this.mealDate = mealDate;
        }
    }
}