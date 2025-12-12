package com.example.livelib.dto.showinfo;

import lombok.Data;

@Data
public class GenreInfo {

    private String id;
    private String name;

    public GenreInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }
}