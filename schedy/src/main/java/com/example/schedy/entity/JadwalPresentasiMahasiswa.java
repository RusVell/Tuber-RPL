package com.example.schedy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "jadwal_presentasi_mahasiswa")
public class JadwalPresentasiMahasiswa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_presentasi")
    private Integer idPresentasi;

    @ManyToOne
    @JoinColumn(name = "id_kelompok", referencedColumnName = "id_kelompok")
    private KelompokMahasiswa kelompok;

    @ManyToOne
    @JoinColumn(name = "id_slot", referencedColumnName = "id_slot")
    private SlotPresentasi slot;

    public JadwalPresentasiMahasiswa() {}

    public Integer getIdPresentasi() {
        return idPresentasi;
    }

    public void setIdPresentasi(Integer idPresentasi) {
        this.idPresentasi = idPresentasi;
    }

    public KelompokMahasiswa getKelompok() {
        return kelompok;
    }

    public void setKelompok(KelompokMahasiswa kelompok) {
        this.kelompok = kelompok;
    }

    public SlotPresentasi getSlot() {
        return slot;
    }

    public void setSlot(SlotPresentasi slot) {
        this.slot = slot;
    }
}
