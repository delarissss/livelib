// src/main/java/com/example/livelib/services/ReadingLogService.java
package com.example.livelib.services;

import com.example.livelib.dto.create.ReadingLogCreateDto;
import com.example.livelib.dto.showinfo.RLInfo;

import java.util.List;

public interface ReadingLogService {
    void createReadingLog(ReadingLogCreateDto readingLogCreateDto);
    List<RLInfo> findReadingLogsByUserId(String userId);
    RLInfo findReadingLogById(String id);
    RLInfo findReadingLogByUserIdAndBookId(String userId, String bookId); // Для проверки существования
    void updateReadingLog(String id, ReadingLogCreateDto updatedData); // Обновление статуса, оценки, заметки
    void deleteReadingLog(String id);
    List<RLInfo> findReadingLogsByStatus(String status); // Например, FINISHED
    List<RLInfo> findReadingLogsByUserIdAndStatus(String userId, String status);
    List<RLInfo> findReadingLogsByRatingGreaterThanEqual(String userId, Integer minRating);
}