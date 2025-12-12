// src/main/java/com/example/livelib/services/impl/GenreServiceImpl.java
package com.example.livelib.services.impl;

import com.example.livelib.dto.create.GenreCreateDto;
import com.example.livelib.dto.showinfo.GenreInfo;
import com.example.livelib.models.entities.Genre;
import com.example.livelib.repos.GenreRepository;
import com.example.livelib.services.GenreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    @CacheEvict(value = "genres", allEntries = true)
    public void createGenre(GenreCreateDto genreCreateDto) {
        log.debug("Создание жанра: {}", genreCreateDto.getName());
        Genre genre = modelMapper.map(genreCreateDto, Genre.class);
        genreRepository.save(genre);
        log.info("Жанр создан: {}", genre.getName());
    }

    @Override
    @Cacheable(value = "genres", key = "'all'")
    public List<GenreInfo> findAllGenres() {
        log.debug("Получение списка всех жанров");
        List<Genre> genres = genreRepository.findAll();
        List<GenreInfo> genreInfos = genres.stream()
                .map(genre -> modelMapper.map(genre, GenreInfo.class))
                .collect(Collectors.toList());
        log.info("Найдено жанров: {}", genreInfos.size());
        return genreInfos;
    }

    @Override
    public GenreInfo findGenreById(String id) {
        log.debug("Поиск жанра по ID: {}", id);
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Жанр не найден по ID: {}", id);
                    return new RuntimeException("Genre not found with id: " + id);
                });
        return modelMapper.map(genre, GenreInfo.class);
    }

    @Override
    public GenreInfo findGenreByName(String name) {
        log.debug("Поиск жанра по имени: {}", name);
        return genreRepository.findByName(name)
                .map(genre -> modelMapper.map(genre, GenreInfo.class))
                .orElse(null); // или выбросить исключение
    }

    @Override
    @Transactional
    @CacheEvict(value = "genres", allEntries = true)
    public void deleteGenre(String id) {
        log.debug("Удаление жанра по ID: {}", id);
        if (!genreRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующий жанр с ID: {}", id);
            throw new RuntimeException("Genre not found with id: " + id);
        }
        genreRepository.deleteById(id);
        log.info("Жанр удален, ID: {}", id);
    }
}