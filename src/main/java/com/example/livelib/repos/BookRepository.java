package com.example.livelib.repos;

import com.example.livelib.models.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> { // Используем String для UUID
    List<Book> findByAuthorId(String authorId);
    List<Book> findByGenresId(String genreId);
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN b.author a LEFT JOIN b.genres g WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(a.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Book> searchByTitleOrAuthorOrGenre(@Param("searchTerm") String searchTerm);

    @Query(value = "SELECT b.* FROM books b " +
            "LEFT JOIN (SELECT book_id, AVG(rating) as avg_rating FROM reviews WHERE is_moderated = true GROUP BY book_id) r_avg ON b.id = r_avg.book_id " +
            "ORDER BY r_avg.avg_rating DESC NULLS LAST, b.created_at DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Book> findTopBooksByAverageRating(@Param("limit") int limit);

    boolean existsByIsbn(String isbn);
}