package com.example.schedy.repository;

import com.example.schedy.entity.JadwalMengawasUjianGuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JadwalMengawasUjianGuruRepository extends JpaRepository<JadwalMengawasUjianGuru, Integer> {

    List<JadwalMengawasUjianGuru> findByDosen_NidnAndTanggal(String nidn, LocalDate tanggal);

}
