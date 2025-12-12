package com.example.livelib.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewCreateDto {

    @NotNull(message = "ID пользователя обязателен")
    private String userId;

    @NotNull(message = "ID книги обязателен")
    private String bookId;

    @NotBlank(message = "Текст отзыва обязателен")
    @Size(max = 5000, message = "Отзыв не может превышать 5000 символов")
    private String reviewText;

    @Positive(message = "Рейтинг должен быть от 1 до 10")
    private Integer rating;
}