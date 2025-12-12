// src/main/java/com/example/livelib/services/BookService.java
package com.example.livelib.services;

import com.example.livelib.dto.create.BookCreateDto;
import com.example.livelib.dto.showinfo.BookInfo;

import java.util.List;

public interface BookService {
    void createBook(BookCreateDto bookCreateDto);
    List<BookInfo> findAllBooks();
    BookInfo findBookById(String id);
    List<BookInfo> findBooksByAuthorId(String authorId);
    List<BookInfo> findBooksByGenreId(String genreId);
    List<BookInfo> searchBooks(String searchTerm); // Для поиска по названию, автору, жанру
    void deleteBook(String id);
}