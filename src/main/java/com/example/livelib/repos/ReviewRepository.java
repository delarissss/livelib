package com.example.livelib.repos;

import com.example.livelib.models.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByUserId(String userId);
    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.isModerated = true WHERE r.id = :reviewId")
    void markAsModerated(@Param("reviewId") String reviewId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Review r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.isModerated = true")
    Long countReviewsByBookId(@Param("bookId") String bookId);

    @Query("SELECT r FROM Review r WHERE r.book.id = :bookId AND r.isModerated = true")
    List<Review> findByBookIdAndIsModeratedTrue(@Param("bookId") String bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId AND r.isModerated = true")
    Double calculateAverageRatingForBook(@Param("bookId") String bookId);
}