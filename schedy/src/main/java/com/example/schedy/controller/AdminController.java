package com.example.schedy.controller;

import com.example.schedy.entity.Users;
import com.example.schedy.entity.JadwalUjian;
import com.example.schedy.entity.Kelas;
import com.example.schedy.entity.Semester;
import com.example.schedy.service.AkademicDataService;
import com.example.schedy.service.AkademicDataService.*;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.schedy.repository.UsersRepository;
import com.example.schedy.repository.MataKuliahRepository;
import com.example.schedy.repository.JadwalKuliahRepository;
import com.example.schedy.repository.SemesterRepository;
import com.example.schedy.repository.KelasRepository;
import com.example.schedy.repository.JadwalUjianRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private final SemesterRepository semesterRepository;
    private final KelasRepository kelasRepository;
    private final JadwalUjianRepository jadwalUjianRepository;
    private final UsersRepository usersRepository;
    private final MataKuliahRepository mataKuliahRepository;
    private final JadwalKuliahRepository jadwalKuliahRepository;
    private final AkademicDataService AkademicDataService;

    public AdminController(SemesterRepository semesterRepository, KelasRepository kelasRepository,
            JadwalUjianRepository jadwalUjianRepository, UsersRepository usersRepository,
            MataKuliahRepository mataKuliahRepository, JadwalKuliahRepository jadwalKuliahRepository,
            AkademicDataService AkademicDataService) {
        this.semesterRepository = semesterRepository;
        this.kelasRepository = kelasRepository;
        this.jadwalUjianRepository = jadwalUjianRepository;
        this.usersRepository = usersRepository;
        this.mataKuliahRepository = mataKuliahRepository;
        this.jadwalKuliahRepository = jadwalKuliahRepository;
        this.AkademicDataService = AkademicDataService;
    }

    @GetMapping("/admin")
    public String dashboard(HttpSession session) {
        return adminOnly(session, "Dashboard_Admin");
    }

    @GetMapping("/admin/semester")
    public String kelolaSemester(HttpSession session, Model model) {
        String view = adminOnly(session, "Kelola_Semester_Admin");
        if (view.startsWith("redirect:"))
            return view;
        model.addAttribute("semesters", semesterRepository.findAllByOrderByIdSemesterDesc());
        return view;
    }

    @GetMapping("/admin/import-csv")
    public String importCsv(HttpSession session) {
        return adminOnly(session, "Import_CSV_Admin");
    }

    @GetMapping("/admin/data-akademik")
    public String dataAkademik(HttpSession session, Model model,
            @RequestParam(defaultValue = "akun") String kategori) {
        String view = adminOnly(session, "Data_Akademik_Admin");
        if (view.startsWith("redirect:"))
            return view;
        isiDataAkademik(session, model, kategori);
        return view;
    }

    @PostMapping("/admin/data-akademik/akun")
    public String tambahAkun(HttpSession session,
            @ModelAttribute("akunForm") AkunForm form,
            BindingResult errors, Model model, RedirectAttributes redirect) {

        String halaman = adminOnly(session, "Data_Akademik_Admin");
        if (halaman.startsWith("redirect:"))
            return halaman;

        isiDataAkademik(session, model, "akun");

        if (blankOrLong(form.nama(), 100)
                || blankOrLong(form.nomor_induk(), 20)
                || !AkademicDataService.ROLES.contains(form.role())) {
            errors.reject("akun.invalid", "Data akun tidak valid.");
        }

        if (errors.hasErrors())
            return halaman;

        try {
            AccountCredentials akun = AkademicDataService.tambahAkun(form);
            redirect.addFlashAttribute("accountCredentials", akun);
            redirect.addFlashAttribute("academicSuccess", "Akun berhasil ditambahkan.");
            return "redirect:/admin/data-akademik?kategori=akun";
        } catch (IllegalArgumentException e) {
            errors.reject("akun.invalid", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            errors.reject("akun.duplicate", "NPM/NIDN sudah digunakan.");
        }

        return halaman;
    }

    @PostMapping("/admin/data-akademik/mata-kuliah")
    public String tambahMataKuliah(HttpSession session, @ModelAttribute("matkulForm") MatkulForm form,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        String view = adminOnly(session, "Data_Akademik_Admin");
        if (view.startsWith("redirect:"))
            return view;
        Integer semesterId = isiDataAkademik(session, model, "mata-kuliah");
        if (blankOrLong(form.kode_matkul(), 20) || blankOrLong(form.nama_matkul(), 100)
                || blankOrLong(form.nama_kelas(), 20) || blankOrLong(form.nip_dosen(), 20)
                || form.sks() == null || form.sks() < 1 || form.sks() > 24)
            errors.reject("matkul.required", "Isi seluruh kolom; SKS harus berupa angka 1 sampai 24.");
        if (semesterId == null)
            errors.reject("matkul.semester", "Pilih semester terlebih dahulu.");
        if (errors.hasErrors())
            return view;
        try {
            AkademicDataService.tambahMatkul(form, semesterId);
            redirect.addFlashAttribute("academicSuccess", "Mata kuliah dan kelas berhasil ditambahkan.");
            return "redirect:/admin/data-akademik?kategori=mata-kuliah";
        } catch (IllegalArgumentException e) {
            errors.reject("matkul.invalid", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            errors.reject("matkul.data", "Data kelas tidak dapat disimpan. Periksa data dosen dan mata kuliah.");
        }
        return view;
    }

    @PostMapping("/admin/data-akademik/jadwal-kuliah")
    public String tambahJadwalKuliah(HttpSession session,
            @ModelAttribute("kuliahForm") KuliahForm form,
            BindingResult errors, Model model, RedirectAttributes redirect) {

        String halaman = adminOnly(session, "Data_Akademik_Admin");
        if (halaman.startsWith("redirect:"))
            return halaman;

        Integer semesterId = isiDataAkademik(session, model, "jadwal");

        if (semesterId == null) {
            errors.reject("kuliah.semester", "Pilih semester terlebih dahulu.");
        }

        if (blankOrLong(form.kode_matkul(), 20)
                || blankOrLong(form.nama_kelas(), 20)
                || blankOrLong(form.ruangan(), 100)
                || form.hari() == null
                || !AkademicDataService.DAYS.contains(form.hari())
                || form.jam_mulai() == null
                || form.jam_selesai() == null) {
            errors.reject("kuliah.required", "Data jadwal belum lengkap atau tidak valid.");
        }

        if (errors.hasErrors())
            return halaman;

        if (!form.jam_selesai().isAfter(form.jam_mulai())) {
            errors.reject("kuliah.time", "Jam selesai harus setelah jam mulai.");
            return halaman;
        }

        try {
            AkademicDataService.tambahKuliah(form, semesterId);
            redirect.addFlashAttribute("academicSuccess", "Jadwal kuliah berhasil ditambahkan.");
            return "redirect:/admin/data-akademik?kategori=jadwal";
        } catch (IllegalArgumentException e) {
            errors.reject("kuliah.invalid", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            errors.reject("kuliah.data", "Jadwal tidak dapat disimpan. Periksa data kelas.");
        }

        return halaman;
    }

    public record JadwalUjianForm(String kode_matkul, String nama_kelas, String jenis_ujian,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggal,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime jam_mulai,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime jam_selesai, String ruangan) {
    }

    @PostMapping("/admin/data-akademik/jadwal-ujian")
    public String tambahUjianAkademik(HttpSession session,
            @ModelAttribute("ujianForm") JadwalUjianForm form,
            BindingResult errors, Model model, RedirectAttributes redirect) {

        String halaman = adminOnly(session, "Data_Akademik_Admin");
        if (halaman.startsWith("redirect:"))
            return halaman;

        Integer semesterId = isiDataAkademik(session, model, "jadwal-ujian");

        if (semesterId == null) {
            errors.reject("ujian.semester", "Pilih semester terlebih dahulu.");
        }

        if (blankOrLong(form.kode_matkul(), 20)
                || blankOrLong(form.nama_kelas(), 20)
                || blankOrLong(form.jenis_ujian(), 50)
                || blankOrLong(form.ruangan(), 100)
                || form.tanggal() == null
                || form.jam_mulai() == null
                || form.jam_selesai() == null) {
            errors.reject("ujian.required", "Data ujian belum lengkap atau tidak valid.");
        }

        if (errors.hasErrors())
            return halaman;

        if (!form.jam_selesai().isAfter(form.jam_mulai())) {
            errors.reject("ujian.time", "Jam selesai harus setelah jam mulai.");
            return halaman;
        }

        List<Kelas> daftarKelas = AkademicDataService.cariKelas(
                form.kode_matkul().strip(),
                form.nama_kelas().strip(),
                semesterId);

        if (daftarKelas.size() != 1) {
            errors.reject("ujian.class", "Kelas tidak ditemukan atau tidak unik.");
            return halaman;
        }

        Kelas kelas = daftarKelas.get(0);

        for (JadwalUjian jadwal : jadwalUjianRepository.findByKelas_IdKelas(kelas.getIdKelas())) {
            if (form.tanggal().equals(jadwal.getTanggal())
                    && form.jam_mulai().equals(jadwal.getJamMulai())) {
                errors.reject("ujian.duplicate", "Jadwal ujian sudah ada.");
                return halaman;
            }
        }

        JadwalUjian ujian = new JadwalUjian();
        ujian.setKelas(kelas);
        ujian.setJenisUjian(form.jenis_ujian().strip());
        ujian.setTanggal(form.tanggal());
        ujian.setJamMulai(form.jam_mulai());
        ujian.setJamSelesai(form.jam_selesai());
        ujian.setRuangan(form.ruangan().strip());

        jadwalUjianRepository.save(ujian);

        redirect.addFlashAttribute("ujianSuccess", "Jadwal ujian berhasil ditambahkan.");
        return "redirect:/admin/data-akademik?kategori=jadwal-ujian";
    }

    private boolean blankOrLong(String value, int max) {
        return value == null || value.isBlank() || value.strip().length() > max;
    }

    private Integer isiDataAkademik(HttpSession session, Model model, String kategori) {
        List<Semester> semesters = semesterRepository.findAllByOrderByIdSemesterDesc();
        Integer selected = (Integer) session.getAttribute("idSemester");
        Integer requested = selected;
        if (semesters.stream().noneMatch(s -> s.getIdSemester().equals(requested))) {
            selected = semesters.stream().filter(s -> Boolean.TRUE.equals(s.getStatusAktif()))
                    .findFirst().or(() -> semesters.stream().findFirst()).map(Semester::getIdSemester).orElse(null);
            if (selected != null)
                session.setAttribute("idSemester", selected);
        }
        List<Integer> kelasIds = selected == null ? List.of()
                : kelasRepository.findBySemester_IdSemester(selected)
                        .stream().map(Kelas::getIdKelas).toList();
        model.addAttribute("semesters", semesters);
        model.addAttribute("idSemester", selected);
        model.addAttribute("kategori",
                List.of("akun", "mata-kuliah", "jadwal", "jadwal-ujian").contains(kategori) ? kategori : "akun");
        model.addAttribute("akunList", usersRepository.findAll());
        model.addAttribute("mataKuliahList", mataKuliahRepository.findAll());
        model.addAttribute("kelasList",
                selected == null ? List.of() : kelasRepository.findBySemester_IdSemester(selected));
        model.addAttribute("roles", AkademicDataService.ROLES);
        model.addAttribute("hariList", AkademicDataService.DAYS);
        if (!model.containsAttribute("akunForm"))
            model.addAttribute("akunForm", new AkunForm(null, null, null));
        if (!model.containsAttribute("matkulForm"))
            model.addAttribute("matkulForm", new MatkulForm(null, null, null, null, null));
        if (!model.containsAttribute("kuliahForm"))
            model.addAttribute("kuliahForm", new KuliahForm(null, null, null, null, null, null));
        if (!model.containsAttribute("ujianForm"))
            model.addAttribute("ujianForm", new JadwalUjianForm(null, null, null, null, null, null, null));
        model.addAttribute("jadwalList",
                kelasIds.isEmpty() ? List.of() : jadwalKuliahRepository.findByKelas_IdKelasIn(kelasIds));
        model.addAttribute("ujianList",
                kelasIds.isEmpty() ? List.of() : jadwalUjianRepository.findByKelas_IdKelasIn(kelasIds));
        return selected;
    }

    private String adminOnly(HttpSession session, String view) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        return view;
    }
}
