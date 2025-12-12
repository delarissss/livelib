// src/main/java/com/example/livelib/services/AuthService.java
package com.example.livelib.services;

import com.example.livelib.dto.create.UserRegistrationDto;
import com.example.livelib.models.entities.User;

public interface AuthService {
    void register(UserRegistrationDto registrationDTO);
    User getUser(String username);
    boolean userExists(String email);
}