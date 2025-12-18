package com.example.livelib.dto.showinfo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RLInfo {
    private String id;
    private UserInfo user;
    private BookInfo book;
    private Integer rating;
    private String note;
    private String status; // PLANNED, READING, FINISHED
    private LocalDateTime dateFinished;
}