package com.example.livelib.services.impl;

import com.example.livelib.dto.create.BookCreateDto;
import com.example.livelib.dto.showinfo.AuthorInfo;
import com.example.livelib.dto.showinfo.BookInfo;
import com.example.livelib.dto.showinfo.GenreInfo;
import com.example.livelib.models.entities.Author;
import com.example.livelib.models.entities.Book;
import com.example.livelib.models.entities.Genre;
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

        Author author = authorRepository.findById(bookCreateDto.getAuthorId()) // Изменено: String id
                .orElseThrow(() -> {
                    log.warn("Автор не найден при создании книги, ID: {}", bookCreateDto.getAuthorId());
                    return new RuntimeException("Author not found with id: " + bookCreateDto.getAuthorId());
                });
        book.setAuthor(author);

        // Установка жанров (ожидаем List<String> ids)
        Set<Genre> genres = bookCreateDto.getGenreIds().stream() // Изменено: List<String> ids
                .map(genreId -> genreRepository.findById(genreId) // Изменено: String id
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
        bookInfo.setAuthor(modelMapper.map(book.getAuthor(), AuthorInfo.class));
        bookInfo.setGenres(book.getGenres().stream()
                .map(genre -> modelMapper.map(genre, GenreInfo.class))
                .collect(Collectors.toList()));

        fillReviewStats(bookInfo, book.getId());
        return bookInfo;
    }

    @Override
    public List<BookInfo> findBooksByAuthorId(String authorId) { // Принимает String id
        log.debug("Поиск книг по ID автора: {}", authorId);
        List<Book> books = bookRepository.findByAuthorId(authorId); // Репозиторий должен принимать String id
        return mapBooksToBookInfo(books);
    }

    @Override
    public List<BookInfo> findBooksByGenreId(String genreId) { // Принимает String id
        log.debug("Поиск книг по ID жанра: {}", genreId);
        List<Book> books = bookRepository.findByGenresId(genreId); // Репозиторий должен принимать String id
        return mapBooksToBookInfo(books);
    }

    @Override
    public List<BookInfo> searchBooks(String searchTerm) {
        log.debug("Поиск книг по термину: {}", searchTerm);
        // Используем метод из репозитория для поиска на уровне БД
        List<Book> books = bookRepository.searchByTitleOrAuthorOrGenre(searchTerm);
        return mapBooksToBookInfo(books);
    }

    @Override
    public List<BookInfo> getTopRatedBooks(int limit) {
        log.debug("Получение топ {} книг по рейтингу", limit);
        // Используем метод из репозитория для получения топ книг
        List<Book> topBooks = bookRepository.findTopBooksByAverageRating(limit);
        return mapBooksToBookInfo(topBooks);
    }

    @Override
    public List<BookInfo> getRecommendedBooksForUser(String userId, int limit) {
        log.debug("Получение {} рекомендованных книг для пользователя: {}", limit, userId);
        // Логика рекомендаций
        // 1. Получить предпочтения пользователя (жанры, авторы)
        // 2. Получить книги, которые пользователь уже оценил (ReadingLog) и их рейтинги
        // 3. Найти книги, соответствующие предпочтениям и/или авторам, исключая уже прочитанные
        // 4. Отсортировать по потенциальному интересу (например, по среднему рейтингу среди похожих пользователей или просто по среднему рейтингу для простоты)
        // 5. Вернуть top 'limit' книг

        // Пример упрощенной логики:
        // - Найти жанры и авторов из предпочтений пользователя
        // - Найти книги этих жанров/авторов, которые не находятся в статусе FINISHED у пользователя
        // - Исключить книги, которые уже есть в его ReadingLog (FINISHED, READING, PLANNED)
        // - Отсортировать по среднему рейтингу
        // - Ограничить результат

        // Это сложная логика. Для начала можно просто вернуть топ книг по среднему рейтингу,
        // или книги по жанрам/авторам из предпочтений без учета ReadingLog.

        // Псевдокод:
        // List<UserPreference> userPrefs = userPreferenceService.findPreferencesByUserId(userId);
        // Set<Long> preferredGenreIds = userPrefs.stream().filter(p -> p.getItemType() == GENRE).map(p -> p.getItemId()).collect(Collectors.toSet());
        // Set<Long> preferredAuthorIds = userPrefs.stream().filter(p -> p.getItemType() == AUTHOR).map(p -> p.getItemId()).collect(Collectors.toSet());

        // List<String> finishedBookIds = readingLogService.findReadingLogsByUserIdAndStatus(userId, "FINISHED").stream().map(rl -> rl.getBook().getId()).collect(Collectors.toList());

        // List<Book> recommendedBooks = bookRepository.findBooksByGenreIdsAndAuthorIdsNotInList(preferredGenreIds, preferredAuthorIds, finishedBookIds, limit);

        // Пока что возвращаем топ по рейтингу как placeholder.
        // TODO: Реализовать полноценную логику рекомендаций.
        log.warn("Полноценная логика рекомендаций еще не реализована. Возвращаем топ по рейтингу.");
        return getTopRatedBooks(limit);
    }


    @Override
    @Transactional
    @CacheEvict(value = {"books", "book"}, allEntries = true)
    public void deleteBook(String id) { // Принимает String id
        log.debug("Удаление книги по ID: {}", id);
        if (!bookRepository.existsById(id)) { // Репозиторий должен принимать String id
            log.warn("Попытка удалить несуществующую книгу с ID: {}", id);
            throw new RuntimeException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id); // Репозиторий должен принимать String id
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
                    fillReviewStats(info, book.getId());
                    return info;
                })
                .collect(Collectors.toList());
    }

    // Используем метод из репозитория для получения среднего рейтинга
    private void fillReviewStats(BookInfo bookInfo, String bookId) {
        Long reviewCount = reviewRepository.countReviewsByBookId(bookId);
        bookInfo.setReviewCount(reviewCount);

        Double avgRating = reviewRepository.calculateAverageRatingForBook(bookId); // Новый метод
        bookInfo.setAverageRating(avgRating != null ? avgRating : 0.0); // Обработка случая, если отзывов нет
    }
}