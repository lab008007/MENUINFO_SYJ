package com.aloha.menuinfo.service;

import com.aloha.menuinfo.domain.Post;
import com.aloha.menuinfo.domain.PostCategory;
import com.aloha.menuinfo.domain.User;
import com.aloha.menuinfo.mapper.PostMapper;
import com.aloha.menuinfo.client.dto.PostDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    
    private final PostMapper postMapper;
    
    public List<Post> getRecentPostsByCategory(PostCategory category, int limit) {
        return postMapper.findRecentByCategory(category.name(), limit);
    }
    
    public void writePost(PostDto postDto, User user) {
        Post post = new Post();
        post.setUserId(user.getUserId());
        post.setCategory(postDto.getCategory());
        post.setContent(postDto.getContent());
        post.setImagePath(postDto.getImagePath());
        postMapper.insert(post);
    }
    
    public List<Post> getPostsByCategory(PostCategory category, int page, int size) {
        int offset = (page - 1) * size;
        return postMapper.findByCategory(category.name(), size, offset);
    }
    
    public long getTotalPostsByCategory(PostCategory category) {
        return postMapper.countByCategory(category.name());
    }
    
    public void toggleLike(Long postId, Long userId) {
        boolean hasLiked = postMapper.hasLiked(postId, userId) > 0;
        
        if (hasLiked) {
            postMapper.deleteLike(postId, userId);
            postMapper.decrementLikes(postId);
        } else {
            postMapper.insertLike(postId, userId);
            postMapper.incrementLikes(postId);
        }
    }
} 