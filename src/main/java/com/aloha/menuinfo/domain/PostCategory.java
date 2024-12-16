package com.aloha.menuinfo.domain;

public enum PostCategory {
    PRAISE("급식 칭찬"),
    PROMOTION("급식 홍보"),
    IDEA("급식 아이디어"),
    PRIDE("학교 자랑");
    
    private final String displayName;
    
    PostCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}