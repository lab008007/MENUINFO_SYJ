package com.aloha.menuinfo.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    private Long postId;
    private Long userId;
    private String category;
    private String content;
    private String imagePath;
    private int likes;
    private LocalDateTime createdAt;
    private String userName;
} 