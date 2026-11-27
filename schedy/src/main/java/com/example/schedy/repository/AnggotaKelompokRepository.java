package com.example.schedy.repository;

import com.example.schedy.entity.AnggotaKelompok;
import com.example.schedy.entity.AnggotaKelompokId;
import com.example.schedy.entity.Mahasiswa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnggotaKelompokRepository extends JpaRepository<AnggotaKelompok, AnggotaKelompokId> {

    List<AnggotaKelompok> findByIdKelompok(Integer idKelompok);

    List<AnggotaKelompok> findByIdPengambilanIn(List<Integer> idPengambilan);

    List<AnggotaKelompok> findByIdKelompokIn(List<Integer> ideKelompok);

    Integer countByIdKelompok(Integer idKelompok);

    boolean existsByIdPengambilan(Integer idPengambilan);

    AnggotaKelompok findByIdPengambilan(Integer idPengambilan);

    void deleteByIdPengambilan(Integer idPengambilan);

}
