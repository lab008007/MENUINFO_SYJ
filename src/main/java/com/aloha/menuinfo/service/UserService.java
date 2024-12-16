package com.aloha.menuinfo.service;

import com.aloha.menuinfo.mapper.UserMapper;
import com.aloha.menuinfo.domain.User;
import com.aloha.menuinfo.client.dto.UserDto;
import com.aloha.menuinfo.security.PrincipalDetails;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;

@Service
public class UserService implements UserDetailsService {
    
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    
    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    public void join(UserDto userDto) {
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setUserName(userDto.getUserName());
        user.setRole("USER");
        userMapper.save(user);
    }
    
    public void login(String email, String password) {
        User user = userMapper.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            new PrincipalDetails(user),
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }
        return new PrincipalDetails(user);
    }
} 