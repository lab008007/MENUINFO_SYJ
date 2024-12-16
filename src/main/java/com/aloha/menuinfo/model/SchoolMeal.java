package com.aloha.menuinfo.model;

import lombok.Data;

@Data
public class SchoolMeal {
    private String schoolName;      // 학교명
    private String mealDate;        // 급식일자
    private String menuItems;       // 메뉴 내용
    private String calInfo;         // 칼로리 정보
    private String nutritionInfo;   // 영양 정보
}
