package com.example.schedy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pengambilan_kelas")
public class PengambilanKelas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pengambilan")
    private Integer idPengambilan;

    @ManyToOne
    @JoinColumn(name = "npm", referencedColumnName = "npm")
    private Mahasiswa mahasiswa;

    @ManyToOne
    @JoinColumn(name = "id_kelas", referencedColumnName = "id_kelas")
    private Kelas kelas;

    public PengambilanKelas() {}

    public Integer getIdPengambilan() {
        return idPengambilan;
    }

    public void setIdPengambilan(Integer idPengambilan) {
        this.idPengambilan = idPengambilan;
    }

    public Mahasiswa getMahasiswa() {
        return mahasiswa;
    }

    public void setMahasiswa(Mahasiswa mahasiswa) {
        this.mahasiswa = mahasiswa;
    }

    public Kelas getKelas() {
        return kelas;
    }

    public void setKelas(Kelas kelas) {
        this.kelas = kelas;
    }
}
