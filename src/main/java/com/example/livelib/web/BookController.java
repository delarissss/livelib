// src/main/java/com/example/livelib/web/BookController.java
package com.example.livelib.web;

import com.example.livelib.dto.create.BookCreateDto;
import com.example.livelib.dto.create.ReviewCreateDto;
import com.example.livelib.dto.showinfo.*;
import com.example.livelib.models.entities.Book;
import com.example.livelib.models.entities.Genre;
import com.example.livelib.services.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final GenreService genreService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final UserPreferenceService userPreferenceService;


    @Autowired
    public BookController(BookService bookService, AuthorService authorService, GenreService genreService, ReviewService reviewService,
                          UserPreferenceService userPreferenceService, UserService userService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.genreService = genreService;
        this.reviewService = reviewService;
        this.userPreferenceService = userPreferenceService;
        this.userService = userService;
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam(required = false) String query, Model model) {
        log.debug("Поиск книг по запросу: {}", query);
        List<BookInfo> results = bookService.searchBooks(query != null ? query : "");
        model.addAttribute("books", results);
        model.addAttribute("searchQuery", query);
        return "books/search-results";
    }

    @GetMapping("/details/{id}")
    public String showBookDetails(@PathVariable("id") String id, Model model) {
        log.debug("Запрос деталей книги с ID: {}", id);
        BookInfo bookInfo = bookService.findBookById(id);
        List<ReviewInfo> reviews = reviewService.findReviewsByBookId(id); // Только модерированные
        model.addAttribute("book", bookInfo);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewForm", new ReviewCreateDto()); // Для формы добавления отзыва
        return "books/details";
    }

    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAddForm(Model model) {
        log.debug("Отображение формы добавления книги");
        model.addAttribute("bookForm", new BookCreateDto());
        model.addAttribute("authors", authorService.findAllAuthors());
        model.addAttribute("genres", genreService.findAllGenres());
        return "books/add";
    }

    @ModelAttribute("bookForm")
    public BookCreateDto initBookForm() {
        return new BookCreateDto();
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addBook(@Valid @ModelAttribute("bookForm") BookCreateDto bookForm,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {
        log.debug("Обработка POST запроса на добавление книги: {}", bookForm.getTitle());
        if (bindingResult.hasErrors()) {
            log.warn("Ошибки валидации при добавлении книги: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("bookForm", bookForm);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.bookForm", bindingResult);
            redirectAttributes.addFlashAttribute("authors", authorService.findAllAuthors());
            redirectAttributes.addFlashAttribute("genres", genreService.findAllGenres());
            return "redirect:/books/add";
        }
        bookService.createBook(bookForm);
        log.info("Книга успешно добавлена: {}", bookForm.getTitle());
        redirectAttributes.addFlashAttribute("successMessage", "Книга '" + bookForm.getTitle() + "' успешно добавлена!");
        return "redirect:/books/all";
    }

    @GetMapping("/all")
    public String showAllBooks(Model model) {
        log.debug("Отображение списка всех книг");
        List<BookInfo> books = bookService.findAllBooks();
        model.addAttribute("books", books);
        return "books/list";
    }
    @GetMapping("/recommended")
    public String showRecommendedBooks(Principal principal, Model model) {
        if (principal == null) {
            log.debug("Запрос рекомендаций от неавторизованного пользователя");
            // Можно перенаправить на главную или страницу входа
            return "redirect:/";
        }
        String username = principal.getName();
        log.debug("Отображение рекомендованных книг для пользователя: {}", username);

        // Получаем ID пользователя
        UserInfo userInfo = userService.findUserByUsername(username); // Предполагаем наличие userService в контроллере или получение через AuthService
        if (userInfo == null) {
            log.warn("Пользователь '{}' не найден при запросе рекомендаций", username);
            return "redirect:/users/login";
        }

        // Получаем рекомендованные книги
        int recommendationLimit = 10; // Можно вынести в настройки
        List<BookInfo> recommendedBooks = bookService.getRecommendedBooksForUser(userInfo.getId(), recommendationLimit); // Используем новый метод

        model.addAttribute("recommendedBooks", recommendedBooks);
        model.addAttribute("user", userInfo); // Возможно, понадобится в шаблоне
        return "books/recommended"; // Новый шаблон
    }

}