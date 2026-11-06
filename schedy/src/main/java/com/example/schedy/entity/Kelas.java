package com.example.schedy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "kelas")
public class Kelas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kelas")
    private Integer idKelas;

    @Column(name = "nama_kelas", nullable = false, length = 20)
    private String namaKelas;

    @ManyToOne
    @JoinColumn(name = "id_matkul", referencedColumnName = "id_matkul")
    private MataKuliah mataKuliah;

    @ManyToOne
    @JoinColumn(name = "id_semester", referencedColumnName = "id_semester")
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "nidn", referencedColumnName = "nidn")
    private Dosen dosen;

    public Kelas() {}

    public Integer getIdKelas() {
        return idKelas;
    }

    public void setIdKelas(Integer idKelas) {
        this.idKelas = idKelas;
    }

    public String getNamaKelas() {
        return namaKelas;
    }

    public void setNamaKelas(String namaKelas) {
        this.namaKelas = namaKelas;
    }

    public MataKuliah getMataKuliah() {
        return mataKuliah;
    }

    public void setMataKuliah(MataKuliah mataKuliah) {
        this.mataKuliah = mataKuliah;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public Dosen getDosen() {
        return dosen;
    }

    public void setDosen(Dosen dosen) {
        this.dosen = dosen;
    }
}
