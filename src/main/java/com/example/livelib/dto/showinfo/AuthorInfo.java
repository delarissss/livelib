package com.example.livelib.dto.showinfo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthorInfo {

    private String id;
    private String fullName;

    public AuthorInfo(String id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }
}