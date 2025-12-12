// src/main/java/com/example/livelib/services/impl/ReadingLogServiceImpl.java
package com.example.livelib.services.impl;

import com.example.livelib.dto.create.ReadingLogCreateDto;
import com.example.livelib.dto.showinfo.RLInfo;
import com.example.livelib.models.entities.Book;
import com.example.livelib.models.entities.ReadingLog;
import com.example.livelib.models.entities.User;
import com.example.livelib.models.enums.Status;
import com.example.livelib.repos.BookRepository;
import com.example.livelib.repos.ReadingLogRepository;
import com.example.livelib.repos.UserRepository;
import com.example.livelib.services.ReadingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // <-- Аннотация Lombok для логирования
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReadingLogServiceImpl implements ReadingLogService {

    private final ReadingLogRepository readingLogRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    @CacheEvict(value = {"readingLogs", "book", "user"}, allEntries = true) // Очищаем кэш книги и пользователя
    public void createReadingLog(ReadingLogCreateDto readingLogCreateDto) {
        log.debug("Создание записи в читательском дневнике для пользователя ID: {}, книги ID: {}", readingLogCreateDto.getUserId(), readingLogCreateDto.getBookId());

        if (findReadingLogByUserIdAndBookId(readingLogCreateDto.getUserId(), readingLogCreateDto.getBookId()) != null) {
            log.warn("Запись в дневнике уже существует для пользователя {} и книги {}", readingLogCreateDto.getUserId(), readingLogCreateDto.getBookId());
            throw new RuntimeException("Reading log entry already exists for this user and book.");
        }

        User user = userRepository.findById(readingLogCreateDto.getUserId())
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден при создании записи в дневнике, ID: {}", readingLogCreateDto.getUserId());
                    return new RuntimeException("User not found with id: " + readingLogCreateDto.getUserId());
                });

        Book book = bookRepository.findById(readingLogCreateDto.getBookId())
                .orElseThrow(() -> {
                    log.warn("Книга не найдена при создании записи в дневнике, ID: {}", readingLogCreateDto.getBookId());
                    return new RuntimeException("Book not found with id: " + readingLogCreateDto.getBookId());
                });

        ReadingLog readingLog = new ReadingLog();
        readingLog.setUser(user);
        readingLog.setBook(book);
        readingLog.setRating(readingLogCreateDto.getRating());
        readingLog.setNote(readingLogCreateDto.getNote());
        if (readingLogCreateDto.getStatus() != null) {
            readingLog.setStatus(Status.valueOf(readingLogCreateDto.getStatus().toUpperCase()));
        } else {
            readingLog.setStatus(Status.PLANNED); // По умолчанию
        }
        if (readingLog.getStatus() == Status.FINISHED && readingLog.getDateFinished() == null) {
            readingLog.setDateFinished(LocalDateTime.now());
        }

        readingLogRepository.save(readingLog);
        log.info("Запись в читательском дневнике создана для пользователя '{}' и книги '{}'", user.getUsername(), book.getTitle());
    }

    @Override
    public List<RLInfo> findReadingLogsByUserId(String userId) {
        log.debug("Поиск записей в дневнике для пользователя ID: {}", userId);
        List<ReadingLog> readingLogEntries = readingLogRepository.findByUserId(userId);
        return readingLogEntries.stream()
                .map(entry -> modelMapper.map(entry, RLInfo.class)) // Используем 'entry' вместо 'log'
                .collect(Collectors.toList());
    }

    @Override
    public RLInfo findReadingLogById(String id) {
        log.debug("Поиск записи в дневнике по ID: {}", id);
        ReadingLog readingLogEntry = readingLogRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Запись в дневнике не найдена по ID: {}", id);
                    return new RuntimeException("Reading log not found with id: " + id);
                });
        return modelMapper.map(readingLogEntry, RLInfo.class);
    }

    @Override
    public RLInfo findReadingLogByUserIdAndBookId(String userId, String bookId) {
        log.debug("Поиск записи в дневнике для пользователя ID: {} и книги ID: {}", userId, bookId);
        return readingLogRepository.findByUserIdAndBookId(userId, bookId)
                .map(entry -> modelMapper.map(entry, RLInfo.class)) // Используем 'entry' вместо 'log'
                .orElse(null); // или выбросить исключение
    }

    @Override
    @Transactional
    @CacheEvict(value = {"readingLogs", "book", "user"}, allEntries = true) // Очищаем кэш книги и пользователя
    public void updateReadingLog(String id, ReadingLogCreateDto updatedData) {
        log.debug("Обновление записи в дневнике ID: {}", id);
        ReadingLog readingLogEntry = readingLogRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Запись в дневнике не найдена для обновления, ID: {}", id);
                    return new RuntimeException("Reading log not found with id: " + id);
                });

        // Обновляем поля, если они указаны в DTO
        if (updatedData.getRating() != null) readingLogEntry.setRating(updatedData.getRating());
        if (updatedData.getNote() != null) readingLogEntry.setNote(updatedData.getNote());
        if (updatedData.getStatus() != null) {
            Status newStatus = Status.valueOf(updatedData.getStatus().toUpperCase());
            readingLogEntry.setStatus(newStatus);
            // Если статус стал FINISHED и дата не установлена, устанавливаем текущую
            if (newStatus == Status.FINISHED && readingLogEntry.getDateFinished() == null) {
                readingLogEntry.setDateFinished(LocalDateTime.now());
            } else if (newStatus != Status.FINISHED) {
                readingLogEntry.setDateFinished(null); // Сбрасываем дату, если статус не FINISHED
            }
        }

        readingLogRepository.save(readingLogEntry);
        log.info("Запись в читательском дневнике обновлена, ID: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"readingLogs", "book", "user"}, allEntries = true) // Очищаем кэш книги и пользователя
    public void deleteReadingLog(String id) {
        log.debug("Удаление записи в дневнике по ID: {}", id);
        if (!readingLogRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующую запись в дневнике с ID: {}", id);
            throw new RuntimeException("Reading log not found with id: " + id);
        }
        readingLogRepository.deleteById(id);
        log.info("Запись в читательском дневнике удалена, ID: {}", id);
    }

    @Override
    public List<RLInfo> findReadingLogsByStatus(String statusStr) {
        log.debug("Поиск записей в дневнике по статусу: {}", statusStr);
        Status status = Status.valueOf(statusStr.toUpperCase());
        List<ReadingLog> readingLogEntries = readingLogRepository.findByStatus(status);
        return readingLogEntries.stream()
                .map(entry -> modelMapper.map(entry, RLInfo.class)) // Используем 'entry' вместо 'log'
                .collect(Collectors.toList());
    }

    @Override
    public List<RLInfo> findReadingLogsByUserIdAndStatus(String userId, String statusStr) {
        log.debug("Поиск записей в дневнике для пользователя ID: {} и статуса: {}", userId, statusStr);
        Status status = Status.valueOf(statusStr.toUpperCase());
        List<ReadingLog> readingLogEntries = readingLogRepository.findByUserIdAndStatus(userId, status);
        return readingLogEntries.stream()
                .map(entry -> modelMapper.map(entry, RLInfo.class)) // Используем 'entry' вместо 'log'
                .collect(Collectors.toList());
    }

    @Override
    public List<RLInfo> findReadingLogsByRatingGreaterThanEqual(String userId, Integer minRating) {
        log.debug("Поиск записей в дневнике пользователя ID: {} с оценкой >= {}", userId, minRating);
        List<ReadingLog> readingLogEntries = readingLogRepository.findByUserId(userId).stream()
                .filter(entry -> entry.getRating() != null && entry.getRating() >= minRating) // Используем 'entry' вместо 'log'
                .collect(Collectors.toList());
        return readingLogEntries.stream()
                .map(entry -> modelMapper.map(entry, RLInfo.class)) // Используем 'entry' вместо 'log'
                .collect(Collectors.toList());
    }
}