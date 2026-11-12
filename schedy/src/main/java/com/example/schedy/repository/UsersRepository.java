package com.example.schedy.repository;

import com.example.schedy.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
    boolean existsByUsername(String username);
    Users findByUsername(String username);

    // Untuk login
    Users findByUsernameAndPasswordHash(String username, String passwordHash);

}

