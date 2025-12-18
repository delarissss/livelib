package com.example.livelib.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;

@Setter
public class AuthorCreateDto {

    private String fullName;

    @NotBlank(message = "Имя и фамилия автора обязательны")
    @Size(max = 255, message = "Полное имя автора не может превышать 255 символов")
    public String getFullName() {
        return fullName;
    };
}
