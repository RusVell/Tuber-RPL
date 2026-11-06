package com.example.schedy.repository;

import com.example.schedy.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {

    // Untuk login
    Users findByUsernameAndPasswordHash(String username, String passwordHash);

}

