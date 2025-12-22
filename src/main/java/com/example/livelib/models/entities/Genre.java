package com.example.livelib.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "genres")
public class Genre extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    public Genre(String name) {
        this.name = name;
    }
}
