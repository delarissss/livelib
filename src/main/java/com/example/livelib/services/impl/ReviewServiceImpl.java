package com.example.livelib.services.impl;

import com.example.livelib.dto.create.ReviewCreateDto;
import com.example.livelib.dto.showinfo.ReviewInfo;
import com.example.livelib.models.entities.Book;
import com.example.livelib.models.entities.Review;
import com.example.livelib.models.entities.User;
import com.example.livelib.repos.BookRepository;
import com.example.livelib.repos.ReviewRepository;
import com.example.livelib.repos.UserRepository;
import com.example.livelib.services.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    @CacheEvict(value = {"reviews", "book"}, allEntries = true) // Очищаем кэш книги, так как изменится статистика
    public void createReview(ReviewCreateDto reviewCreateDto) {
        log.debug("Создание отзыва для книги ID: {} от пользователя ID: {}", reviewCreateDto.getBookId(), reviewCreateDto.getUserId());
        User user = userRepository.findById(reviewCreateDto.getUserId())
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден при создании отзыва, ID: {}", reviewCreateDto.getUserId());
                    return new RuntimeException("User not found with id: " + reviewCreateDto.getUserId());
                });
        Book book = bookRepository.findById(reviewCreateDto.getBookId())
                .orElseThrow(() -> {
                    log.warn("Книга не найдена при создании отзыва, ID: {}", reviewCreateDto.getBookId());
                    return new RuntimeException("Book not found with id: " + reviewCreateDto.getBookId());
                });

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setReviewText(reviewCreateDto.getReviewText());
        review.setRating(reviewCreateDto.getRating());
        // isModerated по умолчанию false, как в сущности

        reviewRepository.save(review);
        log.info("Отзыв создан для книги '{}' пользователем '{}'", book.getTitle(), user.getUsername());
    }

    @Override
    public List<ReviewInfo> findReviewsByBookId(String bookId) {
        log.debug("Поиск модерированных отзывов для книги ID: {}", bookId);
        // Используем метод репозитория для фильтрации по bookId и isModerated
        List<Review> reviews = reviewRepository.findByBookIdAndIsModeratedTrue(bookId);
        return reviews.stream()
                .map(review -> modelMapper.map(review, ReviewInfo.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewInfo> findReviewsByUserId(String userId) {
        log.debug("Поиск отзывов пользователя ID: {}", userId);
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(review -> modelMapper.map(review, ReviewInfo.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewInfo> findAllReviews() {
        log.debug("Получение всех отзывов (для администратора)");
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .map(review -> modelMapper.map(review, ReviewInfo.class))
                .collect(Collectors.toList());
    }

    @Override
    public ReviewInfo findReviewById(String id) {
        log.debug("Поиск отзыва по ID: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Отзыв не найден по ID: {}", id);
                    return new RuntimeException("Review not found with id: " + id);
                });
        return modelMapper.map(review, ReviewInfo.class);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"reviews", "book"}, allEntries = true) // Очищаем кэш книги, так как изменится статистика
    public void markReviewAsModerated(String reviewId) {
        log.debug("Модерация отзыва ID: {}", reviewId);
        reviewRepository.markAsModerated(reviewId); // Этот метод уже есть в репозитории
        log.info("Отзыв ID {} отмечен как модерированный", reviewId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"reviews", "book"}, allEntries = true) // Очищаем кэш книги, так как изменится статистика
    public void deleteReview(String id) {
        log.debug("Удаление отзыва по ID: {}", id);
        if (!reviewRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующий отзыв с ID: {}", id);
            throw new RuntimeException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
        log.info("Отзыв удален, ID: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"reviews", "book"}, allEntries = true) // Очищаем кэш книги, так как изменится статистика
    public void deleteReviewsByUserId(String userId) {
        log.debug("Удаление всех отзывов пользователя ID: {}", userId);
        reviewRepository.deleteByUserId(userId);
        log.info("Все отзывы пользователя ID {} удалены", userId);
    }

    @Override
    public Double getAverageRatingForBook(String bookId) {
        log.debug("Получение среднего рейтинга для книги ID: {}", bookId);
        // Используем метод репозитория для вычисления среднего
        return reviewRepository.calculateAverageRatingForBook(bookId);
    }
}