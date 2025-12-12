package com.example.livelib.dto.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingLogCreateDto {

    @NotNull(message = "ID пользователя обязателен")
    private String userId;

    @NotNull(message = "ID книги обязателен")
    private String bookId;

    @Positive(message = "Оценка должна быть от 1 до 10")
    private Integer rating;

    @Size(max = 1000, message = "Заметка не может превышать 1000 символов")
    private String note;

    private String status;

    private LocalDateTime dateFinished;
}