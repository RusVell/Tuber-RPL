package com.example.schedy.repository;

import com.example.schedy.entity.SlotPresentasi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlotPresentasiRepository extends JpaRepository<SlotPresentasi, Integer> {

    List<SlotPresentasi> findByKelas_IdKelas(Integer idKelas);

}

