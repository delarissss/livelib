
package com.example.livelib.services;

import com.example.livelib.dto.create.AuthorCreateDto;
import com.example.livelib.dto.showinfo.AuthorInfo;

import java.util.List;

public interface AuthorService {
    void createAuthor(AuthorCreateDto authorCreateDto);
    List<AuthorInfo> findAllAuthors();
    AuthorInfo findAuthorById(String id);
    AuthorInfo findAuthorByFullName(String fullName);
    void deleteAuthor(String id);
}