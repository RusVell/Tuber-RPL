package com.example.schedy.entity;

import java.io.Serializable;
import java.util.Objects;

public class AnggotaKelompokId implements Serializable {

    private Integer idKelompok;
    private Integer idPengambilan;

    public AnggotaKelompokId() {}

    public AnggotaKelompokId(Integer idKelompok, Integer idPengambilan) {
        this.idKelompok = idKelompok;
        this.idPengambilan = idPengambilan;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof AnggotaKelompokId)) return false;
        AnggotaKelompokId that = (AnggotaKelompokId) o;
        return Objects.equals(idKelompok, that.idKelompok) && Objects.equals(idPengambilan, that.idPengambilan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idKelompok, idPengambilan);
    }
}

