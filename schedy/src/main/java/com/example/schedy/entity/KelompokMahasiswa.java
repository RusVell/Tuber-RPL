package com.example.schedy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kelompok_mahasiswa")
public class KelompokMahasiswa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kelompok")
    private Integer idKelompok;

    @ManyToOne
    @JoinColumn(name = "id_kelas", referencedColumnName = "id_kelas")
    private Kelas kelas;

    @Column(name = "nama_kelompok", length = 100)
    private String namaKelompok;

    @Column(name = "min_kelompok")
    private Integer minKelompok;

    @Column(name = "max_kelompok")
    private Integer maxKelompok;

    @Transient
    private Integer jumlahAnggotaKelompok;

    public KelompokMahasiswa() {}

    public Integer getIdKelompok() {
        return idKelompok;
    }

    public void setIdKelompok(Integer idKelompok) {
        this.idKelompok = idKelompok;
    }

    public Kelas getKelas() {
        return kelas;
    }

    public void setKelas(Kelas kelas) {
        this.kelas = kelas;
    }

    public Integer getMinKelompok() {
        return this.minKelompok;
    }

    public void setMinKelompok(Integer minKelompok) {
        this.minKelompok = minKelompok;
    }

    public Integer getMaxKelompok() {
        return this.maxKelompok;
    }

    public Integer getJumlahAnggota() {
        return this.jumlahAnggotaKelompok;
    }

    public void setJumlahAnggota(Integer jumlahAnggotaKelompok) {
        this.jumlahAnggotaKelompok = jumlahAnggotaKelompok;
    }

    public void setMaxKelompok(Integer maxKelompok) {
        this.maxKelompok = maxKelompok;
    }

    public String getNamaKelompok() {
        return namaKelompok;
    }

    public void setNamaKelompok(String namaKelompok) {
        this.namaKelompok = namaKelompok;
    }
}
