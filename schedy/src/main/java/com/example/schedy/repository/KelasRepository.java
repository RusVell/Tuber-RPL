package com.example.schedy.repository;

import com.example.schedy.entity.Kelas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KelasRepository extends JpaRepository<Kelas, Integer> {

    // semua kelas yang dibuka di semester tertentu
    List<Kelas> findBySemester_IdSemester(Integer idSemester);


    // Mengambil kelas yang diajar oleh dosen itu sendiri berdasakan semester yang dipilih
    List<Kelas> findByDosen_NidnAndSemester_idSemester(String nidn, Integer idSemester);
}

