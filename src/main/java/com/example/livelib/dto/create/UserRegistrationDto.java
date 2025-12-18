package com.example.livelib.dto.create;

import com.example.livelib.utils.validation.UniqueEmail;
import com.example.livelib.utils.validation.UniqueUsername;
import jakarta.validation.constraints.*;
import lombok.Setter;

@Setter
public class UserRegistrationDto {

    @UniqueUsername
    private String username;

    @UniqueEmail
    private String email;

    private String password;

    private String confirmPassword;

    public UserRegistrationDto() {}

    @NotEmpty(message = "Имя пользователя не должно быть пустым!")
    @Size(min = 5, max = 20, message = "Имя пользователя должно быть от 5 до 20 символов!")
    public String getUsername() {
        return username;
    }


    @NotEmpty(message = "Email не должен быть пустым!")
    @Email(message = "Введите корректный email!")
    public String getEmail() {
        return email;
    }


    @NotEmpty(message = "Пароль не должен быть пустым!")
    @Size(min = 5, max = 20, message = "Пароль должен быть от 5 до 20 символов!")
    public String getPassword() {
        return password;
    }

    @NotEmpty(message = "Подтверждение пароля не должно быть пустым!")
    @Size(min = 5, max = 20, message = "Подтверждение пароля должно быть от 5 до 20 символов!")
    public String getConfirmPassword() {
        return confirmPassword;
    }
}
