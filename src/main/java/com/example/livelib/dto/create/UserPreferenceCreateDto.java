package com.example.livelib.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPreferenceCreateDto {

    @NotNull(message = "ID пользователя обязателен")
    private String userId;

    @NotNull(message = "Тип элемента обязателен")
    private String itemType; // "GENRE", "AUTHOR", "BOOK"

    @NotNull(message = "ID элемента обязателен")
    private Long itemId;
}