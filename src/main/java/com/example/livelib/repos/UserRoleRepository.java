package com.example.livelib.repos;

import com.example.livelib.models.entities.Role;
import com.example.livelib.models.enums.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findRoleByName(UserRoles role);
}
