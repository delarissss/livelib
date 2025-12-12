package com.example.livelib.repos;

import com.example.livelib.models.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByUserId(String userId);

    List<Review> findByRatingGreaterThan(Integer rating);

    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.isModerated = true WHERE r.id = :reviewId")
    void markAsModerated(String reviewId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Review r WHERE r.user.id = :userId")
    void deleteByUserId(String userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.isModerated = true")
    Long countReviewsByBookId(String bookId);
}