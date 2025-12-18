package com.example.livelib.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table
public class Author extends BaseEntity {

    @Column(name = "full_name")
    private String fullName;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    public Author(String firstName, String lastName) {
        this.fullName = firstName + " " + lastName;
    }
}
