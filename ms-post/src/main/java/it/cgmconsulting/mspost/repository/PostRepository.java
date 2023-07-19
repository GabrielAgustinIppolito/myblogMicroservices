package it.cgmconsulting.mspost.repository;

import it.cgmconsulting.mspost.entity.Post;
import it.cgmconsulting.mspost.payload.response.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {
    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, long postId);

    @Query(value = """
                   SELECT new it.cgmconsulting.mspost.payload.response.PostResponse(
                   p.id,
                   p.title,
                   p.content,
                   p.publishedAt,
                   p.authorId
                   ) FROM Post p 
                   WHERE p.id = :postId 
                   AND (p.publishedAt IS NOT NULL 
                   AND p.publishedAt <= :now)
                   """)
    Optional<PostResponse> getPost(@Param("postId") long postId, @Param("now")LocalDateTime now);

    @Query(value = """
                   SELECT new it.cgmconsulting.mspost.payload.response.PostListResponseComplete(
                   p.id,
                   p.title,
                   p.overview,
                   p.authorId
                   ) FROM Post p 
                   WHERE p.publishedAt IS NOT NULL 
                   AND p.publishedAt <= :now
                   """)
    List<PostListResponseComplete> getAllPublishedPost(@Param("now")LocalDateTime now);

    @Query(value = """
                   SELECT new it.cgmconsulting.mspost.payload.response.PostListResponse(
                   p.id,
                   p.title,
                   p.overview
                   ) FROM Post p 
                   WHERE  p.publishedAt IS NOT NULL 
                   AND p.publishedAt <= :now
                   AND p.id IN (:postIds)
                   ORDER BY p.publishedAt DESC
                   """)
    Set<PostListResponse> getPostsByCategory(@Param("postIds") Set<Long> postIds, @Param("now")LocalDateTime now);

    @Query(value = """
                   SELECT new it.cgmconsulting.mspost.payload.response.UserPostResponse(
                   p.authorId,
                   p.id
                   ) FROM Post p
                   GROUP BY p.authorId
                   """)
    List<UserPostResponse> countPostsByAuthor();
}
