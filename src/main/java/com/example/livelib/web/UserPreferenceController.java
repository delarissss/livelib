// src/main/java/com/example/livelib/web/UserPreferenceController.java
package com.example.livelib.web;

import com.example.livelib.dto.create.UserPreferenceCreateDto;
import com.example.livelib.dto.showinfo.UserPrefInfo;
import com.example.livelib.models.enums.ItemType;
import com.example.livelib.services.AuthorService;
import com.example.livelib.services.BookService;
import com.example.livelib.services.GenreService;
import com.example.livelib.services.UserPreferenceService;
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

@Slf4j
@Controller
@RequestMapping("/user-preferences")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;
    private final UserService userService;
    private final GenreService genreService;
    private final AuthorService authorService;
    private final BookService bookService;

    @Autowired
    public UserPreferenceController(UserPreferenceService userPreferenceService, UserService userService, GenreService genreService, AuthorService authorService, BookService bookService) {
        this.userPreferenceService = userPreferenceService;
        this.userService = userService;
        this.genreService = genreService;
        this.authorService = authorService;
        this.bookService = bookService;
    }

    @GetMapping("/my-preferences")
    public String showMyPreferences(Principal principal, Model model) {
        String username = principal.getName();
        log.debug("Отображение пользовательских предпочтений для: {}", username);
        var user = userService.findUserByUsername(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден при попытке доступа к предпочтениям", username);
            return "redirect:/users/login";
        }
        List<UserPrefInfo> prefs = userPreferenceService.findUserPreferencesWithDetails(user.getId());
        model.addAttribute("preferences", prefs);
        return "user-preferences/my-preferences";
    }

    @PostMapping("/add")
    public String addPreference(@Valid @ModelAttribute("prefForm") UserPreferenceCreateDto prefForm,
                                Principal principal,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        log.debug("Обработка добавления предпочтения для пользователя {}", username);

        var user = userService.findUserByUsername(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден при попытке добавить предпочтение", username);
            return "redirect:/users/login";
        }
        prefForm.setUserId(user.getId());

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при добавлении предпочтения: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("prefForm", prefForm);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.prefForm", bindingResult);
            return "redirect:/"; // Или перенаправить на страницу выбора
        }

        // Проверка, что itemType и itemId корректны (опционально в контроллере, но может быть в сервисе)
        try {
            ItemType type = ItemType.valueOf(prefForm.getItemType().toUpperCase());
            // Дополнительная проверка существования itemId для типа (например, через сервисы)
            // if (!itemExists(type, prefForm.getItemId())) { throw new ... }
        } catch (IllegalArgumentException e) {
            log.warn("Некорректный тип элемента для предпочтения: {}", prefForm.getItemType());
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректный тип элемента.");
            return "redirect:/";
        }

        userPreferenceService.createUserPreference(prefForm);
        log.info("Предпочтение добавлено для пользователя {} и элемента типа '{}' с ID {}", username, prefForm.getItemType(), prefForm.getItemId());
        redirectAttributes.addFlashAttribute("successMessage", "Предпочтение добавлено.");
        return "redirect:/user-preferences/my-preferences";
    }

    @PostMapping("/delete/{id}")
    public String deletePreference(@PathVariable("id") String id, Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        log.debug("Обработка удаления предпочтения ID {} для пользователя {}", id, username);

        var user = userService.findUserByUsername(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден при попытке удалить предпочтение", username);
            return "redirect:/users/login";
        }

        // Проверяем, принадлежит ли предпочтение пользователю (опционально, если сервис это делает)
        var existingPref = userPreferenceService.findUserPreferencesWithDetails(user.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
        if (existingPref.isEmpty()) {
            log.warn("Пользователь {} пытается удалить чужое предпочтение ID {}", username, id);
            return "redirect:/"; // Или ошибка доступа
        }

        userPreferenceService.deletePreferenceById(id);
        log.info("Предпочтение удалено, ID: {}", id);
        redirectAttributes.addFlashAttribute("successMessage", "Предпочтение удалено.");
        return "redirect:/user-preferences/my-preferences";
    }
}