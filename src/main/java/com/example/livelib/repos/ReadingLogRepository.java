package com.example.livelib.repos;

import com.example.livelib.models.entities.ReadingLog;
import com.example.livelib.models.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingLogRepository extends JpaRepository<ReadingLog, String> {

    List<ReadingLog> findByUserId(String userId);

    Optional<ReadingLog> findByUserIdAndBookId(String userId, String bookId);

    List<ReadingLog> findByStatus(Status status);

    List<ReadingLog> findByUserIdAndStatus(String userId, Status status);

    List<ReadingLog> findByRatingGreaterThanEqual(Integer minRating);

}