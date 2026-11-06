package com.example.schedy.repository;

import com.example.schedy.entity.Mahasiswa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MahasiswaRepository extends JpaRepository<Mahasiswa, String> {

    // cari data mahasiswa dari user yang login
    Mahasiswa findByUser_IdUser(Integer idUser);

}

