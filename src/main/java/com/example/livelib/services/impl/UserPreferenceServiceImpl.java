// src/main/java/com/example/livelib/services/impl/UserPreferenceServiceImpl.java
package com.example.livelib.services.impl;

import com.example.livelib.dto.create.UserPreferenceCreateDto;
import com.example.livelib.dto.showinfo.UserPrefInfo;
import com.example.livelib.models.entities.*;
import com.example.livelib.models.enums.ItemType;
import com.example.livelib.repos.*;
import com.example.livelib.services.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    @CacheEvict(value = "userPrefs", key = "#userPrefCreateDto.userId") // Очищаем кэш для конкретного юзера
    public void createUserPreference(UserPreferenceCreateDto userPrefCreateDto) {
        log.debug("Создание пользовательского предпочтения для пользователя ID: {}", userPrefCreateDto.getUserId());

        User user = userRepository.findById(userPrefCreateDto.getUserId())
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден при создании предпочтения, ID: {}", userPrefCreateDto.getUserId());
                    return new RuntimeException("User not found with id: " + userPrefCreateDto.getUserId());
                });

        ItemType itemType = ItemType.valueOf(userPrefCreateDto.getItemType().toUpperCase());

        // Проверяем, существует ли целевой объект (жанр, автор, книга)
        boolean itemExists = validateItemExists(itemType, userPrefCreateDto.getItemId());
        if (!itemExists) {
            log.warn("Целевой элемент ({} ID: {}) для предпочтения не существует.", userPrefCreateDto.getItemType(), userPrefCreateDto.getItemId());
            throw new RuntimeException("Target item for preference does not exist.");
        }

        UserPreference userPref = new UserPreference();
        userPref.setUser(user);
        userPref.setItemType(itemType);
        userPref.setItemId(userPrefCreateDto.getItemId());

        userPreferenceRepository.save(userPref);
        log.info("Предпочтение добавлено для пользователя '{}' и элемента типа '{}' с ID {}", user.getUsername(), itemType, userPrefCreateDto.getItemId());
    }

    @Override
    public List<UserPrefInfo> findPreferencesByUserId(String userId) {
        log.debug("Поиск предпочтений для пользователя ID: {}", userId);
        List<UserPreference> prefs = userPreferenceRepository.findByUserId(userId);
        return prefs.stream()
                .map(pref -> modelMapper.map(pref, UserPrefInfo.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "userPrefs", key = "#userId")
    public void deletePreferencesByUserId(String userId) {
        log.debug("Удаление всех предпочтений пользователя ID: {}", userId);
        userPreferenceRepository.deleteByUserId(userId);
        log.info("Все предпочтения пользователя ID {} удалены", userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userPrefs", "user"}, key = "#p0.substring(0, p0.lastIndexOf('-'))") // Грубый хак для очистки кэша юзера из ID привычки
    public void deletePreferenceById(String id) {
        log.debug("Удаление предпочтения по ID: {}", id);
        if (!userPreferenceRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующее предпочтение с ID: {}", id);
            throw new RuntimeException("User preference not found with id: " + id);
        }
        userPreferenceRepository.deleteById(id);
        log.info("Предпочтение удалено, ID: {}", id);
    }

    @Override
    public List<UserPrefInfo> findUserPreferencesWithDetails(String userId) {
        log.debug("Поиск предпочтений с деталями для пользователя ID: {}", userId);
        List<UserPreference> prefs = userPreferenceRepository.findByUserId(userId);

        return prefs.stream()
                .map(pref -> {
                    UserPrefInfo info = modelMapper.map(pref, UserPrefInfo.class);
                    // Заполняем itemName вручную
                    String itemName = getItemNameByIdAndType(pref.getItemId(), pref.getItemType());
                    info.setItemName(itemName);
                    return info;
                })
                .collect(Collectors.toList());
    }

    // --- Вспомогательные методы ---

    private boolean validateItemExists(ItemType type, Long itemId) {
        switch (type) {
            case GENRE:
                return genreRepository.existsById(String.valueOf(itemId));
            case AUTHOR:
                return authorRepository.existsById(String.valueOf(itemId));
            case BOOK:
                return bookRepository.existsById(String.valueOf(itemId));
            default:
                return false;
        }
    }

    private String getItemNameByIdAndType(Long itemId, ItemType type) {
        switch (type) {
            case GENRE:
                return genreRepository.findById(String.valueOf(itemId)).map(Genre::getName).orElse("Unknown Genre");
            case AUTHOR:
                return authorRepository.findById(String.valueOf(itemId)).map(Author::getFullName).orElse("Unknown Author");
            case BOOK:
                return bookRepository.findById(String.valueOf(itemId)).map(Book::getTitle).orElse("Unknown Book");
            default:
                return "Unknown Item";
        }
    }
}