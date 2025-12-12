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
    private String coverUrl;
    private AuthorInfo author;
    private List<GenreInfo> genres;

    public BookInfo(String id, String title, String description, String isbn, Integer publicationYear, String coverUrl,
                        AuthorInfo author, List<GenreInfo> genres) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.coverUrl = coverUrl;
        this.author = author;
        this.genres = genres;
    }
}
