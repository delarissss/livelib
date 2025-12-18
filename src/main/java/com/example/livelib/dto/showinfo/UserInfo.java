package com.example.livelib.dto.showinfo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class UserInfo {
    private String id;
    private String username;
    private String email;
    private List<String> roles;
}
