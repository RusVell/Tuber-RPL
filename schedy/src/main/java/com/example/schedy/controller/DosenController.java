package com.example.schedy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.schedy.entity.Dosen;
import com.example.schedy.entity.KelompokMahasiswa;
import com.example.schedy.entity.Semester;
import com.example.schedy.entity.Users;
import com.example.schedy.repository.DosenRepository;
import com.example.schedy.repository.KelompokMahasiswaRepository;
import com.example.schedy.repository.SemesterRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/dosen")
public class DosenController {
    @Autowired
    private DosenRepository dosenRepo;

    @Autowired
    private SemesterRepository semesterRepo;

    @Autowired
    private KelompokMahasiswaRepository kelompokMahasiswaRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !user.getRole().equals("DOSEN"))
            return "redirect:/login";

        Dosen dosen = dosenRepo.findByUser_IdUser(user.getIdUser());
        Integer idSemester = getIdSemester(session);
        List<KelompokMahasiswa> daftarKelompok = kelompokMahasiswaRepository.findByKelas_IdKelas(1);
        model.addAttribute("daftarKelompok", daftarKelompok);

        isiTopbar(model, user, idSemester);

        return "Dashboard_Dosen";
    }

    private Integer getIdSemester(HttpSession session) {
        Integer idSemester = (Integer) session.getAttribute("idSemester");
        if (idSemester == null) {
            Semester aktif = semesterRepo.findFirstByStatusAktifTrue();
            if (aktif != null) {
                idSemester = aktif.getIdSemester();
                session.setAttribute("idSemester", idSemester);
            }
        }
        return idSemester;
    }

    private void isiTopbar(Model model, Users user, Integer idSemester) {
        model.addAttribute("namaUser", user.getNama());
        model.addAttribute("semesterList", semesterRepo.findAllByOrderByIdSemesterDesc());
        model.addAttribute("idSemester", idSemester);
    }
}
