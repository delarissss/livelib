// src/main/java/com/example/livelib/security/AppUserDetailsService.java
package com.example.livelib.services.impl;

import com.example.livelib.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        log.debug("Загрузка userDetails для идентификатора: {}", identifier);

        Optional<com.example.livelib.models.entities.User> userOpt = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier));

        return userOpt.map(u -> User.builder()
                        .username(u.getUsername())
                        .password(u.getPassword())
                        .authorities(u.getRoles().stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                                .collect(Collectors.toList()))
                        .build())
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден при загрузке userDetails: {}", identifier);
                    return new UsernameNotFoundException("User " + identifier + " was not found!");
                });
    }
}