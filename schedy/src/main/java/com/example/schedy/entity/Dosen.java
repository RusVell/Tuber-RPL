package com.example.schedy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "dosen")
public class Dosen {

    @Id
    @Column(name = "nidn", length = 20)
    private String nidn;

    @OneToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user", unique = true)
    private Users user;

    public Dosen() {}

    public String getNidn() {
        return nidn;
    }

    public void setNidn(String nidn) {
        this.nidn = nidn;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
