package com.aloha.menuinfo.domain;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Getter 
@Setter
public class User {
    private Long userId;
    private String email;
    private String password;
    private String userName;
    private String role;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 