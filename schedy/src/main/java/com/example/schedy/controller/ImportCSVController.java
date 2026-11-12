package com.example.schedy.controller;

import java.io.UncheckedIOException;
import java.time.DateTimeException;
import java.util.Locale;
import java.util.Set;

import com.example.schedy.entity.Users;
import com.example.schedy.repository.SemesterRepository;
import com.example.schedy.service.DataCSVService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImportCSVController {
    private static final Logger log = LoggerFactory.getLogger(ImportCSVController.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> TYPES = Set.of("mahasiswa", "mata-kuliah", "jadwal-kuliah", "jadwal-ujian");

    private final DataCSVService csvService;
    private final SemesterRepository semesters;

    public ImportCSVController(DataCSVService csvService, SemesterRepository semesters) {
        this.csvService = csvService;
        this.semesters = semesters;
    }

    public record ImportResult(boolean success, String message) {
    }

    @PostMapping("/admin/import-csv")
    public ResponseEntity<ImportResult> importCsv(HttpSession session,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) Integer idSemester) {
        if (!(session.getAttribute("loggedUser") instanceof Users user)) {
            return failure(HttpStatus.UNAUTHORIZED, "Sesi berakhir. Silakan login kembali.");
        }
        if (!"ADMIN".equals(user.getRole())) {
            return failure(HttpStatus.FORBIDDEN, "Hanya admin yang dapat mengimpor CSV.");
        }
        if (type == null || !TYPES.contains(type)) {
            return failure(HttpStatus.BAD_REQUEST, "Jenis data CSV tidak valid.");
        }
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null
                || !file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            return failure(HttpStatus.BAD_REQUEST, "Pilih file CSV berisi data.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return fileTooLarge();
        }
        // Gunakan semester yang tampil saat halaman dibuka, termasuk jika sesi berubah di tab lain.
        if (!"mahasiswa".equals(type) && (idSemester == null || !semesters.existsById(idSemester))) {
            return failure(HttpStatus.BAD_REQUEST, "Pilih semester yang valid terlebih dahulu.");
        }

        try {
            switch (type) {
                case "mahasiswa" -> csvService.importAkun(file);
                case "mata-kuliah" -> csvService.importmatkul(file, idSemester);
                case "jadwal-kuliah" -> csvService.importJadwalKuliah(file, idSemester);
                case "jadwal-ujian" -> csvService.importjadwalujian(file, idSemester);
                default -> throw new IllegalArgumentException("Jenis data CSV tidak valid.");
            }
            return ResponseEntity.ok(new ImportResult(true, "Impor CSV berhasil."));
        } catch (DateTimeException e) {
            return failure(HttpStatus.BAD_REQUEST,
                    "Format tanggal atau jam tidak valid. Gunakan tanggal YYYY-MM-DD dan jam HH:mm.");
        } catch (IllegalArgumentException e) {
            return failure(HttpStatus.BAD_REQUEST, "Impor CSV gagal: " + e.getMessage());
        } catch (UncheckedIOException e) {
            return failure(HttpStatus.BAD_REQUEST, "CSV tidak dapat dibaca. Periksa format dan isi file.");
        } catch (DataAccessException e) {
            log.error("Gagal menyimpan impor CSV jenis {}", type, e);
            return failure(HttpStatus.CONFLICT,
                    "Data CSV tidak dapat disimpan. Periksa duplikasi, panjang kolom, dan referensi data.");
        } catch (MaxUploadSizeExceededException e) {
            return fileTooLarge();
        } catch (RuntimeException e) {
            log.error("Gagal memproses impor CSV jenis {}", type, e);
            return failure(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Impor CSV gagal diproses. Periksa file atau coba kembali.");
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ImportResult> fileTooLarge() {
        return failure(HttpStatus.PAYLOAD_TOO_LARGE, "Ukuran file CSV maksimum 10 MB.");
    }

    private ResponseEntity<ImportResult> failure(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ImportResult(false, message));
    }
}
