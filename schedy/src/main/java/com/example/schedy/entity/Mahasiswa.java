package com.example.schedy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mahasiswa")
public class Mahasiswa {

    @Id
    @Column(name = "npm", length = 20)
    private String npm;

    @OneToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user", unique = true)
    private Users user;

    public Mahasiswa() {}

    public String getNpm() {
        return npm;
    }

    public void setNpm(String npm) {
        this.npm = npm;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
