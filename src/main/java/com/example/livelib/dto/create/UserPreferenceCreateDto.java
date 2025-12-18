package com.example.livelib.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.Setter;

@Setter
public class UserPreferenceCreateDto {

    private String itemType;
    private String itemId;
    private String userId;

    @NotNull(message = "Тип элемента обязателен")
    public String getItemType() {
        return itemType;
    } // "GENRE", "AUTHOR", "BOOK"

    @NotNull(message = "ID элемента обязателен")
    public String getItemId() {
        return itemId;
    }
    @NotNull(message = "ID пользователя обязательно")
    public String getUserId() { // <-- Геттер для userId
        return userId;
    }
}