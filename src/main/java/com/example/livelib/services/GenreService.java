// src/main/java/com/example/livelib/services/GenreService.java
package com.example.livelib.services;

import com.example.livelib.dto.create.GenreCreateDto;
import com.example.livelib.dto.showinfo.GenreInfo;

import java.util.List;

public interface GenreService {
    void createGenre(GenreCreateDto genreCreateDto);
    List<GenreInfo> findAllGenres();
    GenreInfo findGenreById(String id);
    GenreInfo findGenreByName(String name);
    void deleteGenre(String id);
}