package com.example.livelib.repos;

import com.example.livelib.models.entities.Author;
import com.example.livelib.models.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> { // Используем String для UUID

    List<Book> findByAuthorId(Long authorId);
    List<Book> findByGenres_Id(Long genreId); // Поиск по жанру

    @Query("SELECT DISTINCT a FROM Author a JOIN a.books b WHERE b.publicationYear > :year")
    List<Author> findAuthorsWithBooksPublishedAfter(int year);
}
