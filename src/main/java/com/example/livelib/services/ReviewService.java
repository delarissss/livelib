// src/main/java/com/example/livelib/services/ReviewService.java
package com.example.livelib.services;

import com.example.livelib.dto.create.ReviewCreateDto;
import com.example.livelib.dto.showinfo.ReviewInfo;

import java.util.List;

public interface ReviewService {
    void createReview(ReviewCreateDto reviewCreateDto);
    List<ReviewInfo> findReviewsByBookId(String bookId); // Только модерированные
    List<ReviewInfo> findReviewsByUserId(String userId);
    List<ReviewInfo> findAllReviews(); // Для администратора
    ReviewInfo findReviewById(String id);
    void markReviewAsModerated(String reviewId); // Для администратора
    void deleteReview(String id); // Для администратора
    void deleteReviewsByUserId(String userId); // При удалении пользователя
}