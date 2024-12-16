package com.aloha.menuinfo.controller;

import com.aloha.menuinfo.domain.Post;
import com.aloha.menuinfo.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.aloha.menuinfo.security.PrincipalDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import com.aloha.menuinfo.service.PostService;

@Slf4j
@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
    private final PostService postService;
    private final PostMapper postMapper;
    
    @GetMapping("/{boardType}")
    public String boardList(
        @PathVariable String boardType,
        @RequestParam(defaultValue = "1") int page,
        Model model
    ) {
        int pageSize = 10;
        int offset = (page - 1) * pageSize;
        
        System.out.println("Page: " + page);
        System.out.println("Offset: " + offset);
        System.out.println("PageSize: " + pageSize);
        
        List<Post> posts = postMapper.findByBoardTypeWithPaging(boardType, offset, pageSize);
        System.out.println("Retrieved posts: " + posts.size());
        
        int totalPosts = postMapper.countByBoardType(boardType);
        System.out.println("Total posts: " + totalPosts);
        
        int totalPages = (int) Math.ceil((double) totalPosts / pageSize);
        System.out.println("Total pages: " + totalPages);
        
        model.addAttribute("posts", posts);
        model.addAttribute("boardType", boardType);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        
        return "board/" + boardType;
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{boardType}/write")
    public String writeForm(@PathVariable String boardType, Model model) {
        model.addAttribute("boardType", boardType);
        return "board/write";
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardType}/write")
    public String write(
        @PathVariable String boardType,
        @RequestParam String content,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            
            Post post = Post.builder()
                    .userId(principalDetails.getUser().getUserId())
                    .category(boardType)
                    .content(content)
                    .imagePath(null)
                    .likes(0)
                    .createdAt(LocalDateTime.now())
                    .build();
                    
            System.out.println("저장할 게시물 카테고리: " + boardType);
            System.out.println("저장할 게시���: " + post);
            postMapper.insert(post);
            System.out.println("게시물 저장 완료");
            
            return "redirect:/board/" + boardType;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "게시물 저장에 실패했습니다.");
            return "redirect:/board/" + boardType + "/write";
        }
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardType}/{postId}/like")
    @ResponseBody
    public ResponseEntity<?> toggleLike(
            @PathVariable Long postId,
            @PathVariable String boardType) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            PrincipalDetails principalDetails = (PrincipalDetails) auth.getPrincipal();
            Long userId = principalDetails.getUser().getUserId();
            
            postService.toggleLike(postId, userId);
            
            Post post = postMapper.findById(postId);
            return ResponseEntity.ok().body(Map.of(
                "likes", post.getLikes(),
                "hasLiked", postMapper.hasLiked(postId, userId) > 0
            ));
        } catch (Exception e) {
            log.error("좋아요 처리 중 오류 발생", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "좋아요 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{boardType}/{postId}/edit")
    public String editForm(
            @PathVariable String boardType,
            @PathVariable Long postId,
            Model model) {
        Post post = postMapper.findById(postId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        PrincipalDetails principalDetails = (PrincipalDetails) auth.getPrincipal();
        
        if (!post.getUserId().equals(principalDetails.getUser().getUserId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        
        model.addAttribute("post", post);
        model.addAttribute("boardType", boardType);
        return "board/edit";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardType}/{postId}/edit")
    public String edit(
            @PathVariable String boardType,
            @PathVariable Long postId,
            @RequestParam String content) {
        Post post = postMapper.findById(postId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        PrincipalDetails principalDetails = (PrincipalDetails) auth.getPrincipal();
        
        if (!post.getUserId().equals(principalDetails.getUser().getUserId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        
        postMapper.update(postId, content);
        return "redirect:/board/" + boardType;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{boardType}/{postId}/delete")
    public String delete(
            @PathVariable String boardType,
            @PathVariable Long postId) {
        Post post = postMapper.findById(postId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        PrincipalDetails principalDetails = (PrincipalDetails) auth.getPrincipal();
        
        if (!post.getUserId().equals(principalDetails.getUser().getUserId())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
        
        postMapper.delete(postId);
        return "redirect:/board/" + boardType;
    }
}