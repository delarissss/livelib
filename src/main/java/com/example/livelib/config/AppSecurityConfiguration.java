// src/main/java/com/example/livelib/config/AppSecurityConfiguration.java
package com.example.livelib.config;

import com.example.livelib.models.enums.UserRoles;
import com.example.livelib.security.AppUserDetailsService;
import com.example.livelib.repos.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AppSecurityConfiguration {

    private final UserRepository userRepository;

    @Autowired
    public AppSecurityConfiguration(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("AppSecurityConfiguration инициализирована");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Разрешаем доступ к статическим ресурсам и общедоступным страницам
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/users/login", "/users/register", "/users/login-error", "/books/search", "/books/details/**").permitAll() // Главная, поиск книг, детали книги без авторизации
                        // Разрешаем доступ к профилю и функциям читательского дневника только для авторизованных пользователей
                        .requestMatchers("/profile", "/reading-log/**", "/reviews/add", "/user-preferences/**", "/export/**").authenticated()
                        // Разрешаем доступ к админским функциям только для роли ADMIN
                        .requestMatchers("/admin/reviews", "/admin/reviews/moderate/**", "/admin/users/**").hasRole(UserRoles.ADMIN.name())
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/users/login") // Страница логина
                        .usernameParameter("email") // Используем email как имя пользователя
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true) // Перенаправление после успешного входа
                        .failureUrl("/users/login?error=true") // Перенаправление при ошибке
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .key("uniqueAndSecretKeyForLivelib") // Уникальный ключ для "Запомнить меня"
                        .tokenValiditySeconds(86400 * 7) // 7 дней
                        .userDetailsService(userDetailsService()) // Используем наш сервис для загрузки UserDetails
                )
                .logout(logout -> logout
                        .logoutUrl("/users/logout") // URL для выхода
                        .logoutSuccessUrl("/") // Перенаправление после выхода
                        .invalidateHttpSession(true) // Инвалидация сессии
                        .deleteCookies("JSESSIONID", "remember-me") // Удаление кук
                        .permitAll()
                );

        log.info("SecurityFilterChain настроен");
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new AppUserDetailsService(userRepository);
    }
}