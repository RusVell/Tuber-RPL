package com.example.schedy.entity;

import jakarta.persistence.*;

@Entity
@IdClass(AnggotaKelompokId.class)
@Table(name = "anggota_kelompok")
public class AnggotaKelompok {

    @Id
    @Column(name = "id_kelompok")
    private Integer idKelompok;

    @Id
    @Column(name = "id_pengambilan", unique = true)
    private Integer idPengambilan;

    @Column(name = "id_kelas")
    private Integer idKelas;

    @ManyToOne
    @JoinColumn(name = "id_pengambilan", referencedColumnName = "id_pengambilan",
                insertable = false, updatable = false)
    private PengambilanKelas pengambilan;

    public AnggotaKelompok() {}

    public Integer getIdKelompok() {
        return idKelompok;
    }

    public void setIdKelompok(Integer idKelompok) {
        this.idKelompok = idKelompok;
    }

    public Integer getIdPengambilan() {
        return idPengambilan;
    }

    public void setIdPengambilan(Integer idPengambilan) {
        this.idPengambilan = idPengambilan;
    }

    public Integer getIdKelas() {
        return idKelas;
    }

    public void setIdKelas(Integer idKelas) {
        this.idKelas = idKelas;
    }

    public PengambilanKelas getPengambilan() {
        return pengambilan;
    }

    public void setPengambilan(PengambilanKelas pengambilan) {
        this.pengambilan = pengambilan;
    }
}
