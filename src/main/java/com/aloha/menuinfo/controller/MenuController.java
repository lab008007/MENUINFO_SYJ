package com.aloha.menuinfo.controller;

import com.aloha.menuinfo.service.MenuService;
import com.aloha.menuinfo.service.SchoolSearchService;
import com.aloha.menuinfo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aloha.menuinfo.domain.PostCategory;
import com.aloha.menuinfo.exception.ApiException;
import com.aloha.menuinfo.model.MealInfo;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final SchoolSearchService schoolSearchService;
    private final PostService postService;
    
    @GetMapping("/")
    public String home(Model model) {
        int previewLimit = 5;
        model.addAttribute("praisePosts", postService.getRecentPostsByCategory(PostCategory.PRAISE, previewLimit));
        model.addAttribute("promotionPosts", postService.getRecentPostsByCategory(PostCategory.PROMOTION, previewLimit));
        model.addAttribute("ideaPosts", postService.getRecentPostsByCategory(PostCategory.IDEA, previewLimit));
        model.addAttribute("pridePosts", postService.getRecentPostsByCategory(PostCategory.PRIDE, previewLimit));
        return "index";
    }
    
    @PostMapping("/search")
    public String searchSchool(@RequestParam String schoolName, Model model) {
        try {
            var schoolInfo = schoolSearchService.findSchool(schoolName);
            model.addAttribute("school", schoolInfo);
            return "index";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }
    
    private boolean isValidDate(LocalDate date) {
        LocalDate now = LocalDate.now();
        LocalDate oneYearAgo = now.minusYears(1);
        LocalDate oneMonthLater = now.plusMonths(1);
        return !date.isBefore(oneYearAgo) && !date.isAfter(oneMonthLater);
    }
    
    @GetMapping("/menu")
    public String getMenu(
        @RequestParam String schoolCode,
        @RequestParam String officeCode,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String date,
        Model model) {
        try {
            if (schoolCode == null || schoolCode.trim().isEmpty() ||
                officeCode == null || officeCode.trim().isEmpty()) {
                throw new ApiException("학교 정보가 올바르지 않습니다", "INVALID_SCHOOL_INFO");
            }

            LocalDate parsedDate = LocalDate.parse(date);
            if (!isValidDate(parsedDate)) {
                throw new ApiException("조회 가능한 날짜가 아닙니다 (과거 1년부터 미래 1개월까지 조회 가능)", "INVALID_DATE_RANGE");
            }
            
            String formattedDate = parsedDate.format(DateTimeFormatter.BASIC_ISO_DATE);
            List<MealInfo> meal = menuService.getMealsByDate(schoolCode, officeCode, formattedDate);
            
            if (meal.isEmpty()) {
                throw new ApiException("해당 날짜의 급식 정보가 없습니다", "NO_MEAL_INFO");
            }
            
            var schoolInfo = schoolSearchService.findSchoolByCode(schoolCode, officeCode);
            model.addAttribute("school", schoolInfo);
            model.addAttribute("meal", meal);
            return "index";
        } catch (Exception e) {
            log.error("메뉴 조회 실패: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            return "index";
        }
    }
}