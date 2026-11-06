package com.example.schedy.repository;

import com.example.schedy.entity.Dosen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DosenRepository extends JpaRepository<Dosen, String> {

    Dosen findByUser_IdUser(Integer idUser);

}

