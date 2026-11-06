package com.example.schedy.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "slot_presentasi")
public class SlotPresentasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_slot")
    private Integer idSlot;

    @ManyToOne
    @JoinColumn(name = "id_kelas", referencedColumnName = "id_kelas")
    private Kelas kelas;

    @Column(name = "tanggal")
    private LocalDate tanggal;

    @Column(name = "jam_mulai")
    private LocalTime jamMulai;

    @Column(name = "durasi_menit")
    private Integer durasiMenit;

    @Column(name = "lokasi", length = 100)
    private String lokasi;

    public SlotPresentasi() {}

    public LocalTime getJamSelesai() {
        if (jamMulai == null || durasiMenit == null) return null;
        return jamMulai.plusMinutes(durasiMenit);
    }

    public Integer getIdSlot() {
        return idSlot;
    }

    public void setIdSlot(Integer idSlot) {
        this.idSlot = idSlot;
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

    public Integer getDurasiMenit() {
        return durasiMenit;
    }

    public void setDurasiMenit(Integer durasiMenit) {
        this.durasiMenit = durasiMenit;
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }
}
