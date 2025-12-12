package com.example.livelib.repos;

import com.example.livelib.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> { // Используем String для UUID

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
}