package com.example.livelib.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table
public class Author extends BaseEntity {

    @Column(name = "full_name")
    private String fullName;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    public Author() {
    }

    public Author(String firstName, String lastName) {
        this.fullName = firstName + " " + lastName;
    }
}
