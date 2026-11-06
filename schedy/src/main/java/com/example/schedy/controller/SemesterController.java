package com.example.schedy.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@Controller
public class SemesterController {

    // ganti semester dari dropdown di topbar
    @PostMapping("/semester/pilih")
    public String pilihSemester(@RequestParam("idSemester") Integer idSemester,
                                HttpSession session, HttpServletRequest request) {

        if (session.getAttribute("loggedUser") == null) return "redirect:/login";

        session.setAttribute("idSemester", idSemester);

        // balik lagi ke halaman sebelumnya
        String asal = request.getHeader("Referer");
        if (asal == null) return "redirect:/login";
        return "redirect:" + URI.create(asal).getPath();
    }
}

