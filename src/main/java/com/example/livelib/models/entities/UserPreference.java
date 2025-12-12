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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Column(name = "item_id", nullable = false)
    private Long itemId; // ID конкретного жанра, автора или книги


    public UserPreference() {
    }

    public UserPreference(User user, ItemType itemType, Long itemId) {
        this.user = user;
        this.itemType = itemType;
        this.itemId = itemId;
    }
}