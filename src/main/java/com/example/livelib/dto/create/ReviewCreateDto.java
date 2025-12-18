package com.example.livelib.dto.create;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Setter;

@Setter
public class ReviewCreateDto {

    private String userId;
    private String bookId;
    private String reviewText;
    private Integer rating;

    @NotNull(message = "ID пользователя обязателен")
    public String getUserId() {
        return userId;
    }

    @NotNull(message = "ID книги обязателен")
    public String getBookId() {
        return bookId;
    }

    @NotBlank(message = "Текст отзыва обязателен")
    @Size(max = 5000, message = "Отзыв не может превышать 5000 символов")
    public String getReviewText() {
        return reviewText;
    }

    @Positive(message = "Рейтинг должен быть больше 0")
    @Min(value = 1, message = "Минимальный рейтинг 1")
    @Max(value = 10, message = "Максимальный рейтинг 10")
    public Integer getRating() {
        return rating;
    }
}