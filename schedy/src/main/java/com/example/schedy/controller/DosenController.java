package com.example.schedy.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.schedy.entity.AnggotaKelompok;
import com.example.schedy.entity.Dosen;
import com.example.schedy.entity.Kelas;
import com.example.schedy.entity.KelompokMahasiswa;
import com.example.schedy.entity.MataKuliah;
import com.example.schedy.entity.Semester;
import com.example.schedy.entity.Users;
import com.example.schedy.repository.AnggotaKelompokRepository;
import com.example.schedy.repository.DosenRepository;
import com.example.schedy.repository.KelasRepository;
import com.example.schedy.repository.KelompokMahasiswaRepository;
import com.example.schedy.repository.MahasiswaRepository;
import com.example.schedy.repository.MataKuliahRepository;
import com.example.schedy.repository.SemesterRepository;
import com.example.schedy.repository.UsersRepository;
import com.example.schedy.entity.SlotPresentasi;
import com.example.schedy.entity.JadwalPresentasiMahasiswa;
import com.example.schedy.repository.SlotPresentasiRepository;
import com.example.schedy.repository.JadwalPresentasiMahasiswaRepository;

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

    @Autowired
    private KelasRepository kelasRepository;

    @Autowired
    private AnggotaKelompokRepository anggotaKelompokRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private MahasiswaRepository mahasiswaRepository;

    @Autowired
    private SlotPresentasiRepository slotPresentasiRepository;

    @Autowired
    private JadwalPresentasiMahasiswaRepository jadwalPresentasiMahasiswaRepository;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Integer filterIdKelas, HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !user.getRole().equals("DOSEN"))
            return "redirect:/login";

        Dosen dosen = dosenRepo.findByUser_IdUser(user.getIdUser());
        Integer idSemester = getIdSemester(session);

        List<Kelas> daftarKelas = kelasRepository.findByDosen_NidnAndSemester_idSemester(dosen.getNidn(), idSemester);
        List<KelompokMahasiswa> daftarKelompok;

        if (filterIdKelas != null) {
            daftarKelompok = kelompokMahasiswaRepository.findByKelas_IdKelas(filterIdKelas);
        } else {
            daftarKelompok = new ArrayList<>();
        }

        for (KelompokMahasiswa k : daftarKelompok) {
            Integer jumlahAnggota = anggotaKelompokRepository.countByIdKelompok(k.getIdKelompok());
            k.setJumlahAnggota(jumlahAnggota);
        }

        model.addAttribute("daftarKelompok", daftarKelompok);
        model.addAttribute("daftarKelas", daftarKelas);
        model.addAttribute("selectedIdKelas", filterIdKelas);

        isiTopbar(model, user, idSemester);

        return "Dashboard_Dosen";
    }

    @GetMapping("/JadwalPresentasi")
    public String jadwalPresentasi(
            @RequestParam(required = false) Integer filterIdSemester,
            @RequestParam(required = false) Integer filterIdMatkul,
            @RequestParam(required = false) LocalDate tanggal,
            HttpSession session,
            Model model) {

        Users user = (Users) session.getAttribute("loggedUser");

        if (user == null || !user.getRole().equals("DOSEN")) {
            return "redirect:/login";
        }

        Dosen dosen = dosenRepo.findByUser_IdUser(user.getIdUser());

        Integer idSemester = filterIdSemester != null
                ? filterIdSemester
                : getIdSemester(session);

        List<Semester> daftarSemester = semesterRepo.findAllByOrderByIdSemesterDesc();

        model.addAttribute("daftarSemester", daftarSemester);
        model.addAttribute("selectedIdSemester", idSemester);

        List<Kelas> daftarKelas = kelasRepository.findByDosen_NidnAndSemester_idSemester(
                dosen.getNidn(),
                idSemester);

        model.addAttribute("daftarKelas", daftarKelas);

        List<MataKuliah> daftarMataKuliah = new ArrayList<>();

        for (Kelas kelas : daftarKelas) {
            MataKuliah mataKuliah = kelas.getMataKuliah();

            if (mataKuliah != null && daftarMataKuliah.stream()
                    .noneMatch(m -> m.getIdMatkul().equals(mataKuliah.getIdMatkul()))) {
                daftarMataKuliah.add(mataKuliah);
            }
        }

        model.addAttribute("daftarMataKuliah", daftarMataKuliah);

        model.addAttribute("selectedIdMatkul", filterIdMatkul);

        LocalDate tanggalReferensi = tanggal != null ? tanggal : LocalDate.now();

        LocalDate tanggalAwalMinggu = tanggalReferensi.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate tanggalAkhirMinggu = tanggalAwalMinggu.plusDays(4);

        LocalDate mingguSebelumnya = tanggalAwalMinggu.minusWeeks(1);
        LocalDate mingguBerikutnya = tanggalAwalMinggu.plusWeeks(1);

        List<Integer> idKelas = new ArrayList<>();

        for (Kelas kelas : daftarKelas) {
            if (filterIdMatkul == null ||
                    (kelas.getMataKuliah() != null &&
                            kelas.getMataKuliah().getIdMatkul().equals(filterIdMatkul))) {

                idKelas.add(kelas.getIdKelas());
            }
        }

        List<SlotPresentasi> daftarSlot = new ArrayList<>();

        if (!idKelas.isEmpty()) {
            daftarSlot = slotPresentasiRepository.findByKelas_IdKelasInAndTanggalBetween(
                    idKelas,
                    tanggalAwalMinggu,
                    tanggalAkhirMinggu);
        }

        model.addAttribute("daftarSlot", daftarSlot);

        List<Integer> idSlot = new ArrayList<>();

        for (SlotPresentasi slot : daftarSlot) {
            idSlot.add(slot.getIdSlot());
        }

        List<JadwalPresentasiMahasiswa> daftarPenetapan = new ArrayList<>();

        if (!idSlot.isEmpty()) {
            daftarPenetapan = jadwalPresentasiMahasiswaRepository.findBySlot_IdSlotIn(idSlot);
        }

        model.addAttribute("daftarPenetapan", daftarPenetapan);

        Map<Integer, JadwalPresentasiMahasiswa> penetapanPerSlot = new HashMap<>();

        for (JadwalPresentasiMahasiswa penetapan : daftarPenetapan) {
            penetapanPerSlot.put(
                    penetapan.getSlot().getIdSlot(),
                    penetapan);
        }

        model.addAttribute("penetapanPerSlot", penetapanPerSlot);

        List<LocalDate> daftarHari = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            daftarHari.add(tanggalAwalMinggu.plusDays(i));
        }

        model.addAttribute("daftarHari", daftarHari);

        List<String> daftarWaktu = new ArrayList<>();

        for (int jam = 8; jam < 17; jam++) {
            daftarWaktu.add(String.format("%02d:00", jam));
            daftarWaktu.add(String.format("%02d:30", jam));
        }

        model.addAttribute("daftarWaktu", daftarWaktu);

        model.addAttribute("tanggalAwalMinggu", tanggalAwalMinggu);
        model.addAttribute("tanggalAkhirMinggu", tanggalAkhirMinggu);
        model.addAttribute("mingguSebelumnya", mingguSebelumnya);
        model.addAttribute("mingguBerikutnya", mingguBerikutnya);

        isiTopbar(model, user, getIdSemester(session));

        return "Jadwal_Presentasi";
    }

    @PostMapping("/dashboard/buatKelompok")
    public String buatKelompok(@RequestParam Integer idKelas, @RequestParam String namaKelompok,
            @RequestParam Integer minAnggota, @RequestParam Integer maxAnggota, HttpSession session) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !user.getRole().equals("DOSEN"))
            return "redirect:/login";

        if ((namaKelompok.length() < 3 || namaKelompok.length() > 10) || (minAnggota < 1 || minAnggota > 2)
                || (maxAnggota < 3 || maxAnggota > 6)) {
            return "redirect:/dosen/dashboard";
        }

        Optional<Kelas> kelasOpt = kelasRepository.findById(idKelas);
        Kelas kelas = kelasOpt.get();

        KelompokMahasiswa newKelompok = new KelompokMahasiswa();
        newKelompok.setKelas(kelas);
        newKelompok.setNamaKelompok(namaKelompok);
        newKelompok.setMinKelompok(minAnggota);
        newKelompok.setMaxKelompok(maxAnggota);

        kelompokMahasiswaRepository.save(newKelompok);

        return "redirect:/dosen/dashboard";
    }

    @GetMapping("/detailKelompok/{idKelompok}")
    @ResponseBody
    public ResponseEntity<?> detailKelompok(@PathVariable Integer idKelompok, HttpSession session) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !user.getRole().equals("DOSEN"))
            return null;

        List<AnggotaKelompok> anggotaKelompok = anggotaKelompokRepository.findByIdKelompok(idKelompok);

        List<Map<String, String>> daftarAnggota = new ArrayList<>();

        for (AnggotaKelompok anggota : anggotaKelompok) {
            String npm = anggota.getNpm();
            Integer idUser = mahasiswaRepository.findIdUserByNpm(npm);
            String name = (usersRepository.findByIdUser(idUser).getNama());

            Map<String, String> mahasiswa = new HashMap<>();
            mahasiswa.put("npm", npm);
            mahasiswa.put("nama", name);
            daftarAnggota.add(mahasiswa);
        }

        return ResponseEntity.ok(daftarAnggota);
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
