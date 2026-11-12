package com.example.schedy.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.example.schedy.entity.Users;
import com.example.schedy.repository.SemesterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@Controller
public class SemesterController {
    private final SemesterRepository semesterRepository;

    public SemesterController(SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    // ganti semester dari dropdown di topbar
    @PostMapping("/semester/pilih")
    public String pilihSemester(@RequestParam(value = "idSemester", required = false) Integer idSemester,
                                @RequestParam(value = "returnTo", required = false) String returnTo,
                                HttpSession session, HttpServletRequest request) {
        if (!(session.getAttribute("loggedUser") instanceof Users user)) return "redirect:/login";
        if (idSemester == null || !semesterRepository.existsById(idSemester))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semester tidak ditemukan.");
        session.setAttribute("idSemester", idSemester);
        String fallback = switch (user.getRole()) {
            case "ADMIN" -> "/admin";
            case "MAHASISWA" -> "/mahasiswa";
            case "DOSEN" -> "/dosen";
            default -> "/login";
        };
        String asal = returnTo == null || returnTo.isBlank() ? request.getHeader("Referer") : returnTo;
        if (asal == null) return "redirect:" + fallback;
        try {
            URI uri = URI.create(asal).normalize();
            String path = uri.getRawPath();
            String contextPath = request.getContextPath();
            if (path == null) return "redirect:" + fallback;
            if (!contextPath.isEmpty() && path.startsWith(contextPath + "/")) path = path.substring(contextPath.length());
            // Rekonstruksi hanya path lokal agar tidak mengarahkan pengguna ke situs lain.
            if (!path.equals(fallback) && !path.startsWith(fallback + "/")) return "redirect:" + fallback;
            if (path.contains("\\") || path.contains("//")) return "redirect:" + fallback;
            return "redirect:" + path + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        } catch (IllegalArgumentException e) {
            return "redirect:" + fallback;
        }
    }
}

