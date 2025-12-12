package com.example.livelib.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class GenreCreateDto {

    @NotBlank(message = "Название жанра обязательно")
    @Size(max = 100, message = "Название жанра не может превышать 100 символов")
    private String name;
}