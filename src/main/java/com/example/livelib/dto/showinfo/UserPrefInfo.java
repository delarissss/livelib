package com.example.livelib.dto.showinfo;

import lombok.Data;

import com.example.livelib.models.enums.ItemType;

@Data
public class UserPrefInfo {

    private String id;
    private UserInfo user;
    private ItemType itemType;
    private String itemId;
    private String itemName; // Название жанра/автора/книги (для удобства отображения)

}