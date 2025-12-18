package com.example.livelib.dto.showinfo;

import lombok.Data;

import java.util.List;

@Data
public class BookInfo {
    private String id;
    private String title;
    private String description;
    private String isbn;
    private Integer publicationYear;
    private AuthorInfo author;
    private List<GenreInfo> genres;
    private Double averageRating;
    private Long reviewCount;
}