package com.example.schedy.repository;

import com.example.schedy.entity.PengambilanKelas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PengambilanKelasRepository extends JpaRepository<PengambilanKelas, Integer> {

    // kelas yang diambil mahasiswa di semester tertentu
    List<PengambilanKelas> findByMahasiswa_NpmAndKelas_Semester_IdSemester(String npm, Integer idSemester);

    // cek biar ga dicatat 2x (REQ-16)
    boolean existsByMahasiswa_NpmAndKelas_IdKelas(String npm, Integer idKelas);

    PengambilanKelas findByMahasiswa_NpmAndKelas_IdKelas(String npm, Integer idKelas);

    // semua mahasiswa di 1 kelas (REQ-20)
    List<PengambilanKelas> findByKelas_IdKelas(Integer idKelas);

}

