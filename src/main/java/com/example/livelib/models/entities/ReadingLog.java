package com.example.livelib.models.entities;

import com.example.livelib.models.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "reading_log")
public class ReadingLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(columnDefinition = "integer check (rating >= 1 and rating <= 10)")
    private Integer rating;

    @Column(name = "note")
    private String note;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.PLANNED; // ENUM: PLANNED, READING, FINISHED

    @Column(name = "date_finished")
    private LocalDateTime dateFinished;

    public ReadingLog() {
    }

    public ReadingLog(User user, Book book) {
        this.user = user;
        this.book = book;
    }
}