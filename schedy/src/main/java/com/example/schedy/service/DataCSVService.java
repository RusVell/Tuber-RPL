package com.example.schedy.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.schedy.repository.UsersRepository;
import com.example.schedy.service.AkademicDataService.AkunForm;
import com.example.schedy.service.AkademicDataService.KuliahForm;
import com.example.schedy.service.AkademicDataService.MatkulForm;
import com.example.schedy.service.AkademicDataService.UjianForm;

import jakarta.transaction.Transactional;

@Service
public class DataCSVService {
    private final AkademicDataService akademicDataService;

    public DataCSVService(AkademicDataService akademicDataService) {
        this.akademicDataService = akademicDataService;
    }

    @Transactional
    public void importJadwalKuliah(MultipartFile file, Integer semesterID) {
        try (BufferedReader filReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(filReader,
                        CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get())) {
            for (CSVRecord row : csvParser) {
                KuliahForm form = new KuliahForm(row.get("kode_matkul"), row.get("nama_kelas"),
                        row.get("hari").toUpperCase(), bacaJam(row.get("jam_mulai")),
                        bacaJam(row.get("jam_selesai")), row.get("ruangan"));
                akademicDataService.tambahKuliah(form, semesterID);
            }

        } catch (IOException e) {
            throw new RuntimeException("Gagal membaca CSV", e);
        }

    }

    @Transactional
    public void importAkun(MultipartFile file) {
        try (BufferedReader filReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(filReader,
                        CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get())) {
            for (CSVRecord row : csvParser) {
                AkunForm form = new AkunForm(row.get("nama"), row.get("NPM/NIDN"), row.get("role"));
                akademicDataService.tambahAkun(form);
            }

        } catch (IOException e) {
            throw new RuntimeException("Gagal membaca CSV", e);
        }

    }

    @Transactional
    public void importmatkul(MultipartFile file, Integer semesterId) {
        try (BufferedReader filReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(filReader,
                        CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get())) {
            for (CSVRecord row : csvParser) {
                MatkulForm form = new MatkulForm(row.get("kode_matkul"), row.get("nama_matkul"),
                        Integer.parseInt(row.get("sks")), row.get("nama_kelas"), row.get("nip_dosen"));
                akademicDataService.tambahMatkul(form, semesterId);
            }

        } catch (IOException e) {
            throw new RuntimeException("Gagal membaca CSV", e);
        }

    }

    @Transactional
    public void importjadwalujian(MultipartFile file, Integer semesterId) {
        try (BufferedReader filReader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(filReader,
                        CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get())) {
            for (CSVRecord row : csvParser) {
                UjianForm form = new UjianForm(row.get("kode_matkul"), row.get("nama_kelas"), row.get("jenis_ujian"),
                        LocalDate.parse(row.get("tanggal")), bacaJam(row.get("jam_mulai")),
                        bacaJam(row.get("jam_selesai")), row.get("ruangan"));
                akademicDataService.tambahJadwalUjian(form, semesterId);
            }

        } catch (IOException e) {
            throw new RuntimeException("Gagal membaca CSV", e);
        }

    }

    private static final DateTimeFormatter JAM = DateTimeFormatter.ofPattern("H:mm[:ss]");

    private LocalTime bacaJam(String teks) {
        return LocalTime.parse(teks, JAM);
    }
}
