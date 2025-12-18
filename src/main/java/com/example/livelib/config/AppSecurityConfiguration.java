package com.example.livelib.config;

import com.example.livelib.models.enums.UserRoles; // Убедитесь, что enum UserRoles существует
import com.example.livelib.repos.UserRepository; // Убедитесь, что репозиторий существует
import com.example.livelib.services.impl.AppUserDetailsService; // Убедитесь, что сервис существует
import lombok.RequiredArgsConstructor; // Импортируем RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Slf4j
@Configuration
@EnableWebSecurity // Включаем настройку безопасности
@RequiredArgsConstructor // Lombok: создает конструктор для final полей
public class AppSecurityConfiguration {

    // @RequiredArgsConstructor автоматически создаст конструктор для userRepository
    private final UserRepository userRepository;

    // Удалили старый конструктор, так как Lombok его создаст

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Позволяем доступ к статическим ресурсам, favicon, ошибкам
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/favicon.ico", "/error").permitAll()
                        // Открытые URL
                        .requestMatchers("/", "/users/login", "/users/register", "/users/login-error", "/books/search", "/books/details/**").permitAll()
                        // URL для администраторов
                        .requestMatchers("/books/add", "/reviews/admin/**").hasRole(UserRoles.ADMIN.name()) // Используем enum
                        // Требуется аутентификация
                        .requestMatchers("/users/profile", "/reading-log/**", "/reviews/add", "/user-preferences/**").authenticated()
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/users/login") // Страница входа
                        .usernameParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY) // Параметр имени пользователя (обычно 'username')
                        .passwordParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY) // Параметр пароля (обычно 'password')
                        .defaultSuccessUrl("/", true) // Куда перенаправить после успешного входа
                        .failureUrl("/users/login-error") // Куда перенаправить при ошибке входа
                        .permitAll() // Позволяем всем видеть страницу входа
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKeyForLivelib") // Уникальный ключ для "Запомнить меня"
                        .tokenValiditySeconds(86400 * 7) // 7 дней (в секундах)
                        .userDetailsService(userDetailsService()) // Используем наш сервис для загрузки UserDetails
                )
                .logout(logout -> logout
                        .logoutUrl("/users/logout") // URL для выхода
                        .logoutSuccessUrl("/") // Куда перенаправить после выхода
                        .invalidateHttpSession(true) // Инвалидация сессии
                        .deleteCookies("JSESSIONID", "remember-me") // Удаление куки
                        .permitAll() // Позволяем всем выполнить выход
                )
                // Настройка SecurityContextRepository
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository)
                )
                // Отключаем CSRF для упрощения (в production может потребоваться включить)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/actuator/**") // Игнорировать CSRF для actuator, если используется
                );

        log.info("SecurityFilterChain настроен");
        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Используем BCrypt для хеширования паролей
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Создаем бин UserDetailsService, передавая userRepository
        // Убедитесь, что AppUserDetailsService корректно реализован
        return new AppUserDetailsService(userRepository);
    }
}