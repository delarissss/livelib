// src/main/java/com/example/livelib/security/AppUserDetailsService.java
package com.example.livelib.security;

import com.example.livelib.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Загрузка userDetails для пользователя: {}", username);
        return userRepository.findByUsername(username)
                .map(u -> new User(
                        u.getUsername(),
                        u.getPassword(),
                        u.getRoles().stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name())) // ROLE_USER, ROLE_ADMIN
                                .collect(Collectors.toList())
                ))
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден при загрузке userDetails: {}", username);
                    return new UsernameNotFoundException("User " + username + " was not found!");
                });
    }
}