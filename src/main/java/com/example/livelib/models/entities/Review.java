package com.example.livelib.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "review_text")
    private String reviewText;

    @Column(columnDefinition = "integer check (rating >= 1 and rating <= 10)")
    private Integer rating;

    @Column(name = "is_moderated", columnDefinition = "boolean default false")
    private Boolean isModerated = false;

    public Review() {
    }

    public Review(User user, Book book, String reviewText) {
        this.user = user;
        this.book = book;
        this.reviewText = reviewText;
    }
}