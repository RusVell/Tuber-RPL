package com.example.schedy.controller;

import com.example.schedy.entity.Users;
import com.example.schedy.repository.UsersRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UsersRepository usersRepository;

    // landing page langsung ke halaman login (tidak ada sign up)
    @GetMapping("/")
    public String landingPage() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        Users user = (Users) session.getAttribute("loggedUser");

        // kalau sudah login, langsung lempar ke halaman sesuai role
        if (user != null) {
            return redirectByRole(user);
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model, HttpSession session) {

        Users user = usersRepository.findByUsernameAndPasswordHash(username, password);

        if (user != null) {
            // simpan user ke session
            session.setAttribute("loggedUser", user);
            return redirectByRole(user);
        }

        model.addAttribute("error", "Username atau Password salah!");
        model.addAttribute("username", username);
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // hapus session biar aman
        session.invalidate();
        return "redirect:/login";
    }

    private String redirectByRole(Users user) {
        switch (user.getRole()) {
            case "MAHASISWA": return "redirect:/mahasiswa";
            case "DOSEN": return "redirect:/dosen";
            case "ADMIN": return "redirect:/admin";
            default: return "redirect:/login";
        }
    }
}
