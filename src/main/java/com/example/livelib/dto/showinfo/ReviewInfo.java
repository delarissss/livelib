package com.example.livelib.dto.showinfo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewInfo {
    private String id;
    private UserInfo user;
    private BookInfo book;
    private String reviewText;
    private Integer rating;
    private Boolean isModerated;
    private LocalDateTime createdAt;
}