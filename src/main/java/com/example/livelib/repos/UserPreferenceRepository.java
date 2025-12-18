package com.example.livelib.repos;

import com.example.livelib.models.entities.UserPreference;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> { // Используем String для UUID
    List<UserPreference> findByUserId(String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserPreference up WHERE up.user.id = :userId")
    void deleteByUserId(@Param("userId") String userId);
}