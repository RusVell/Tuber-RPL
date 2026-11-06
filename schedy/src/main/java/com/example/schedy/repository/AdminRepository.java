package com.example.schedy.repository;

import com.example.schedy.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Admin findByUser_IdUser(Integer idUser);

}

