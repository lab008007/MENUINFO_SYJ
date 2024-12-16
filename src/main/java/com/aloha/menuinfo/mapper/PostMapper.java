package com.aloha.menuinfo.mapper;
import com.aloha.menuinfo.domain.Post;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface PostMapper {
    List<Post> findRecentByCategory(@Param("category") String category, @Param("limit") int limit);
    
    void insert(Post post);
    
    @Select("SELECT p.*, u.user_name FROM post p " +
            "JOIN user u ON p.user_id = u.user_id " +
            "WHERE p.category = #{category} " +
            "ORDER BY p.created_at DESC " +
            "LIMIT #{size} OFFSET #{offset}")
    List<Post> findByCategory(@Param("category") String category, 
                            @Param("size") int size, 
                            @Param("offset") int offset);
    
    @Select("SELECT COUNT(*) FROM post WHERE category = #{category}")
    long countByCategory(@Param("category") String category);
    
    List<Post> findByBoardType(String boardType);
    List<Post> findRecentByBoardType(String boardType, int limit);
    
    @Update("UPDATE post SET likes = likes + 1 WHERE post_id = #{postId}")
    void incrementLikes(@Param("postId") Long postId);
    
    @Update("UPDATE post SET likes = likes - 1 WHERE post_id = #{postId}")
    void decrementLikes(@Param("postId") Long postId);
    
    @Insert("INSERT INTO post_like (post_id, user_id) VALUES (#{postId}, #{userId})")
    void insertLike(@Param("postId") Long postId, @Param("userId") Long userId);
    
    @Delete("DELETE FROM post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    void deleteLike(@Param("postId") Long postId, @Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int hasLiked(@Param("postId") Long postId, @Param("userId") Long userId);
    
    @Select("SELECT * FROM post WHERE post_id = #{postId}")
    Post findById(@Param("postId") Long postId);
    
    @Update("UPDATE post SET content = #{content}, updated_at = NOW() WHERE post_id = #{postId}")
    void update(@Param("postId") Long postId, @Param("content") String content);
    
    @Delete("DELETE FROM post WHERE post_id = #{postId}")
    void delete(@Param("postId") Long postId);
    
    @Select("SELECT COUNT(*) FROM post WHERE category = #{boardType}")
    int countByBoardType(@Param("boardType") String boardType);
    
    List<Post> findByBoardTypeWithPaging(@Param("boardType") String boardType,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);
} 