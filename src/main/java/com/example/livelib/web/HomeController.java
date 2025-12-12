// src/main/java/com/example/livelib/web/HomeController.java
package com.example.livelib.web;

import com.example.livelib.dto.showinfo.BookInfo;
import com.example.livelib.services.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
public class HomeController {

    private final BookService bookService;

    @Autowired
    public HomeController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        log.debug("Отображение главной страницы");
        // Пример: отображение последних добавленных книг или случайных книг
        List<BookInfo> allBooks = bookService.findAllBooks();
        // Берем первые 10 книг для отображения на главной
        List<BookInfo> featuredBooks = allBooks.stream().limit(10).toList();
        model.addAttribute("featuredBooks", featuredBooks);
        return "index";
    }
}