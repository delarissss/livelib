package com.example.livelib.repos;

import com.example.livelib.models.entities.ReadingLog;
import com.example.livelib.models.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingLogRepository extends JpaRepository<ReadingLog, String> { // Используем String для UUID
    List<ReadingLog> findByUserId(String userId);
    Optional<ReadingLog> findByUserIdAndBookId(String userId, String bookId);
    List<ReadingLog> findByStatus(Status status);
    List<ReadingLog> findByUserIdAndStatus(String userId, Status status);
    @Query("SELECT r FROM ReadingLog r WHERE r.user.id = :userId AND r.rating >= :minRating")
    List<ReadingLog> findByUserIdAndRatingGreaterThanEqual(@Param("userId") String userId, @Param("minRating") Integer minRating);
}