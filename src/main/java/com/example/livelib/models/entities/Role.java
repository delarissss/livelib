package com.example.livelib.models.entities;

import com.example.livelib.models.enums.UserRoles;
import jakarta.persistence.*;
import lombok.Setter;

@Setter

@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
    private UserRoles name;

    public Role(UserRoles name) {
        this.name = name;
    }

    public Role() {
    }

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    public UserRoles getName() {
        return name;
    }
}