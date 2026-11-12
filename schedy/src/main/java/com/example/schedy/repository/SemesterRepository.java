package com.example.schedy.repository;

import com.example.schedy.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Integer> {
    boolean existsByTahunAjaranAndJenisSemester(String tahunAjaran, String jenisSemester);

    // buat dropdown semester di topbar
    List<Semester> findAllByOrderByIdSemesterDesc();

    // semester yang lagi berjalan
    Semester findFirstByStatusAktifTrue();

}

