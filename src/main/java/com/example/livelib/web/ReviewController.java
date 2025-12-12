// src/main/java/com/example/livelib/web/ReviewController.java
package com.example.livelib.web;

import com.example.livelib.dto.create.ReviewCreateDto;
import com.example.livelib.dto.showinfo.ReviewInfo;
import com.example.livelib.services.BookService;
import com.example.livelib.services.ReviewService;
import com.example.livelib.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final BookService bookService; // Для проверки существования книги

    @Autowired
    public ReviewController(ReviewService reviewService, UserService userService, BookService bookService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.bookService = bookService;
    }

    @PostMapping("/add")
    public String addReview(@Valid @ModelAttribute("reviewForm") ReviewCreateDto reviewForm,
                            Principal principal,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        log.debug("Обработка добавления отзыва для книги {} от пользователя {}", reviewForm.getBookId(), username);

        var user = userService.findUserByUsername(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден при попытке добавить отзыв", username);
            return "redirect:/users/login";
        }
        reviewForm.setUserId(user.getId());

        // Проверка существования книги (опционально, если не валидируется на уровне сервиса)
        if (bookService.findBookById(reviewForm.getBookId()) == null) {
            log.warn("Попытка оставить отзыв на несуществующую книгу ID: {}", reviewForm.getBookId());
            redirectAttributes.addFlashAttribute("errorMessage", "Книга не найдена.");
            return "redirect:/books/details/" + reviewForm.getBookId();
        }

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при добавлении отзыва: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("reviewForm", reviewForm);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.reviewForm", bindingResult);
            return "redirect:/books/details/" + reviewForm.getBookId();
        }

        reviewService.createReview(reviewForm);
        log.info("Отзыв добавлен пользователем {} для книги {}", username, reviewForm.getBookId());
        redirectAttributes.addFlashAttribute("successMessage", "Ваш отзыв отправлен на модерацию.");
        return "redirect:/books/details/" + reviewForm.getBookId();
    }

    // --- Контроллер для администратора ---
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAllReviews(Model model) {
        log.debug("Отображение всех отзывов для администратора");
        List<ReviewInfo> allReviews = reviewService.findAllReviews();
        model.addAttribute("reviews", allReviews);
        return "admin/reviews";
    }

    @PostMapping("/admin/moderate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String moderateReview(@PathVariable("id") String reviewId, RedirectAttributes redirectAttributes) {
        log.debug("Модерация отзыва ID: {}", reviewId);
        reviewService.markReviewAsModerated(reviewId);
        redirectAttributes.addFlashAttribute("successMessage", "Отзыв ID " + reviewId + " отмечен как модерированный.");
        return "redirect:/reviews/admin";
    }

    @PostMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteReview(@PathVariable("id") String reviewId, RedirectAttributes redirectAttributes) {
        log.debug("Удаление отзыва ID: {}", reviewId);
        // Проверка на существование может быть в сервисе
        reviewService.deleteReview(reviewId);
        redirectAttributes.addFlashAttribute("successMessage", "Отзыв ID " + reviewId + " удален.");
        return "redirect:/reviews/admin";
    }
}