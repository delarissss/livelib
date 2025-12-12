package com.example.livelib.dto.showinfo;

import lombok.Data;

@Data
public class AuthorInfo {

    private String id;
    private String fullName;
    private String biography;

    public AuthorInfo(String id, String fullName, String biography) {
        this.id = id;
        this.fullName = fullName;
        this.biography = biography;
    }
}