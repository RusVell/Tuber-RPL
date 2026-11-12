package com.example.schedy.service;

import com.example.schedy.entity.*;
import com.example.schedy.repository.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDate;

@Service
public class AkademicDataService {
    public static final List<String> ROLES = List.of("MAHASISWA", "DOSEN", "ADMIN");
    public static final List<String> DAYS = List.of("SENIN", "SELASA", "RABU", "KAMIS", "JUMAT", "SABTU", "MINGGU");

    public record AkunForm(String nama, String nomor_induk, String role) {
    }

    public record MatkulForm(String kode_matkul, String nama_matkul, Integer sks, String nama_kelas, String nip_dosen) {
    }

    public record KuliahForm(String kode_matkul, String nama_kelas, String hari,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime jam_mulai,
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime jam_selesai, String ruangan) {
    }

    public record UjianForm(
            String kode_matkul,
            String nama_kelas,
            String jenis_ujian,
            LocalDate tanggal,
            LocalTime jam_mulai,
            LocalTime jam_selesai,
            String ruangan) {
    }

    public record AccountCredentials(String username, String password) {
    }

    private final UsersRepository users;
    private final MahasiswaRepository mahasiswa;
    private final DosenRepository dosen;
    private final AdminRepository admins;
    private final SemesterRepository semesters;
    private final MataKuliahRepository courses;
    private final KelasRepository classes;
    private final JadwalKuliahRepository schedules;
    private final JadwalUjianRepository ujianRepository;

    public AkademicDataService(UsersRepository users, MahasiswaRepository mahasiswa, DosenRepository dosen,
            AdminRepository admins, SemesterRepository semesters, MataKuliahRepository courses,
            KelasRepository classes, JadwalKuliahRepository schedules, JadwalUjianRepository ujianRepository) {
        this.users = users;
        this.mahasiswa = mahasiswa;
        this.dosen = dosen;
        this.admins = admins;
        this.semesters = semesters;
        this.courses = courses;
        this.classes = classes;
        this.schedules = schedules;
        this.ujianRepository = ujianRepository;
    }

    @Transactional
    public AccountCredentials tambahAkun(AkunForm form) {
        String number = form.nomor_induk().strip();
        if (users.existsByUsername(number) || mahasiswa.existsById(number) || dosen.existsById(number))
            throw new IllegalArgumentException("NPM/NIDN sudah digunakan oleh akun lain.");
        String password = "123";
        Users user = new Users();
        user.setNama(form.nama().strip());
        user.setUsername(number);
        user.setRole(form.role());
        user.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        users.save(user);
        switch (form.role()) {
            case "MAHASISWA" -> {
                Mahasiswa m = new Mahasiswa();
                m.setNpm(number);
                m.setUser(user);
                mahasiswa.save(m);
            }
            case "DOSEN" -> {
                Dosen d = new Dosen();
                d.setNidn(number);
                d.setUser(user);
                dosen.save(d);
            }
            case "ADMIN" -> {
                Admin a = new Admin();
                a.setUser(user);
                admins.save(a);
            }
            default -> throw new IllegalArgumentException("Role tidak valid.");
        }
        return new AccountCredentials(number, password);
    }

    @Transactional
    public void tambahMatkul(MatkulForm form, Integer semesterId) {
        Semester semester = semesterId == null ? null : semesters.findById(semesterId).orElse(null);
        if (semester == null)
            throw new IllegalArgumentException("Pilih semester terlebih dahulu.");
        Dosen lecturer = dosen.findById(form.nip_dosen().strip()).orElseThrow(
                () -> new IllegalArgumentException(
                        "NIP/NIDN dosen belum terdaftar. Tambahkan akun dosen terlebih dahulu."));
        String code = form.kode_matkul().strip();
        if (!cariKelas(code, form.nama_kelas().strip(), semesterId).isEmpty())
            throw new IllegalArgumentException("Kelas mata kuliah ini sudah terdaftar pada semester yang dipilih.");
        List<MataKuliah> matches = courses.findByKodeMatkulIgnoreCase(code);
        if (matches.size() > 1)
            throw new IllegalArgumentException("Kode mata kuliah tidak unik. Periksa data mata kuliah.");
        MataKuliah course;
        if (matches.isEmpty()) {
            course = new MataKuliah();
            course.setKodeMatkul(code);
            course.setNamaMatkul(form.nama_matkul().strip());
            course.setSks(form.sks());
            courses.save(course);
        } else {
            course = matches.getFirst();
            if (!form.nama_matkul().strip().equalsIgnoreCase(course.getNamaMatkul())
                    || !form.sks().equals(course.getSks()))
                throw new IllegalArgumentException("Kode mata kuliah sudah ada dengan nama atau SKS berbeda.");
        }
        Kelas kelas = new Kelas();
        kelas.setNamaKelas(form.nama_kelas().strip());
        kelas.setMataKuliah(course);
        kelas.setSemester(semester);
        kelas.setDosen(lecturer);
        classes.save(kelas);
    }

    public List<Kelas> cariKelas(String code, String name, Integer semesterId) {
        if (semesterId == null)
            return List.of();
        return classes.findBySemester_IdSemester(semesterId).stream().filter(k -> k.getMataKuliah() != null
                && code.equalsIgnoreCase(k.getMataKuliah().getKodeMatkul()) && name.equalsIgnoreCase(k.getNamaKelas()))
                .toList();
    }

    @Transactional
    public void tambahKuliah(KuliahForm form, Integer semesterId) {
        List<Kelas> matches = cariKelas(form.kode_matkul().strip(), form.nama_kelas().strip(), semesterId);
        if (matches.size() != 1)
            throw new IllegalArgumentException(
                    "Kode mata kuliah dan nama kelas harus cocok dengan satu kelas pada semester yang dipilih.");
        Kelas kelas = matches.getFirst();
        boolean overlap = schedules.findByKelas_IdKelas(kelas.getIdKelas()).stream()
                .anyMatch(j -> form.hari().equals(j.getHari()) && j.getJamMulai() != null && j.getJamSelesai() != null
                        && form.jam_mulai().isBefore(j.getJamSelesai()) && form.jam_selesai().isAfter(j.getJamMulai()));
        if (overlap)
            throw new IllegalArgumentException(
                    "Jadwal kelas ini sudah ada atau bertabrakan pada hari dan jam tersebut.");
        JadwalKuliah jadwal = new JadwalKuliah();
        jadwal.setKelas(kelas);
        jadwal.setHari(form.hari());
        jadwal.setJamMulai(form.jam_mulai());
        jadwal.setJamSelesai(form.jam_selesai());
        jadwal.setRuangan(form.ruangan().strip());
        schedules.save(jadwal);
    }

    @Transactional
    public void tambahJadwalUjian(UjianForm form, Integer semesterId) {

        if (form.kode_matkul() == null || form.nama_kelas() == null
                || form.jenis_ujian() == null || form.ruangan() == null
                || form.tanggal() == null || form.jam_mulai() == null
                || form.jam_selesai() == null) {
            throw new IllegalArgumentException("Data ujian belum lengkap.");
        }

        if (form.kode_matkul().isBlank() || form.nama_kelas().isBlank()
                || form.jenis_ujian().isBlank() || form.ruangan().isBlank()
                || form.jenis_ujian().strip().length() > 50
                || form.ruangan().strip().length() > 100) {
            throw new IllegalArgumentException("Data ujian tidak valid.");
        }

        if (!form.jam_selesai().isAfter(form.jam_mulai())) {
            throw new IllegalArgumentException("Jam selesai harus setelah jam mulai.");
        }

        List<Kelas> daftarKelas = cariKelas(
                form.kode_matkul().strip(),
                form.nama_kelas().strip(),
                semesterId);

        if (daftarKelas.size() != 1) {
            throw new IllegalArgumentException("Kelas tidak ditemukan pada semester ini.");
        }

        Kelas kelas = daftarKelas.get(0);

        for (JadwalUjian lama : ujianRepository.findByKelas_IdKelas(kelas.getIdKelas())) {
            if (form.tanggal().equals(lama.getTanggal())
                    && lama.getJamMulai() != null
                    && lama.getJamSelesai() != null
                    && form.jam_mulai().isBefore(lama.getJamSelesai())
                    && form.jam_selesai().isAfter(lama.getJamMulai())) {
                throw new IllegalArgumentException("Jadwal ujian bertabrakan.");
            }
        }

        JadwalUjian ujian = new JadwalUjian();
        ujian.setKelas(kelas);
        ujian.setJenisUjian(form.jenis_ujian().strip());
        ujian.setTanggal(form.tanggal());
        ujian.setJamMulai(form.jam_mulai());
        ujian.setJamSelesai(form.jam_selesai());
        ujian.setRuangan(form.ruangan().strip());

        ujianRepository.save(ujian);
    }

}
