package com.example.livelib.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class GenreCreateDto {
    private String name;

    @NotBlank(message = "Название жанра обязательно")
    @Size(max = 100, message = "Название жанра не может превышать 100 символов")
    public String getName() {
        return name;
    }
}