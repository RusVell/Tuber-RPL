package com.example.schedy.repository;

import com.example.schedy.entity.AnggotaKelompok;
import com.example.schedy.entity.AnggotaKelompokId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnggotaKelompokRepository extends JpaRepository<AnggotaKelompok, AnggotaKelompokId> {

    List<AnggotaKelompok> findByIdKelompok(Integer idKelompok);

    List<AnggotaKelompok> findByIdKelas(Integer idKelas);

    List<AnggotaKelompok> findByIdPengambilanIn(List<Integer> idPengambilan);

    boolean existsByIdPengambilan(Integer idPengambilan);

    AnggotaKelompok findByIdPengambilan(Integer idPengambilan);

    void deleteByIdPengambilan(Integer idPengambilan);

}
