package com.example.livelib.dto.create;

import jakarta.validation.constraints.*;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
public class ReadingLogCreateDto {

    private String userId;
    private String bookId;
    private Integer rating;
    private String note;
    private String status;
    private LocalDateTime dateFinished;


    @NotNull(message = "ID пользователя обязателен")
    public String getUserId() {
        return userId;
    }

    @NotNull(message = "ID книги обязателен")
    public String getBookId() {
        return bookId;
    }

    @Positive(message = "Оценка должна быть от 1 до 10")
    public Integer getRating() {
        return rating;
    }

    @Size(max = 1000, message = "Заметка не может превышать 1000 символов")
    public String getNote() {
        return note;
    }

    @NotEmpty(message = "У заметки и книге должен быть статус")
    public String getStatus() {
        return status;
    }

    @PastOrPresent(message = "Дата завершения чтения не может быть в будущем")
    public LocalDateTime getDateFinished() {
        return dateFinished;
    }

}