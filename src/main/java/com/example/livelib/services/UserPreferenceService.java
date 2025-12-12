// src/main/java/com/example/livelib/services/UserPreferenceService.java
package com.example.livelib.services;

import com.example.livelib.dto.create.UserPreferenceCreateDto;
import com.example.livelib.dto.showinfo.UserPrefInfo;

import java.util.List;

public interface UserPreferenceService {
    void createUserPreference(UserPreferenceCreateDto userPrefCreateDto);
    List<UserPrefInfo> findPreferencesByUserId(String userId);
    void deletePreferencesByUserId(String userId); // При удалении пользователя
    void deletePreferenceById(String id);
    List<UserPrefInfo> findUserPreferencesWithDetails(String userId); // Для отображения с названиями
}