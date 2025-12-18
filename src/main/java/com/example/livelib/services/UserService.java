
package com.example.livelib.services;

import com.example.livelib.dto.showinfo.UserInfo;

public interface UserService {
    UserInfo findUserById(String id);
    UserInfo findUserByEmail(String email);
    UserInfo findUserByUsername(String username);
    void deleteUser(String id); // Удаление пользователя и связанных данных
}