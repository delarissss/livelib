// src/main/java/com/example/livelib/web/ReadingLogController.java
package com.example.livelib.web;

import com.example.livelib.dto.create.ReadingLogCreateDto;
import com.example.livelib.dto.showinfo.RLInfo;
import com.example.livelib.models.entities.ReadingLog;
import com.example.livelib.models.enums.Status;
import com.example.livelib.services.BookService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/reading-log")
public class ReadingLogController {

    private final ReadingLogService readingLogService;
    private final UserService userService;
    private final BookService bookService;

    @Autowired
    public ReadingLogController(ReadingLogService readingLogService, UserService userService, BookService bookService) {
        this.readingLogService = readingLogService;
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/my-books")
    public String showMyReadingLog(Principal principal, Model model) {
        String username = principal.getName();
        log.debug("Отображение читательского дневника для пользователя: {}", username);
        var user = userService.findUserByUsername(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден при попытке доступа к дневнику", username);
            return "redirect:/users/login"; // Или страница ошибки
        }
        List<RLInfo> logs = readingLogService.findReadingLogsByUserId(user.getId());
        model.addAttribute("readingLogs", logs);
        model.addAttribute("statuses", Arrays.stream(Status.values()).map(Enum::name).collect(Collectors.toList()));
        return "reading-log/my-books";
    }

    @GetMapping("/add/{bookId}")
    public String showAddLogForm(@PathVariable("bookId") String bookId, Principal principal, Model model) {
        String username = principal.getName();
        log.debug("Отображение формы добавления в дневник для книги {} и пользователя {}", bookId, username);

        var user = userService.findUserByUsername(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден при попытке доступа к дневнику", username);
            return "redirect:/users/login";
        }

        // Проверяем, есть ли уже запись
        var existingLog = readingLogService.findReadingLogByUserIdAndBookId(user.getId(), bookId);
        if (existingLog != null) {
            log.warn("Запись в дневнике уже существует для пользователя {} и книги {}", user.getId(), bookId);
            return "redirect:/books/details/" + bookId; // Или другая логика
        }

        model.addAttribute("logForm", new ReadingLogCreateDto());
        model.addAttribute("bookId", bookId);
        model.addAttribute("statuses", Arrays.stream(Status.values()).map(Enum::name).collect(Collectors.toList()));
        return "reading-log/add";
    }

    @ModelAttribute("logForm")
    public ReadingLogCreateDto initLogForm() {
        return new ReadingLogCreateDto();
    }

    @PostMapping("/add")
    public String addReadingLog(@Valid @ModelAttribute("logForm") ReadingLogCreateDto logForm,
                                Principal principal,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        log.debug("Обработка добавления в читательский дневник для пользователя {}", username);

        var user = userService.findUserByUsername(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден при попытке добавить в дневник", username);
            return "redirect:/users/login";
        }
        logForm.setUserId(user.getId());

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при добавлении в дневник: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("logForm", logForm);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.logForm", bindingResult);
            redirectAttributes.addFlashAttribute("bookId", logForm.getBookId());
            redirectAttributes.addFlashAttribute("statuses", Arrays.stream(Status.values()).map(Enum::name).collect(Collectors.toList()));
            return "redirect:/reading-log/add/" + logForm.getBookId();
        }

        readingLogService.createReadingLog(logForm);
        log.info("Запись в читательском дневнике создана для пользователя {} и книги {}", user.getId(), logForm.getBookId());
        redirectAttributes.addFlashAttribute("successMessage", "Книга добавлена в ваш читательский дневник!");
        return "redirect:/books/details/" + logForm.getBookId();
    }

    @PostMapping("/update/{id}")
    public String updateReadingLog(@PathVariable("id") String id,
                                   @Valid @ModelAttribute("logForm") ReadingLogCreateDto logForm,
                                   Principal principal,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        log.debug("Обработка обновления записи в дневнике ID {} для пользователя {}", id, username);

        var user = userService.findUserByUsername(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден при попытке обновить дневник", username);
            return "redirect:/users/login";
        }

        // Проверяем, принадлежит ли запись пользователю
        var existingLog = readingLogService.findReadingLogById(id);
        if (!existingLog.getUser().getId().equals(user.getId())) {
            log.warn("Пользователь {} пытается обновить чужую запись в дневнике ID {}", username, id);
            return "redirect:/"; // Или ошибка доступа
        }

        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при обновлении дневника: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("logForm", logForm);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.logForm", bindingResult);
            redirectAttributes.addFlashAttribute("logId", id); // Передаем ID для формы
            redirectAttributes.addFlashAttribute("statuses", Arrays.stream(Status.values()).map(Enum::name).collect(Collectors.toList()));
            return "redirect:/reading-log/edit/" + id; // Предполагаем наличие такой формы
        }

        readingLogService.updateReadingLog(id, logForm);
        log.info("Запись в читательском дневнике обновлена, ID: {}", id);
        redirectAttributes.addFlashAttribute("successMessage", "Запись в дневнике обновлена!");
        return "redirect:/reading-log/my-books";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Principal principal, Model model) {
        String username = principal.getName();
        log.debug("Отображение формы редактирования записи в дневнике ID {} для пользователя {}", id, username);

        var user = userService.findUserByUsername(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден при попытке редактировать дневник", username);
            return "redirect:/users/login";
        }

        var existingLog = readingLogService.findReadingLogById(id);
        if (!existingLog.getUser().getId().equals(user.getId())) {
            log.warn("Пользователь {} пытается редактировать чужую запись в дневнике ID {}", username, id);
            return "redirect:/"; // Или ошибка доступа
        }

        // Преобразуем RLInfo обратно в DTO для формы
        ReadingLogCreateDto logForm = new ReadingLogCreateDto();
        logForm.setRating(existingLog.getRating());
        logForm.setNote(existingLog.getNote());

        // --- ИСПРАВЛЕНО ---
        // Явно указываем тип и вызываем name()
        Status statusEnum = Status.valueOf(existingLog.getStatus());
        String statusName = (statusEnum != null) ? statusEnum.name() : null;
        logForm.setStatus(statusName);
        // --- /ИСПРАВЛЕНО ---

        model.addAttribute("logForm", logForm);
        model.addAttribute("logId", id);
        model.addAttribute("statuses", Arrays.stream(Status.values()).map(Enum::name).collect(Collectors.toList()));
        return "reading-log/edit"; // Предполагаем, что шаблон edit.html существует
    }

    @PostMapping("/delete/{id}")
    public String deleteReadingLog(@PathVariable("id") String id, Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        log.debug("Обработка удаления записи в дневнике ID {} для пользователя {}", id, username);

        var user = userService.findUserByUsername(username);
        if (user == null) {
            log.warn("Пользователь '{}' не найден при попытке удалить дневник", username);
            return "redirect:/users/login";
        }

        var existingLog = readingLogService.findReadingLogById(id);
        if (!existingLog.getUser().getId().equals(user.getId())) {
            log.warn("Пользователь {} пытается удалить чужую запись в дневнике ID {}", username, id);
            return "redirect:/"; // Или ошибка доступа
        }

        readingLogService.deleteReadingLog(id);
        log.info("Запись в читательском дневнике удалена, ID: {}", id);
        redirectAttributes.addFlashAttribute("successMessage", "Запись в дневнике удалена.");
        return "redirect:/reading-log/my-books";
    }
}