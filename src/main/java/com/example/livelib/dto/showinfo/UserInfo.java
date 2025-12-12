package com.example.livelib.dto.showinfo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserInfo {

    private String id;
    private String username;
    private String email;
    private LocalDateTime registrationDate;
    private Boolean isAdmin;
    private List<String> roles;

    public UserInfo(String id, String username, String email, LocalDateTime registrationDate, Boolean isAdmin, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.registrationDate = registrationDate;
        this.isAdmin = isAdmin;
        this.roles = roles;
    }
}
