package com.aloha.menuinfo.client.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PostDto {
    @NotBlank(message = "카테고리는 필수 입력값입니다")
    private String category;
    
    @NotBlank(message = "내용은 필수 입력값입니다")
    private String content;
    
    private String imagePath;
}