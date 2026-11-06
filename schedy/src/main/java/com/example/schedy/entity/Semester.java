package com.example.schedy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "semester")
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_semester")
    private Integer idSemester;

    @Column(name = "tahun_ajaran", nullable = false, length = 9)
    private String tahunAjaran;

    @Column(name = "jenis_semester", nullable = false)
    private String jenisSemester;

    @Column(name = "status_aktif")
    private Boolean statusAktif;

    public Semester() {}

    public Integer getIdSemester() {
        return idSemester;
    }

    public void setIdSemester(Integer idSemester) {
        this.idSemester = idSemester;
    }

    public String getTahunAjaran() {
        return tahunAjaran;
    }

    public void setTahunAjaran(String tahunAjaran) {
        this.tahunAjaran = tahunAjaran;
    }

    public String getJenisSemester() {
        return jenisSemester;
    }

    public void setJenisSemester(String jenisSemester) {
        this.jenisSemester = jenisSemester;
    }

    public Boolean getStatusAktif() {
        return statusAktif;
    }

    public void setStatusAktif(Boolean statusAktif) {
        this.statusAktif = statusAktif;
    }
}
