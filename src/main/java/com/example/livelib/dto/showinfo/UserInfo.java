package com.example.livelib.dto.showinfo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserInfo {
    private String id;
    private String username;
    private String email;
    private List<String> roles; // Список названий ролей (USER, ADMIN)
}
