package it.cgmconsulting.mscategory.repository;

import it.cgmconsulting.mscategory.entity.CategoryId;
import it.cgmconsulting.mscategory.entity.CategoryPosts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CategoryPostsRepository extends JpaRepository<CategoryPosts, CategoryId> {

    List<CategoryPosts> findByCategoryIdPostId(long postId);

    @Modifying
    @Transactional
    @Query(value = """
                   INSERT INTO category_posts (post_id, category_id)
                   VALUES (:postId, :categoryId)
                   """, nativeQuery = true )
    void insertCategoryPost(long postId, long categoryId);

    @Modifying
    @Transactional
    @Query(value = """
                   DELETE FROM category_posts
                   WHERE post_id = :postId
                   """, nativeQuery = true )
    void deleteCategoryPost(long postId);


    @Query(value = """
                   SELECT cat.categoryId.categoryId.categoryName
                   FROM CategoryPosts cat
                   WHERE cat.categoryId.postId = :postId
                   AND cat.categoryId.categoryId.visible = true
                   """)
    Set<String> getCategoriesByPost(@Param("postId") long postId);

    @Query(value = """
                   SELECT catp.categoryId.postId
                   FROM CategoryPosts catp
                   WHERE catp.categoryId.categoryId.categoryName = :categoryName
                   AND catp.categoryId.categoryId.visible = true
                   """)
    Set<Long> getPostsByCategory(@Param("categoryName") String categoryName);

    @Query(value = """
                   SELECT catp
                   FROM CategoryPosts catp
                   WHERE catp.categoryId.categoryId.visible = true
                   ORDER BY catp.categoryId.postId
                   """)
    List<CategoryPosts> findAllCategoriesByPosts();

//    @Query(value = """ NON FUNZIONA
//                   SELECT (catp.categoryId.postId,
//                   catp.categoryId.categoryId.categoryName)
//                   FROM CategoryPosts catp
//                   WHERE catp.categoryId.categoryId.visible = true
//                   ORDER BY catp.categoryId.postId
//                   """)
//    List<Object[]> findAllCategoriesByPosts2();
}
