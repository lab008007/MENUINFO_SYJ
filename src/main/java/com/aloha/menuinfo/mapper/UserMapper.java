package com.aloha.menuinfo.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.aloha.menuinfo.domain.User;

@Mapper
public interface UserMapper {
    void save(User user);
    User findByEmail(String email);
    User findById(Long id);
} 