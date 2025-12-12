// src/main/java/com/example/livelib/services/impl/UserServiceImpl.java
package com.example.livelib.services.impl;

import com.example.livelib.dto.showinfo.UserInfo;
import com.example.livelib.models.entities.User;
import com.example.livelib.repos.UserRepository;
import com.example.livelib.services.ReadingLogService;
import com.example.livelib.services.ReviewService;
import com.example.livelib.services.UserPreferenceService;
import com.example.livelib.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final ReadingLogService readingLogService;
    private final UserPreferenceService userPreferenceService;
    private final ModelMapper modelMapper;

    @Override
    @Cacheable(value = "user", key = "#id")
    public UserInfo findUserById(String id) {
        log.debug("Поиск пользователя по ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден по ID: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });
        return modelMapper.map(user, UserInfo.class);
    }

    @Override
    public UserInfo findUserByEmail(String email) {
        log.debug("Поиск пользователя по email: {}", email);
        return userRepository.findByEmail(email)
                .map(user -> modelMapper.map(user, UserInfo.class))
                .orElse(null); // или выбросить исключение
    }

    @Override
    public UserInfo findUserByUsername(String username) {
        log.debug("Поиск пользователя по username: {}", username);
        return userRepository.findByUsername(username)
                .map(user -> modelMapper.map(user, UserInfo.class))
                .orElse(null); // или выбросить исключение
    }

    @Override
    @Transactional
    @CacheEvict(value = "user", key = "#id")
    public void deleteUser(String id) {
        log.debug("Удаление пользователя по ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка удалить несуществующего пользователя с ID: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });

        // Удаляем связанные данные
        reviewService.deleteReviewsByUserId(id);
        readingLogService.findReadingLogsByUserId(id).forEach(log -> readingLogService.deleteReadingLog(log.getId())); // Удаляем все записи в дневнике
        userPreferenceService.deletePreferencesByUserId(id);

        userRepository.deleteById(id);
        log.info("Пользователь удален, ID: {}", id);
    }
}