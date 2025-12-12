package com.example.livelib.dto.showinfo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BookInfo {
    private String id;
    private String title;
    private String description;
    private String isbn;
    private Integer publicationYear;
    private AuthorInfo author;
    private List<GenreInfo> genres;
    private Long reviewCount; // Количество модерированных отзывов
    private Double averageRating; // Средний рейтинг книги
}