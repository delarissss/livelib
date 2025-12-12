package com.example.livelib.dto.showinfo;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.livelib.models.enums.ItemType;

@Data
@NoArgsConstructor
public class UserPrefInfo {
    private String id;
    private UserInfo user;
    private ItemType itemType;
    private Long itemId;
    private String itemName; // Название жанра/автора/книги (для удобства отображения)
}