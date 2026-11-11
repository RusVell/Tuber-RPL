package com.example.schedy.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "jadwal_ujian")
public class JadwalUjian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ujian")
    private Integer idUjian;

    @ManyToOne
    @JoinColumn(name = "id_kelas", referencedColumnName = "id_kelas")
    private Kelas kelas;

    @Column(name = "tanggal")
    private LocalDate tanggal;

    @Column(name = "jam_mulai")
    private LocalTime jamMulai;

    @Column(name = "jenis_ujian", length = 50)
    private String jenisUjian;

    @Column(name = "jam_selesai")
    private LocalTime jamSelesai;

    @Column(name = "ruangan", length = 100)
    private String ruangan;

    public JadwalUjian() {}

    public String getHari() {
        if (tanggal == null) return "SENIN";
        switch (tanggal.getDayOfWeek()) {
            case MONDAY: return "SENIN";
            case TUESDAY: return "SELASA";
            case WEDNESDAY: return "RABU";
            case THURSDAY: return "KAMIS";
            case FRIDAY: return "JUMAT";
            case SATURDAY: return "SABTU";
            default: return "MINGGU";
        }
    }

    public Integer getIdUjian() {
        return idUjian;
    }

    public void setIdUjian(Integer idUjian) {
        this.idUjian = idUjian;
    }

    public Kelas getKelas() {
        return kelas;
    }

    public void setKelas(Kelas kelas) {
        this.kelas = kelas;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public LocalTime getJamMulai() {
        return jamMulai;
    }

    public void setJamMulai(LocalTime jamMulai) {
        this.jamMulai = jamMulai;
    }

    public String getRuangan() {
        return ruangan;
    }

    public String getJenisUjian() { return jenisUjian; }

    public void setJenisUjian(String jenisUjian) { this.jenisUjian = jenisUjian; }

    public LocalTime getJamSelesai() { return jamSelesai; }

    public void setJamSelesai(LocalTime jamSelesai) { this.jamSelesai = jamSelesai; }

    public void setRuangan(String ruangan) {
        this.ruangan = ruangan;
    }
}
