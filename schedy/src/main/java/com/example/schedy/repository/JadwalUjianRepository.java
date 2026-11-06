package com.example.schedy.repository;

import com.example.schedy.entity.JadwalUjian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JadwalUjianRepository extends JpaRepository<JadwalUjian, Integer> {

    List<JadwalUjian> findByKelas_IdKelasIn(List<Integer> idKelas);

    List<JadwalUjian> findByKelas_IdKelas(Integer idKelas);

}
