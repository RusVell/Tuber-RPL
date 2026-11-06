package com.example.schedy.repository;

import com.example.schedy.entity.Kelas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KelasRepository extends JpaRepository<Kelas, Integer> {

    // semua kelas yang dibuka di semester tertentu
    List<Kelas> findBySemester_IdSemester(Integer idSemester);

}

