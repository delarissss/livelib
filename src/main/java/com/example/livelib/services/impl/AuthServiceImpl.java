
package com.example.livelib.services.impl;

import com.example.livelib.dto.create.UserRegistrationDto;
import com.example.livelib.dto.showinfo.UserInfo;
import com.example.livelib.models.entities.Role;
import com.example.livelib.models.entities.User;
import com.example.livelib.models.enums.UserRoles;
import com.example.livelib.repos.UserRepository;
import com.example.livelib.repos.UserRoleRepository;
import com.example.livelib.services.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public AuthServiceImpl(UserRepository userRepository, UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public void register(UserRegistrationDto registrationDTO) {
        log.debug("Регистрация нового пользователя: {}", registrationDTO.getEmail());

        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            log.warn("Попытка регистрации с несовпадающими паролями для email: {}", registrationDTO.getEmail());
            throw new RuntimeException("Passwords do not match!");
        }

        if (userExists(registrationDTO.getEmail())) {
            log.warn("Попытка регистрации с уже существующим email: {}", registrationDTO.getEmail());
            throw new RuntimeException("Email is already in use!");
        }

        var userRole = userRoleRepository.findRoleByName(UserRoles.USER)
                .orElseThrow(() -> {
                    log.error("Роль USER не найдена в базе данных!");
                    return new RuntimeException("Default user role not found!");
                });

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRoles(List.of(userRole)); // Устанавливаем роль при регистрации

        userRepository.save(user);
        log.info("Пользователь успешно зарегистрирован: {}", registrationDTO.getEmail());
    }

    @Override
    public User getUser(String username) {
        log.debug("Получение пользователя по имени: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: {}", username);
                    return new UsernameNotFoundException("User " + username + " was not found!");
                });
    }

    @Override
    public boolean userExists(String email) {
        log.debug("Проверка существования пользователя по email: {}", email);
        return userRepository.findByEmail(email).isPresent();
    }

    // Метод для получения UserInfo, если потребуется
    public UserInfo getUserInfo(String username) {
        User user = getUser(username);
        return modelMapper.map(user, UserInfo.class);
    }
}