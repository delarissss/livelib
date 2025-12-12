// src/main/java/com/example/livelib/services/impl/AuthorServiceImpl.java
package com.example.livelib.services.impl;

import com.example.livelib.dto.create.AuthorCreateDto;
import com.example.livelib.dto.showinfo.AuthorInfo;
import com.example.livelib.models.entities.Author;
import com.example.livelib.repos.AuthorRepository;
import com.example.livelib.services.AuthorService;
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
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    @CacheEvict(value = "authors", allEntries = true)
    public void createAuthor(AuthorCreateDto authorCreateDto) {
        log.debug("Создание автора: {}", authorCreateDto.getFullName());
        Author author = modelMapper.map(authorCreateDto, Author.class);
        authorRepository.save(author);
        log.info("Автор создан: {}", author.getFullName());
    }

    @Override
    @Cacheable(value = "authors", key = "'all'")
    public List<AuthorInfo> findAllAuthors() {
        log.debug("Получение списка всех авторов");
        List<Author> authors = authorRepository.findAll();
        List<AuthorInfo> authorInfos = authors.stream()
                .map(author -> modelMapper.map(author, AuthorInfo.class))
                .collect(Collectors.toList());
        log.info("Найдено авторов: {}", authorInfos.size());
        return authorInfos;
    }

    @Override
    public AuthorInfo findAuthorById(String id) {
        log.debug("Поиск автора по ID: {}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Автор не найден по ID: {}", id);
                    return new RuntimeException("Author not found with id: " + id);
                });
        return modelMapper.map(author, AuthorInfo.class);
    }

    @Override
    public AuthorInfo findAuthorByFullName(String fullName) {
        log.debug("Поиск автора по имени: {}", fullName);
        return authorRepository.findByFullName(fullName)
                .map(author -> modelMapper.map(author, AuthorInfo.class))
                .orElse(null); // или выбросить исключение, если всегда должен быть найден
    }

    @Override
    @Transactional
    @CacheEvict(value = "authors", allEntries = true)
    public void deleteAuthor(String id) {
        log.debug("Удаление автора по ID: {}", id);
        if (!authorRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующего автора с ID: {}", id);
            throw new RuntimeException("Author not found with id: " + id);
        }
        authorRepository.deleteById(id);
        log.info("Автор удален, ID: {}", id);
    }
}