package com.example.schedy.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "jadwal_kuliah")
public class JadwalKuliah {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kuliah")
    private Integer idKuliah;

    @ManyToOne
    @JoinColumn(name = "id_kelas", referencedColumnName = "id_kelas")
    private Kelas kelas;

    @Column(name = "hari")
    private String hari; // SENIN, SELASA, RABU, KAMIS, JUMAT, SABTU, MINGGU

    @Column(name = "jam_mulai")
    private LocalTime jamMulai;

    @Column(name = "jam_selesai")
    private LocalTime jamSelesai;

    @Column(name = "ruangan", length = 100)
    private String ruangan;

    public JadwalKuliah() {}

    // buat posisi kotak di kalender dashboard (1 jam = 48px, mulai jam 07.00) 
    public int getPosisiAtas() {
        if (jamMulai == null) return 0;
        int menit = (jamMulai.getHour() - 7) * 60 + jamMulai.getMinute();
        return menit * 48 / 60;
    }

    public int getTinggi() {
        if (jamMulai == null || jamSelesai == null) return 48;
        int menit = (jamSelesai.getHour() * 60 + jamSelesai.getMinute())
                  - (jamMulai.getHour() * 60 + jamMulai.getMinute());
        return Math.max(28, menit * 48 / 60);
    }

    public Integer getIdKuliah() {
        return idKuliah;
    }

    public void setIdKuliah(Integer idKuliah) {
        this.idKuliah = idKuliah;
    }

    public Kelas getKelas() {
        return kelas;
    }

    public void setKelas(Kelas kelas) {
        this.kelas = kelas;
    }

    public String getHari() {
        return hari;
    }

    public void setHari(String hari) {
        this.hari = hari;
    }

    public LocalTime getJamMulai() {
        return jamMulai;
    }

    public void setJamMulai(LocalTime jamMulai) {
        this.jamMulai = jamMulai;
    }

    public LocalTime getJamSelesai() {
        return jamSelesai;
    }

    public void setJamSelesai(LocalTime jamSelesai) {
        this.jamSelesai = jamSelesai;
    }

    public String getRuangan() {
        return ruangan;
    }

    public void setRuangan(String ruangan) {
        this.ruangan = ruangan;
    }
}
