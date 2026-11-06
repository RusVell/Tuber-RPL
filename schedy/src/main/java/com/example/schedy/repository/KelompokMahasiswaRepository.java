package com.example.schedy.repository;

import com.example.schedy.entity.KelompokMahasiswa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KelompokMahasiswaRepository extends JpaRepository<KelompokMahasiswa, Integer> {

    // kelompok yang dibuat dosen di 1 kelas
    List<KelompokMahasiswa> findByKelas_IdKelas(Integer idKelas);

    // dipakai buat cek kelas mana aja yang punya tugas besar (REQ-19)
    List<KelompokMahasiswa> findByKelas_IdKelasIn(List<Integer> idKelas);

}

