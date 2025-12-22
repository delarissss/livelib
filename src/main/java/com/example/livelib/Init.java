package com.example.livelib;

import com.example.livelib.models.entities.*;
import com.example.livelib.models.enums.UserRoles;
import com.example.livelib.repos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class Init implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;

    private final String defaultPassword;

    public Init(UserRepository userRepository,
                UserRoleRepository userRoleRepository,
                PasswordEncoder passwordEncoder,
                @Value("${app.default.password}") String defaultPassword,
                AuthorRepository authorRepository,
                GenreRepository genreRepository,
                BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.defaultPassword = defaultPassword;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.bookRepository = bookRepository;
        log.info("Init компонент инициализирован");
    }

    @Override
    public void run(String... args) {
        log.info("Запуск инициализации начальных данных");
        initRoles();
        initUsers();
        initGenres();
        initAuthorsAndBooks();
        log.info("Инициализация начальных данных завершена");
    }

    private void initRoles() {
        if (userRoleRepository.count() == 0) {
            log.info("Создание базовых ролей...");
            userRoleRepository.saveAll(List.of(
                    new Role(UserRoles.ADMIN),
                    new Role(UserRoles.MODERATOR),
                    new Role(UserRoles.USER)
            ));
            log.info("Роли созданы: ADMIN, MODERATOR, USER");
        } else {
            log.debug("Роли уже существуют, пропуск инициализации");
        }
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            log.info("Создание пользователей по умолчанию...");
            initAdmin();
            initModerator();
            initNormalUser();
            log.info("Пользователи по умолчанию созданы");
        } else {
            log.debug("Пользователи уже существуют, пропуск инициализации");
        }
    }

    private void initAdmin() {
        var adminRole = userRoleRepository.findRoleByName(UserRoles.ADMIN).orElseThrow();
        var adminUser = new User("admin", "admin@example.com", passwordEncoder.encode(defaultPassword));
        adminUser.setRoles(List.of(adminRole));
        userRepository.save(adminUser);
        log.info("Создан администратор: admin");
    }

    private void initModerator() {
        var moderatorRole = userRoleRepository.findRoleByName(UserRoles.MODERATOR).orElseThrow();
        var moderatorUser = new User("moderator", "moderator@example.com", passwordEncoder.encode(defaultPassword));
        moderatorUser.setRoles(List.of(moderatorRole));
        userRepository.save(moderatorUser);
        log.info("Создан модератор: moderator");
    }

    private void initNormalUser() {
        var userRole = userRoleRepository.findRoleByName(UserRoles.USER).orElseThrow();
        var normalUser = new User("user", "user@example.com", passwordEncoder.encode(defaultPassword));
        normalUser.setRoles(List.of(userRole));
        userRepository.save(normalUser);
        log.info("Создан обычный пользователь: user");
    }

    private void initGenres() {
        List<String> genreNames = Arrays.asList(
                "Роман", "Фантастика", "Историческая проза", "Антиутопия",
                "Приключения", "Философская проза", "Драма", "Эпопея"
        );

        for (String name : genreNames) {
            if (!genreRepository.findByName(name).isPresent()) {
                genreRepository.save(new Genre(name));
                log.debug("Создан жанр: {}", name);
            }
        }
    }

    private void initAuthorsAndBooks() {
        if (bookRepository.count() > 0) {
            log.debug("Книги уже существуют, пропуск инициализации книг");
            return;
        }

        // Создаём/получаем авторов
        Map<String, Author> authors = new HashMap<>();
        List<String> authorNames = Arrays.asList(
                "Михаил Шолохов",
                "Леонид Андреев",
                "Патрик Несс",
                "Рэй Брэдбери",
                "Аарон Манке",
                "Р. Баркер",
                "Джозеф Хеллер",
                "Сюсаку Эндо"
        );

        for (String fullName : authorNames) {
            Optional<Author> existing = authorRepository.findByFullName(fullName);
            Author author = existing.orElseGet(() -> {
                Author a = new Author(fullName);
                return authorRepository.save(a);
            });
            authors.put(fullName, author);
            log.debug("Автор инициализирован: {}", fullName);
        }

        // Создаём книги
        List<BookData> books = Arrays.asList(
                new BookData("Тихий Дон", "Михаил Шолохов", "9785171001262", "1928",
                        "Эпопея о жизни донских казаков в годы Первой мировой войны, Гражданской войны и революции.",
                        Set.of("Эпопея", "Историческая проза", "Роман")),

                new BookData("Иуда Искариот", "Леонид Андреев", "9785170825489", "1907",
                        "Философская новелла, переосмысливающая образ Иуды как трагического искателя истины.",
                        Set.of("Философская проза", "Драма")),

                new BookData("Голос монстра", "Патрик Несс", "9785171145819", "2011",
                        "История подростка, столкнувшегося с внутренними демонами в образе монстра из кошмара.",
                        Set.of("Роман", "Драма", "Фантастика")),

                new BookData("451° по Фаренгейту", "Рэй Брэдбери", "9785171163738", "1953",
                        "Антиутопия о мире, где книги запрещены, а пожарные сжигают их вместо тушения огня.",
                        Set.of("Антиутопия", "Фантастика", "Философская проза")),

                new BookData("Мир легенд о свирепых монстрах", "Аарон Манке", "9785041165210", "2020",
                        "Путеводитель по мифическим существам со всего мира: от славянских до африканских легенд.",
                        Set.of("Приключения", "Фантастика")),

                new BookData("Костяные корабли", "Р. Дж. Баркер", "9785171260198", "2018",
                        "Фэнтези-роман о мире, где корабли строят из костей драконов, а магия — часть повседневности.",
                        Set.of("Фантастика", "Приключения")),

                new BookData("Поправка-22", "Джозеф Хеллер", "9785171088184", "1961",
                        "Сатирический антироман о абсурдности войны и бюрократии через историю пилота Второй мировой.",
                        Set.of("Роман", "Антиутопия", "Сатира")),

                new BookData("Самурай", "Сюсаку Эндо", "9785041079289", "1980",
                        "Исторический роман о японском самурае, отправившемся в Европу, и его духовных исканиях.",
                        Set.of("Историческая проза", "Роман", "Философская проза"))
        );

        for (BookData data : books) {
            Author author = authors.get(data.author);
            Set<Genre> genres = new HashSet<>();
            for (String genreName : data.genres) {
                Genre genre = genreRepository.findByName(genreName)
                        .orElseThrow(() -> new RuntimeException("Жанр не найден: " + genreName));
                genres.add(genre);
            }

            Book book = new Book();
            book.setTitle(data.title);
            book.setDescription(data.description);
            book.setIsbn(data.isbn);
            book.setPublicationYear(data.year);
            book.setAuthor(author);
            book.setGenres(genres);

            bookRepository.save(book);
            log.info("Книга добавлена: {} – {}", data.title, data.author);
        }
    }

    // Вспомогательный класс для удобства
    private static class BookData {
        String title;
        String author;
        String isbn;
        String year;
        String description;
        Set<String> genres;

        BookData(String title, String author, String isbn, String year, String description, Set<String> genres) {
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.year = year;
            this.description = description;
            this.genres = genres;
        }
    }
}