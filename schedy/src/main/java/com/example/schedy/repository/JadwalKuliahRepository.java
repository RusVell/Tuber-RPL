package com.example.schedy.repository;

import com.example.schedy.entity.JadwalKuliah;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JadwalKuliahRepository extends JpaRepository<JadwalKuliah, Integer> {

    List<JadwalKuliah> findByKelas_IdKelasIn(List<Integer> idKelas);

    List<JadwalKuliah> findByKelas_IdKelas(Integer idKelas);

}
