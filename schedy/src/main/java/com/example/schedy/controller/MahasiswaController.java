package com.example.schedy.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.schedy.entity.AnggotaKelompok;
import com.example.schedy.entity.JadwalKuliah;
import com.example.schedy.entity.JadwalPresentasiMahasiswa;
import com.example.schedy.entity.JadwalUjian;
import com.example.schedy.entity.Kelas;
import com.example.schedy.entity.KelompokMahasiswa;
import com.example.schedy.entity.Mahasiswa;
import com.example.schedy.entity.PengambilanKelas;
import com.example.schedy.entity.Semester;
import com.example.schedy.entity.Users;
import com.example.schedy.repository.AnggotaKelompokRepository;
import com.example.schedy.repository.JadwalKuliahRepository;
import com.example.schedy.repository.JadwalPresentasiMahasiswaRepository;
import com.example.schedy.repository.JadwalUjianRepository;
import com.example.schedy.repository.KelasRepository;
import com.example.schedy.repository.KelompokMahasiswaRepository;
import com.example.schedy.repository.MahasiswaRepository;
import com.example.schedy.repository.PengambilanKelasRepository;
import com.example.schedy.repository.SemesterRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class MahasiswaController {

    public static final int MIN_ANGGOTA = 3;

    @Autowired
    private MahasiswaRepository mahasiswaRepo;

    @Autowired
    private SemesterRepository semesterRepo;

    @Autowired
    private KelasRepository kelasRepo;

    @Autowired
    private PengambilanKelasRepository pengambilanRepo;

    @Autowired
    private JadwalKuliahRepository jadwalKuliahRepo;

    @Autowired
    private JadwalUjianRepository jadwalUjianRepo;

    @Autowired
    private KelompokMahasiswaRepository kelompokRepo;

    @Autowired
    private AnggotaKelompokRepository anggotaRepo;

    @Autowired
    private JadwalPresentasiMahasiswaRepository jadwalPresentasiRepo;

    // =====================================================
    //  DASHBOARD - PENCATATAN MATA KULIAH
    // =====================================================
    @GetMapping("/mahasiswa")
    public String dashboard(HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !user.getRole().equals("MAHASISWA")) return "redirect:/login";

        Mahasiswa mhs = mahasiswaRepo.findByUser_IdUser(user.getIdUser());
        Integer idSemester = getIdSemester(session);
        isiTopbar(model, user, idSemester);

        // kelas yang sudah dicatat mahasiswa
        List<PengambilanKelas> pengambilanList = getPengambilan(mhs, idSemester);

        List<Integer> idKelasDiambil = new ArrayList<>();
        Set<Integer> idMatkulDiambil = new HashSet<>();
        for (PengambilanKelas p : pengambilanList) {
            idKelasDiambil.add(p.getKelas().getIdKelas());
            idMatkulDiambil.add(p.getKelas().getMataKuliah().getIdMatkul());
        }

        // kelas yang masih bisa dipilih di semester ini (belum diambil mahasiswa ini)
        List<Kelas> kelasTersedia = new ArrayList<>();
        if (idSemester != null) {
            for (Kelas k : kelasRepo.findBySemester_IdSemester(idSemester)) {
                if (!idMatkulDiambil.contains(k.getMataKuliah().getIdMatkul())) {
                    kelasTersedia.add(k);
                }
            }
        }

        // jadwal kuliah & ujian dari kelas yang diambil
        List<JadwalKuliah> jadwalKuliah = new ArrayList<>();
        List<JadwalUjian> jadwalUjian = new ArrayList<>();
        if (!idKelasDiambil.isEmpty()) {
            jadwalKuliah = jadwalKuliahRepo.findByKelas_IdKelasIn(idKelasDiambil);
            jadwalUjian = jadwalUjianRepo.findByKelas_IdKelasIn(idKelasDiambil);
        }

        // dikelompokkan per kelas buat ditampilkan di tabel
        Map<Integer, List<JadwalKuliah>> kuliahPerKelas = new HashMap<>();
        for (JadwalKuliah jk : jadwalKuliah) {
            kuliahPerKelas.computeIfAbsent(jk.getKelas().getIdKelas(), k -> new ArrayList<>()).add(jk);
        }

        Map<Integer, List<JadwalUjian>> ujianPerKelas = new HashMap<>();
        for (JadwalUjian ju : jadwalUjian) {
            ujianPerKelas.computeIfAbsent(ju.getKelas().getIdKelas(), k -> new ArrayList<>()).add(ju);
        }

        model.addAttribute("pengambilanList", pengambilanList);
        model.addAttribute("kelasTersedia", kelasTersedia);
        model.addAttribute("jadwalKuliah", jadwalKuliah);
        model.addAttribute("jadwalUjian", jadwalUjian);
        model.addAttribute("kuliahPerKelas", kuliahPerKelas);
        model.addAttribute("ujianPerKelas", ujianPerKelas);
        model.addAttribute("hariList", Arrays.asList("SENIN", "SELASA", "RABU", "KAMIS", "JUMAT"));
        model.addAttribute("jamList", Arrays.asList("07.00", "08.00", "09.00", "10.00", "11.00", "12.00",
                "13.00", "14.00", "15.00", "16.00", "17.00"));

        return "dashboardMahasiswa";
    }

    @PostMapping("/mahasiswa/matkul/tambah")
    public String tambahMatkul(@RequestParam("idKelas") Integer idKelas,
                               HttpSession session, RedirectAttributes ra) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !user.getRole().equals("MAHASISWA")) return "redirect:/login";

        Mahasiswa mhs = mahasiswaRepo.findByUser_IdUser(user.getIdUser());
        Integer idSemester = getIdSemester(session);
        Kelas kelas = kelasRepo.findById(idKelas).orElse(null);

        if (mhs == null || kelas == null || !kelas.getSemester().getIdSemester().equals(idSemester)) {
            ra.addFlashAttribute("error", "Kelas tidak ditemukan di semester ini.");
            return "redirect:/mahasiswa";
        }

        // tolak kalau kelas yang sama sudah dicatat
        if (pengambilanRepo.existsByMahasiswa_NpmAndKelas_IdKelas(mhs.getNpm(), idKelas)) {
            ra.addFlashAttribute("error", "Kelas ini sudah kamu catat sebelumnya.");
            return "redirect:/mahasiswa";
        }

        // mata kuliah cuma boleh 1 kelas
        List<PengambilanKelas> kelasSudahDiambil = getPengambilan(mhs, idSemester);
        for (PengambilanKelas p : kelasSudahDiambil) {
            if (p.getKelas().getMataKuliah().getIdMatkul().equals(kelas.getMataKuliah().getIdMatkul())) {
                ra.addFlashAttribute("error", "Mata kuliah " + kelas.getMataKuliah().getNamaMatkul() + " sudah kamu catat di kelas " + p.getKelas().getNamaKelas() + ".");
                return "redirect:/mahasiswa";
            }
        }

        // =========================================================================
        // VALIDASI BENTROK JADWAL KULIAH & UJIAN
        // =========================================================================
        List<Integer> listIdKelasDiambil = new ArrayList<>();
        for (PengambilanKelas p : kelasSudahDiambil) {
            listIdKelasDiambil.add(p.getKelas().getIdKelas());
        }

        // Cek bentrok Jadwal Kuliah Reguler
        List<JadwalKuliah> jadwalBaruList = jadwalKuliahRepo.findByKelas_IdKelas(idKelas);
        if (!listIdKelasDiambil.isEmpty() && !jadwalBaruList.isEmpty()) {
            List<JadwalKuliah> jadwalLamaList = jadwalKuliahRepo.findByKelas_IdKelasIn(listIdKelasDiambil);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH.mm");

            for (JadwalKuliah jBaru : jadwalBaruList) {
                for (JadwalKuliah jLama : jadwalLamaList) {
                    if (jBaru.getHari() != null && jLama.getHari() != null
                            && jBaru.getHari().equalsIgnoreCase(jLama.getHari())) {

                        // Cek irisan waktu: Max(Start1, Start2) < Min(End1, End2)
                        LocalTime start1 = jBaru.getJamMulai();
                        LocalTime end1 = jBaru.getJamSelesai();
                        LocalTime start2 = jLama.getJamMulai();
                        LocalTime end2 = jLama.getJamSelesai();

                        if (start1 != null && end1 != null && start2 != null && end2 != null) {
                            if (start1.isBefore(end2) && start2.isBefore(end1)) {
                                String hariIndo = capitalize(jBaru.getHari());
                                ra.addFlashAttribute("error",
                                        "Gagal menambahkan: Jadwal kuliah bentrok pada hari " + hariIndo + " ("
                                                + start1.format(timeFormatter) + " - " + end1.format(timeFormatter)
                                                + ") dengan mata kuliah " + jLama.getKelas().getMataKuliah().getNamaMatkul()
                                                + " Kelas " + jLama.getKelas().getNamaKelas() + " ("
                                                + start2.format(timeFormatter) + " - " + end2.format(timeFormatter) + ").");
                                return "redirect:/mahasiswa";
                            }
                        }
                    }
                }
            }
        }

        // Cek bentrok Jadwal Ujian
        List<JadwalUjian> ujianBaruList = jadwalUjianRepo.findByKelas_IdKelas(idKelas);
        if (!listIdKelasDiambil.isEmpty() && !ujianBaruList.isEmpty()) {
            List<JadwalUjian> ujianLamaList = jadwalUjianRepo.findByKelas_IdKelasIn(listIdKelasDiambil);

            for (JadwalUjian uBaru : ujianBaruList) {
                for (JadwalUjian uLama : ujianLamaList) {
                    if (uBaru.getTanggal() != null && uLama.getTanggal() != null
                            && uBaru.getTanggal().isEqual(uLama.getTanggal())) {

                        // Jika jam ujian sama persis
                        if (uBaru.getJamMulai() != null && uLama.getJamMulai() != null
                                && uBaru.getJamMulai().equals(uLama.getJamMulai())) {
                            ra.addFlashAttribute("error",
                                    "Gagal menambahkan: Jadwal ujian bentrok pada tanggal "
                                            + uBaru.getTanggal() + " jam " + uBaru.getJamMulai()
                                            + " dengan mata kuliah " + uLama.getKelas().getMataKuliah().getNamaMatkul()
                                            + " Kelas " + uLama.getKelas().getNamaKelas() + ".");
                            return "redirect:/mahasiswa";
                        }
                    }
                }
            }
        }

        // Jika jadwal luang (tidak ada bentrok), simpan ke database
        PengambilanKelas baru = new PengambilanKelas();
        baru.setMahasiswa(mhs);
        baru.setKelas(kelas);
        pengambilanRepo.save(baru);

        ra.addFlashAttribute("success", "Mata kuliah " + kelas.getMataKuliah().getNamaMatkul() + " Kelas " + kelas.getNamaKelas() + " berhasil ditambahkan.");
        return "redirect:/mahasiswa";
    }

    @PostMapping("/mahasiswa/matkul/hapus")
    public String hapusMatkul(@RequestParam("idPengambilan") Integer idPengambilan,
                              HttpSession session, RedirectAttributes ra) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !user.getRole().equals("MAHASISWA")) return "redirect:/login";

        Mahasiswa mhs = mahasiswaRepo.findByUser_IdUser(user.getIdUser());
        PengambilanKelas p = pengambilanRepo.findById(idPengambilan).orElse(null);

        if (p == null || mhs == null || !p.getMahasiswa().getNpm().equals(mhs.getNpm())) {
            return "redirect:/mahasiswa";
        }

        if (anggotaRepo.existsByIdPengambilan(idPengambilan)) {
            ra.addFlashAttribute("error", "Mata kuliah tidak bisa dihapus karena kamu sudah terdaftar di kelompok tugas besar.");
            return "redirect:/mahasiswa";
        }

        pengambilanRepo.delete(p);
        ra.addFlashAttribute("success", "Mata kuliah berhasil dihapus.");
        return "redirect:/mahasiswa";
    }

    // =====================================================
    //  LIHAT JADWAL PRESENTASI
    // =====================================================
    @GetMapping("/mahasiswa/jadwal")
    public String lihatJadwal(HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !user.getRole().equals("MAHASISWA")) return "redirect:/login";

        Mahasiswa mhs = mahasiswaRepo.findByUser_IdUser(user.getIdUser());
        Integer idSemester = getIdSemester(session);
        isiTopbar(model, user, idSemester);

        List<Integer> idPengambilanList = new ArrayList<>();
        for (PengambilanKelas p : getPengambilan(mhs, idSemester)) {
            idPengambilanList.add(p.getIdPengambilan());
        }

        List<Integer> idKelompokList = new ArrayList<>();
        if (!idPengambilanList.isEmpty()) {
            for (AnggotaKelompok a : anggotaRepo.findByIdPengambilanIn(idPengambilanList)) {
                idKelompokList.add(a.getIdKelompok());
            }
        }

        List<JadwalPresentasiMahasiswa> jadwalList = new ArrayList<>();
        Map<Integer, String> anggotaPerKelompok = new HashMap<>();

        if (!idKelompokList.isEmpty()) {
            jadwalList = jadwalPresentasiRepo.findByKelompok_IdKelompokIn(idKelompokList);

            jadwalList.sort(Comparator.comparing((JadwalPresentasiMahasiswa j) -> j.getSlot().getTanggal())
                    .thenComparing(j -> j.getSlot().getJamMulai()));

            for (Integer idKelompok : idKelompokList) {
                List<String> nama = new ArrayList<>();
                for (AnggotaKelompok a : anggotaRepo.findByIdKelompok(idKelompok)) {
                    nama.add(a.getPengambilan().getMahasiswa().getUser().getNama());
                }
                anggotaPerKelompok.put(idKelompok, String.join(", ", nama));
            }
        }

        model.addAttribute("jadwalList", jadwalList);
        model.addAttribute("anggotaPerKelompok", anggotaPerKelompok);

        return "jadwalMahasiswa";
    }

    // =====================================================
    //  DAFTARKAN KELOMPOK TUGAS BESAR
    // =====================================================
    @GetMapping("/mahasiswa/kelompok")
    public String kelompok(@RequestParam(value = "idKelas", required = false) Integer idKelas,
                           HttpSession session, Model model) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !user.getRole().equals("MAHASISWA")) return "redirect:/login";

        Mahasiswa mhs = mahasiswaRepo.findByUser_IdUser(user.getIdUser());
        Integer idSemester = getIdSemester(session);
        isiTopbar(model, user, idSemester);

        List<Integer> idKelasDiambil = new ArrayList<>();
        for (PengambilanKelas p : getPengambilan(mhs, idSemester)) {
            idKelasDiambil.add(p.getKelas().getIdKelas());
        }

        Map<Integer, Kelas> kelasTubes = new LinkedHashMap<>();
        if (!idKelasDiambil.isEmpty()) {
            for (KelompokMahasiswa k : kelompokRepo.findByKelas_IdKelasIn(idKelasDiambil)) {
                kelasTubes.put(k.getKelas().getIdKelas(), k.getKelas());
            }
        }

        if (idKelas == null || !kelasTubes.containsKey(idKelas)) {
            idKelas = kelasTubes.isEmpty() ? null : kelasTubes.keySet().iterator().next();
        }

        List<KelompokMahasiswa> kelompokList = new ArrayList<>();
        Map<Integer, List<AnggotaKelompok>> anggotaPerKelompok = new HashMap<>();
        List<PengambilanKelas> calonAnggota = new ArrayList<>();

        if (idKelas != null) {
            kelompokList = kelompokRepo.findByKelas_IdKelas(idKelas);

            for (KelompokMahasiswa k : kelompokList) {
                anggotaPerKelompok.put(k.getIdKelompok(), new ArrayList<>());
            }

            for (AnggotaKelompok a : anggotaRepo.findByIdKelas(idKelas)) {
                if (anggotaPerKelompok.containsKey(a.getIdKelompok())) {
                    anggotaPerKelompok.get(a.getIdKelompok()).add(a);
                }
            }

            calonAnggota = pengambilanRepo.findByKelas_IdKelas(idKelas);
        }

        model.addAttribute("kelasTubes", new ArrayList<>(kelasTubes.values()));
        model.addAttribute("idKelasDipilih", idKelas);
        model.addAttribute("kelompokList", kelompokList);
        model.addAttribute("anggotaPerKelompok", anggotaPerKelompok);
        model.addAttribute("calonAnggota", calonAnggota);
        model.addAttribute("minAnggota", MIN_ANGGOTA);

        return "kelompokMahasiswa";
    }

    @PostMapping("/mahasiswa/kelompok/submit")
    public String submitKelompok(@RequestParam("idKelompok") Integer idKelompok,
                                 @RequestParam(value = "idPengambilan", required = false) List<Integer> idPengambilanList,
                                 HttpSession session, RedirectAttributes ra) {
        Users user = (Users) session.getAttribute("loggedUser");
        if (user == null || !user.getRole().equals("MAHASISWA")) return "redirect:/login";

        Mahasiswa mhs = mahasiswaRepo.findByUser_IdUser(user.getIdUser());
        KelompokMahasiswa kelompok = kelompokRepo.findById(idKelompok).orElse(null);
        if (mhs == null || kelompok == null) return "redirect:/mahasiswa/kelompok";

        Integer idKelas = kelompok.getKelas().getIdKelas();
        String balik = "redirect:/mahasiswa/kelompok?idKelas=" + idKelas;

        if (pengambilanRepo.findByMahasiswa_NpmAndKelas_IdKelas(mhs.getNpm(), idKelas) == null) {
            ra.addFlashAttribute("error", "Kamu tidak terdaftar di kelas ini.");
            return balik;
        }

        Set<Integer> anggotaBaru = new LinkedHashSet<>();
        if (idPengambilanList != null) anggotaBaru.addAll(idPengambilanList);

        for (Integer idPengambilan : anggotaBaru) {
            PengambilanKelas p = pengambilanRepo.findById(idPengambilan).orElse(null);
            if (p == null || !p.getKelas().getIdKelas().equals(idKelas)) {
                ra.addFlashAttribute("error", "Ada mahasiswa yang tidak terdaftar di kelas ini.");
                ra.addFlashAttribute("bukaKelompok", idKelompok);
                return balik;
            }
        }

        boolean adaPindah = false;
        for (Integer idPengambilan : anggotaBaru) {
            AnggotaKelompok lama = anggotaRepo.findByIdPengambilan(idPengambilan);
            if (lama != null) {
                if (lama.getIdKelompok().equals(idKelompok)) {
                    continue;
                }
                anggotaRepo.delete(lama);
                adaPindah = true;
            }

            AnggotaKelompok baru = new AnggotaKelompok();
            baru.setIdKelompok(idKelompok);
            baru.setIdPengambilan(idPengambilan);
            baru.setIdKelas(idKelas);
            anggotaRepo.save(baru);
        }

        int totalSekarang = anggotaRepo.findByIdKelompok(idKelompok).size();
        if (totalSekarang < MIN_ANGGOTA) {
            ra.addFlashAttribute("error", "Perhatian: Kelompok ini baru memiliki " + totalSekarang + " anggota (Minimal " + MIN_ANGGOTA + " anggota).");
        } else if (adaPindah) {
            ra.addFlashAttribute("success", "Berhasil memperbarui dan memindahkan anggota ke " + kelompok.getNamaKelompok() + ".");
        } else {
            ra.addFlashAttribute("success", "Anggota berhasil disimpan ke " + kelompok.getNamaKelompok() + ".");
        }

        return balik;
    }

    // =====================================================
    //  HELPER
    // =====================================================

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

    private List<PengambilanKelas> getPengambilan(Mahasiswa mhs, Integer idSemester) {
        if (mhs == null || idSemester == null) return new ArrayList<>();
        return pengambilanRepo.findByMahasiswa_NpmAndKelas_Semester_IdSemester(mhs.getNpm(), idSemester);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
