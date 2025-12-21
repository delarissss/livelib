package com.example.livelib.config;

import com.example.livelib.models.enums.UserRoles;
import com.example.livelib.repos.UserRepository;
import com.example.livelib.services.impl.AppUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;


@Slf4j
@Configuration
public class AppSecurityConfiguration {

    private final UserRepository userRepository;

    public AppSecurityConfiguration(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("AppSecurityConfiguration инициализирована");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/favicon.ico", "/error").permitAll()
                        .requestMatchers("/", "/users/login", "/users/register", "/users/login-error", "/books/search", "/books/details/**").permitAll()
                        .requestMatchers("/books/add", "/reviews/admin/**").hasRole(UserRoles.ADMIN.name())
                        .requestMatchers("/users/profile", "/reading-log/**", "/reviews/add", "/user-preferences/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/users/login-error")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecretKey")
                        .tokenValiditySeconds(86400 * 7)
                        .userDetailsService(userDetailsService())
                        .rememberMeParameter("remember-me")
                )
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository)
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/actuator/**")
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
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new AppUserDetailsService(userRepository);
    }
}