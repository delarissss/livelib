package com.example.livelib.services;

import com.example.livelib.dto.create.BookCreateDto;
import com.example.livelib.dto.showinfo.BookInfo;
import java.util.List;

public interface BookService {
    void createBook(BookCreateDto bookCreateDto);
    List<BookInfo> findAllBooks();
    BookInfo findBookById(String id);
    List<BookInfo> findBooksByAuthorId(String authorId); // Принимает String id
    List<BookInfo> findBooksByGenreId(String genreId);   // Принимает String id
    List<BookInfo> searchBooks(String searchTerm); // Для поиска по названию, автору, жанру
    List<BookInfo> getTopRatedBooks(int limit); // Новый метод
    List<BookInfo> getRecommendedBooksForUser(String userId, int limit); // Новый метод
    void deleteBook(String id);
}