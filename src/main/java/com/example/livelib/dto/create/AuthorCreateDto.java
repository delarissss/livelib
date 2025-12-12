package com.example.livelib.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class AuthorCreateDto {

    @NotBlank(message = "Имя и фамилия автора обязательны")
    @Size(max = 255, message = "Полное имя автора не может превышать 255 символов")
    private String fullName;

}
