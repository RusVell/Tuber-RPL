package com.example.schedy.repository;

import com.example.schedy.entity.JadwalPresentasiMahasiswa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JadwalPresentasiMahasiswaRepository extends JpaRepository<JadwalPresentasiMahasiswa, Integer> {

    List<JadwalPresentasiMahasiswa> findByKelompok_IdKelompokIn(List<Integer> idKelompok);

}

