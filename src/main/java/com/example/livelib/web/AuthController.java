// src/main/java/com/example/livelib/web/AuthController.java
package com.example.livelib.web;

import com.example.livelib.dto.create.UserRegistrationDto;
import com.example.livelib.dto.showinfo.RLInfo;
import com.example.livelib.dto.showinfo.UserInfo;
import com.example.livelib.models.entities.User;
import com.example.livelib.services.AuthService;
import com.example.livelib.services.ReadingLogService;
import com.example.livelib.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final ReadingLogService readingLogService;

    @Autowired
    public AuthController(AuthService authService, UserService userService, ReadingLogService readingLogService) {
        this.authService = authService;
        this.userService = userService;
        this.readingLogService = readingLogService;
        log.info("AuthController инициализирован");
    }

    @ModelAttribute("userRegistrationDto")
    public UserRegistrationDto initForm() {
        return new UserRegistrationDto();
    }

    @GetMapping("/register")
    public String register() {
        log.debug("Отображение страницы регистрации");
        return "users/register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid UserRegistrationDto userRegistrationDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        log.debug("Обработка регистрации пользователя: {}", userRegistrationDto.getEmail());
        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при регистрации: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("userRegistrationDto", userRegistrationDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegistrationDto", bindingResult);
            return "redirect:/users/register";
        }
        try {
            authService.register(userRegistrationDto);
            log.info("Пользователь успешно зарегистрирован: {}", userRegistrationDto.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Регистрация прошла успешно! Пожалуйста, войдите.");
            return "redirect:/users/login";
        } catch (RuntimeException e) {
            log.error("Ошибка регистрации: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("userRegistrationDto", userRegistrationDto);
            return "redirect:/users/register";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        log.debug("Отображение страницы входа");
        if (error != null) {
            model.addAttribute("errorMessage", "Неверный email или пароль.");
        }
        return "users/login";
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        String username = principal.getName();
        log.debug("Отображение профиля пользователя: {}", username);

        // Получаем информацию о пользователе
        UserInfo userInfo = userService.findUserByUsername(username); // Используем UserService для получения UserInfo
        if (userInfo == null) {
            log.warn("Профиль запрашивается для несуществующего пользователя: {}", username);
            return "redirect:/users/login"; // Или ошибка 404
        }

        // Получаем записи в дневнике пользователя
        List<RLInfo> readingLogs = readingLogService.findReadingLogsByUserId(userInfo.getId());

        // Группируем книги по статусам
        Map<String, List<RLInfo>> booksByStatus = readingLogs.stream()
                .collect(Collectors.groupingBy(
                        rl -> rl.getStatus() != null ? rl.getStatus() : "UNKNOWN" // Обработка null статуса
                ));

        model.addAttribute("user", userInfo);
        model.addAttribute("booksByStatus", booksByStatus); // Передаем сгруппированные книги
        return "users/profile";
    }
    // В AuthController
    @GetMapping("/users/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true); // Передаем флаг ошибки в шаблон
        return "users/login"; // Возвращаем шаблон входа
    }
}