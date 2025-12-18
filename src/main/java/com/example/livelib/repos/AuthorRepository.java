package com.example.livelib.repos;

import com.example.livelib.models.entities.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, String> { // Используем String для UUID
    Optional<Author> findByFullName(String fullName);
}