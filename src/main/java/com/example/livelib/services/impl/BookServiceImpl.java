// src/main/java/com/example/livelib/services/impl/BookServiceImpl.java
package com.example.livelib.services.impl;

import com.example.livelib.dto.create.BookCreateDto;
import com.example.livelib.dto.showinfo.AuthorInfo;
import com.example.livelib.dto.showinfo.BookInfo;
import com.example.livelib.dto.showinfo.GenreInfo;
import com.example.livelib.models.entities.Author;
import com.example.livelib.models.entities.Book;
import com.example.livelib.models.entities.Genre;
import com.example.livelib.models.entities.Review;
import com.example.livelib.repos.AuthorRepository;
import com.example.livelib.repos.BookRepository;
import com.example.livelib.repos.GenreRepository;
import com.example.livelib.repos.ReviewRepository; // Для подсчета отзывов и среднего рейтинга
import com.example.livelib.services.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final ReviewRepository reviewRepository; // Репозиторий для отзывов
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    @CacheEvict(value = {"books", "book"}, allEntries = true)
    public void createBook(BookCreateDto bookCreateDto) {
        log.debug("Создание книги: {}", bookCreateDto.getTitle());
        Book book = new Book();
        book.setTitle(bookCreateDto.getTitle());
        book.setDescription(bookCreateDto.getDescription());
        book.setIsbn(bookCreateDto.getIsbn());
        book.setPublicationYear(String.valueOf(bookCreateDto.getPublicationYear()));

        // Установка автора
        Author author = authorRepository.findById(String.valueOf(bookCreateDto.getAuthorId()))
                .orElseThrow(() -> {
                    log.warn("Автор не найден при создании книги, ID: {}", bookCreateDto.getAuthorId());
                    return new RuntimeException("Author not found with id: " + bookCreateDto.getAuthorId());
                });
        book.setAuthor(author);

        // Установка жанров
        Set<Genre> genres = bookCreateDto.getGenreIds().stream()
                .map(genreId -> genreRepository.findById(String.valueOf(genreId))
                        .orElseThrow(() -> {
                            log.warn("Жанр не найден при создании книги, ID: {}", genreId);
                            return new RuntimeException("Genre not found with id: " + genreId);
                        }))
                .collect(Collectors.toSet());
        book.getGenres().addAll(genres);

        bookRepository.save(book);
        log.info("Книга создана: {}", book.getTitle());
    }

    @Override
    @Cacheable(value = "books", key = "'all'")
    public List<BookInfo> findAllBooks() {
        log.debug("Получение списка всех книг");
        List<Book> books = bookRepository.findAll();
        List<BookInfo> bookInfos = mapBooksToBookInfo(books);
        log.info("Найдено книг: {}", bookInfos.size());
        return bookInfos;
    }

    @Override
    @Cacheable(value = "book", key = "#id")
    public BookInfo findBookById(String id) {
        log.debug("Поиск книги по ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Книга не найдена по ID: {}", id);
                    return new RuntimeException("Book not found with id: " + id);
                });
        BookInfo bookInfo = modelMapper.map(book, BookInfo.class);
        // Заполняем AuthorInfo и GenreInfo вручную, если ModelMapper не делает это автоматически
        bookInfo.setAuthor(modelMapper.map(book.getAuthor(), AuthorInfo.class));
        bookInfo.setGenres(book.getGenres().stream()
                .map(genre -> modelMapper.map(genre, GenreInfo.class))
                .collect(Collectors.toList()));

        // Заполняем reviewCount и averageRating
        fillReviewStats(bookInfo, book.getId());

        return bookInfo;
    }

    @Override
    public List<BookInfo> findBooksByAuthorId(String authorId) {
        log.debug("Поиск книг по ID автора: {}", authorId);
        List<Book> books = bookRepository.findByAuthorId(Long.valueOf(authorId));
        return mapBooksToBookInfo(books);
    }

    @Override
    public List<BookInfo> findBooksByGenreId(String genreId) {
        log.debug("Поиск книг по ID жанра: {}", genreId);
        List<Book> books = bookRepository.findByGenres_Id(Long.valueOf(genreId));
        return mapBooksToBookInfo(books);
    }

    @Override
    public List<BookInfo> searchBooks(String searchTerm) {
        log.debug("Поиск книг по термину: {}", searchTerm);
        // Простой поиск: по названию и, возможно, описанию
        List<Book> books = bookRepository.findAll().stream()
                .filter(book ->
                        book.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                book.getDescription() != null && book.getDescription().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                book.getAuthor().getFullName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                book.getGenres().stream().anyMatch(g -> g.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                )
                .collect(Collectors.toList());
        return mapBooksToBookInfo(books);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"books", "book"}, allEntries = true)
    public void deleteBook(String id) {
        log.debug("Удаление книги по ID: {}", id);
        if (!bookRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующую книгу с ID: {}", id);
            throw new RuntimeException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        log.info("Книга удалена, ID: {}", id);
    }

    // --- Вспомогательные методы ---

    private List<BookInfo> mapBooksToBookInfo(List<Book> books) {
        return books.stream()
                .map(book -> {
                    BookInfo info = modelMapper.map(book, BookInfo.class);
                    info.setAuthor(modelMapper.map(book.getAuthor(), AuthorInfo.class));
                    info.setGenres(book.getGenres().stream()
                            .map(genre -> modelMapper.map(genre, GenreInfo.class))
                            .collect(Collectors.toList()));
                    // Заполняем статистику отзывов
                    fillReviewStats(info, book.getId());
                    return info;
                })
                .collect(Collectors.toList());
    }

    private void fillReviewStats(BookInfo bookInfo, String bookId) {
        Long reviewCount = reviewRepository.countReviewsByBookId(bookId);
        bookInfo.setReviewCount(reviewCount);

        // Вычисление среднего рейтинга через Stream API из отзывов
        double avgRating = reviewRepository.findAll().stream()
                .filter(r -> r.getBook().getId().equals(bookId) && r.getIsModerated()) // Только модерированные
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0); // Возвращаем 0.0, если нет отзывов
        bookInfo.setAverageRating(avgRating);
    }
}