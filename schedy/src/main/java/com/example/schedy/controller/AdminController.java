package com.example.schedy.controller;

import com.example.schedy.entity.Users;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String dashboard(HttpSession session) {
        return adminOnly(session, "Dashboard_Admin");
    }

    @GetMapping("/admin/semester")
    public String kelolaSemester(HttpSession session) {
        return adminOnly(session, "Kelola_Semester_Admin");
    }

    @GetMapping("/admin/import-csv")
    public String importCsv(HttpSession session) {
        return adminOnly(session, "Import_CSV_Admin");
    }

    @GetMapping("/admin/data-akademik")
    public String dataAkademik(HttpSession session) {
        return adminOnly(session, "Data_Akademik_Admin");
    }

    private String adminOnly(HttpSession session, String view) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        return view;
    }
}
