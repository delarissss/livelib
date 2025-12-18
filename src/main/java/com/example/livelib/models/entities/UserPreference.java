package com.example.livelib.models.entities;

import com.example.livelib.models.enums.ItemType; // Изменено
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "user_preferences")
public class UserPreference extends BaseEntity {

    @Column(name = "item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Column(name = "item_id", nullable = false)
    private String itemId; // ID конкретного жанра, автора или книги

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserPreference() {
    }

    public UserPreference(User user, ItemType itemType, String itemId) {
        this.user = user;
        this.itemType = itemType;
        this.itemId = itemId;
    }
}