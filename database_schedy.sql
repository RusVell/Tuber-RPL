-- ==============================================================================
-- SCHEDY DATABASE SCRIPT (SQL Server / SSMS)
-- Versi Pembersihan Total (Drop Semua FK dan Semua UQ Constraints Sebelum Drop Tabel)
-- ==============================================================================

-- 1. DROP SEMUA FOREIGN KEY CONSTRAINTS
DECLARE @sqlFK NVARCHAR(MAX) = N'';
SELECT @sqlFK += N'ALTER TABLE ' + QUOTENAME(s.name) + '.' + QUOTENAME(t.name) 
    + ' DROP CONSTRAINT ' + QUOTENAME(fk.name) + ';' + CHAR(13)
FROM sys.foreign_keys fk
INNER JOIN sys.tables t ON fk.parent_object_id = t.object_id
INNER JOIN sys.schemas s ON t.schema_id = s.schema_id;

IF LEN(@sqlFK) > 0
    EXEC sp_executesql @sqlFK;
GO

-- 2. DROP SEMUA UNIQUE CONSTRAINTS (Biar tidak ada bentrok nama UQ seperti uq_pengambilan)
DECLARE @sqlUQ NVARCHAR(MAX) = N'';
SELECT @sqlUQ += N'ALTER TABLE ' + QUOTENAME(s.name) + '.' + QUOTENAME(t.name) 
    + ' DROP CONSTRAINT ' + QUOTENAME(kc.name) + ';' + CHAR(13)
FROM sys.key_constraints kc
INNER JOIN sys.tables t ON kc.parent_object_id = t.object_id
INNER JOIN sys.schemas s ON t.schema_id = s.schema_id
WHERE kc.type = 'UQ';

IF LEN(@sqlUQ) > 0
    EXEC sp_executesql @sqlUQ;
GO

-- 3. DROP SEMUA TABEL TERKAIT
IF OBJECT_ID('jadwal_presentasi_mahasiswa', 'U') IS NOT NULL DROP TABLE jadwal_presentasi_mahasiswa;
IF OBJECT_ID('slot_presentasi', 'U') IS NOT NULL DROP TABLE slot_presentasi;
IF OBJECT_ID('anggota_kelompok', 'U') IS NOT NULL DROP TABLE anggota_kelompok;
IF OBJECT_ID('kelompok_mahasiswa', 'U') IS NOT NULL DROP TABLE kelompok_mahasiswa;
IF OBJECT_ID('jadwal_ujian', 'U') IS NOT NULL DROP TABLE jadwal_ujian;
IF OBJECT_ID('jadwal_kuliah', 'U') IS NOT NULL DROP TABLE jadwal_kuliah;
IF OBJECT_ID('pengambilan_kelas', 'U') IS NOT NULL DROP TABLE pengambilan_kelas;
IF OBJECT_ID('kelas', 'U') IS NOT NULL DROP TABLE kelas;
IF OBJECT_ID('semester', 'U') IS NOT NULL DROP TABLE semester;
IF OBJECT_ID('mata_kuliah', 'U') IS NOT NULL DROP TABLE mata_kuliah;
IF OBJECT_ID('dosen', 'U') IS NOT NULL DROP TABLE dosen;
IF OBJECT_ID('mahasiswa', 'U') IS NOT NULL DROP TABLE mahasiswa;
IF OBJECT_ID('admin', 'U') IS NOT NULL DROP TABLE admin;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
-- Drop tabel sisa demo jika ada
IF OBJECT_ID('pengguna', 'U') IS NOT NULL DROP TABLE pengguna;
IF OBJECT_ID('terdaftar', 'U') IS NOT NULL DROP TABLE terdaftar;
IF OBJECT_ID('mengajar', 'U') IS NOT NULL DROP TABLE mengajar;
IF OBJECT_ID('bimbingan', 'U') IS NOT NULL DROP TABLE bimbingan;
IF OBJECT_ID('pembimbing', 'U') IS NOT NULL DROP TABLE pembimbing;
IF OBJECT_ID('permintaan_jadwal', 'U') IS NOT NULL DROP TABLE permintaan_jadwal;
IF OBJECT_ID('tugas_akhir', 'U') IS NOT NULL DROP TABLE tugas_akhir;
IF OBJECT_ID('notifikasi', 'U') IS NOT NULL DROP TABLE notifikasi;
GO

-- ==============================================================================
-- 4. CREATE TABLES (Sesuai Bab 7.2 Dokumen Teknis Schedy)
-- ==============================================================================

-- Tabel Users
CREATE TABLE users (
    id_user INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    nama VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'DOSEN', 'MAHASISWA')),
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT uq_users_username UNIQUE (username)
);

-- Tabel Admin
CREATE TABLE admin (
    id_admin INT IDENTITY(1,1) PRIMARY KEY,
    id_user INT NOT NULL,
    CONSTRAINT uq_admin_user UNIQUE (id_user),
    CONSTRAINT fk_admin_user FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE
);

-- Tabel Mahasiswa
CREATE TABLE mahasiswa (
    npm VARCHAR(20) PRIMARY KEY,
    id_user INT NOT NULL,
    CONSTRAINT uq_mahasiswa_user UNIQUE (id_user),
    CONSTRAINT fk_mahasiswa_user FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE
);

-- Tabel Dosen
CREATE TABLE dosen (
    nidn VARCHAR(20) PRIMARY KEY,
    id_user INT NOT NULL,
    CONSTRAINT uq_dosen_user UNIQUE (id_user),
    CONSTRAINT fk_dosen_user FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE
);

-- Tabel Mata Kuliah
CREATE TABLE mata_kuliah (
    id_matkul INT IDENTITY(1,1) PRIMARY KEY,
    kode_matkul VARCHAR(20) NOT NULL,
    nama_matkul VARCHAR(100) NOT NULL,
    sks INT NOT NULL DEFAULT 3,
    CONSTRAINT uq_matkul_kode UNIQUE (kode_matkul)
);

-- Tabel Semester
CREATE TABLE semester (
    id_semester INT IDENTITY(1,1) PRIMARY KEY,
    tahun_ajaran VARCHAR(9) NOT NULL,
    jenis_semester VARCHAR(10) NOT NULL CHECK (jenis_semester IN ('Ganjil', 'Genap')),
    status_aktif BIT NOT NULL DEFAULT 0,
    CONSTRAINT uq_semester_tahun_jenis UNIQUE (tahun_ajaran, jenis_semester)
);

-- Tabel Kelas
CREATE TABLE kelas (
    id_kelas INT IDENTITY(1,1) PRIMARY KEY,
    nama_kelas VARCHAR(20) NOT NULL,
    id_matkul INT NOT NULL,
    id_semester INT NOT NULL,
    nidn VARCHAR(20) NULL,
    CONSTRAINT fk_kelas_matkul FOREIGN KEY (id_matkul) REFERENCES mata_kuliah(id_matkul) ON DELETE CASCADE,
    CONSTRAINT fk_kelas_semester FOREIGN KEY (id_semester) REFERENCES semester(id_semester) ON DELETE CASCADE,
    CONSTRAINT fk_kelas_dosen FOREIGN KEY (nidn) REFERENCES dosen(nidn)
);

-- Tabel Pengambilan Kelas (KRS)
-- Menggunakan anonymous unique constraint agar nama tidak pernah bentrok
CREATE TABLE pengambilan_kelas (
    id_pengambilan INT IDENTITY(1,1) PRIMARY KEY,
    npm VARCHAR(20) NOT NULL,
    id_kelas INT NOT NULL,
    UNIQUE (npm, id_kelas),
    CONSTRAINT fk_pengambilan_mhs FOREIGN KEY (npm) REFERENCES mahasiswa(npm) ON DELETE CASCADE,
    CONSTRAINT fk_pengambilan_kelas FOREIGN KEY (id_kelas) REFERENCES kelas(id_kelas) ON DELETE CASCADE
);

-- Tabel Jadwal Kuliah Reguler
CREATE TABLE jadwal_kuliah (
    id_kuliah INT IDENTITY(1,1) PRIMARY KEY,
    id_kelas INT NOT NULL,
    hari VARCHAR(10) NOT NULL CHECK (hari IN ('SENIN', 'SELASA', 'RABU', 'KAMIS', 'JUMAT', 'SABTU', 'MINGGU')),
    jam_mulai TIME NOT NULL,
    jam_selesai TIME NOT NULL,
    ruangan VARCHAR(100) NULL,
    CONSTRAINT fk_jadwal_kuliah_kelas FOREIGN KEY (id_kelas) REFERENCES kelas(id_kelas) ON DELETE CASCADE
);

-- Tabel Jadwal Ujian
CREATE TABLE jadwal_ujian (
    id_ujian INT IDENTITY(1,1) PRIMARY KEY,
    id_kelas INT NOT NULL,
    tanggal DATE NOT NULL,
    jam_mulai TIME NOT NULL,
    jenis_ujian VARCHAR(50) NULL, //opsional, kalau mau ditambahkan
    jam_selesai TIME NULL,  //opsional
    ruangan VARCHAR(100) NULL,  //opsional
    CONSTRAINT fk_jadwal_ujian_kelas FOREIGN KEY (id_kelas) REFERENCES kelas(id_kelas) ON DELETE CASCADE
);

-- Tabel Kelompok Mahasiswa
CREATE TABLE kelompok_mahasiswa (
    id_kelompok INT IDENTITY(1,1) PRIMARY KEY,
    id_kelas INT NOT NULL,
    nama_kelompok VARCHAR(100) NOT NULL,
    CONSTRAINT fk_kelompok_kelas FOREIGN KEY (id_kelas) REFERENCES kelas(id_kelas) ON DELETE CASCADE
);

-- Tabel Anggota Kelompok
CREATE TABLE anggota_kelompok (
    id_kelompok INT NOT NULL,
    id_pengambilan INT NOT NULL,
    id_kelas INT NULL,
    PRIMARY KEY (id_kelompok, id_pengambilan),
    UNIQUE (id_pengambilan),
    CONSTRAINT fk_anggota_kelompok FOREIGN KEY (id_kelompok) REFERENCES kelompok_mahasiswa(id_kelompok) ON DELETE CASCADE,
    CONSTRAINT fk_anggota_pengambilan FOREIGN KEY (id_pengambilan) REFERENCES pengambilan_kelas(id_pengambilan)
);

-- Tabel Slot Presentasi
CREATE TABLE slot_presentasi (
    id_slot INT IDENTITY(1,1) PRIMARY KEY,
    id_kelas INT NOT NULL,
    tanggal DATE NOT NULL,
    jam_mulai TIME NOT NULL,
    durasi_menit INT NOT NULL,
    lokasi VARCHAR(100) NULL,
    CONSTRAINT fk_slot_kelas FOREIGN KEY (id_kelas) REFERENCES kelas(id_kelas) ON DELETE CASCADE
);

-- Tabel Jadwal Presentasi Mahasiswa
CREATE TABLE jadwal_presentasi_mahasiswa (
    id_presentasi INT IDENTITY(1,1) PRIMARY KEY,
    id_kelompok INT NOT NULL,
    id_slot INT NOT NULL,
    UNIQUE (id_kelompok, id_slot),
    CONSTRAINT fk_pres_kelompok FOREIGN KEY (id_kelompok) REFERENCES kelompok_mahasiswa(id_kelompok) ON DELETE CASCADE,
    CONSTRAINT fk_pres_slot FOREIGN KEY (id_slot) REFERENCES slot_presentasi(id_slot)
);
GO

-- ==============================================================================
-- 5. INSERT DUMMY DATA LENGKAP
-- ==============================================================================

-- A. USERS & ROLES
INSERT INTO users (username, nama, role, password_hash) VALUES
('6182301029', 'Cavell Gunawan', 'MAHASISWA', '123'),
('6182401049', 'Kendrick Hansell Setiawan', 'MAHASISWA', '123'),
('6182401019', 'Efrem Andra Winata', 'MAHASISWA', '123'),
('6182401042', 'Rakha Alfath Rizqia', 'MAHASISWA', '123'),
('steven', 'Steven', 'MAHASISWA', '123'),
('aliong', 'Aliong', 'MAHASISWA', '123'),
('budi', 'Budi Santoso', 'MAHASISWA', '123'),
('agus', 'Agus, M.T.', 'DOSEN', '123'),
('bambang', 'Bambang Sugiantoro, Ph.D.', 'DOSEN', '123'),
('admin', 'Administrator Schedy', 'ADMIN', '123');

-- B. MAPPING PERAN
INSERT INTO admin (id_user)
SELECT id_user FROM users WHERE username = 'admin';

INSERT INTO dosen (nidn, id_user)
SELECT '0412088001', id_user FROM users WHERE username = 'agus';

INSERT INTO dosen (nidn, id_user)
SELECT '0425097502', id_user FROM users WHERE username = 'bambang';

INSERT INTO mahasiswa (npm, id_user)
SELECT '6182301029', id_user FROM users WHERE username = '6182301029';

INSERT INTO mahasiswa (npm, id_user)
SELECT '6182401049', id_user FROM users WHERE username = '6182401049';

INSERT INTO mahasiswa (npm, id_user)
SELECT '6182401019', id_user FROM users WHERE username = '6182401019';

INSERT INTO mahasiswa (npm, id_user)
SELECT '6182401042', id_user FROM users WHERE username = '6182401042';

INSERT INTO mahasiswa (npm, id_user)
SELECT '6182401099', id_user FROM users WHERE username = 'steven';

INSERT INTO mahasiswa (npm, id_user)
SELECT '6182401088', id_user FROM users WHERE username = 'aliong';

INSERT INTO mahasiswa (npm, id_user)
SELECT '6182401077', id_user FROM users WHERE username = 'budi';

-- C. SEMESTER
INSERT INTO semester (tahun_ajaran, jenis_semester, status_aktif) VALUES
('2024/2025', 'Ganjil', 1),
('2024/2025', 'Genap', 0),
('2025/2026', 'Ganjil', 0);

-- D. MATA KULIAH
INSERT INTO mata_kuliah (kode_matkul, nama_matkul, sks) VALUES
('AIF220', 'Dasar Pemrograman', 4),
('IF2101', 'Pemrograman Berbasis Objek', 3),
('IF2203', 'Basis Data', 3),
('IF3102', 'Rekayasa Perangkat Lunak', 4),
('IF3201', 'Kecerdasan Buatan', 3);

-- E. KELAS
DECLARE @idSem INT = (SELECT TOP 1 id_semester FROM semester WHERE tahun_ajaran = '2024/2025' AND jenis_semester = 'Ganjil');
DECLARE @mkDaspro INT = (SELECT id_matkul FROM mata_kuliah WHERE kode_matkul = 'AIF220');
DECLARE @mkPBO INT = (SELECT id_matkul FROM mata_kuliah WHERE kode_matkul = 'IF2101');
DECLARE @mkBasdat INT = (SELECT id_matkul FROM mata_kuliah WHERE kode_matkul = 'IF2203');
DECLARE @mkRPL INT = (SELECT id_matkul FROM mata_kuliah WHERE kode_matkul = 'IF3102');

INSERT INTO kelas (nama_kelas, id_matkul, id_semester, nidn) VALUES
('A', @mkDaspro, @idSem, '0412088001'),
('B', @mkDaspro, @idSem, '0412088001'),
('A', @mkPBO, @idSem, '0425097502'),
('B', @mkBasdat, @idSem, '0412088001'),
('A', @mkRPL, @idSem, '0425097502');

-- F. JADWAL KULIAH REGULER
DECLARE @klsDasproA INT = (SELECT id_kelas FROM kelas WHERE nama_kelas = 'A' AND id_matkul = @mkDaspro);
DECLARE @klsPBO_A INT = (SELECT id_kelas FROM kelas WHERE nama_kelas = 'A' AND id_matkul = @mkPBO);
DECLARE @klsBasdatB INT = (SELECT id_kelas FROM kelas WHERE nama_kelas = 'B' AND id_matkul = @mkBasdat);
DECLARE @klsRPL_A INT = (SELECT id_kelas FROM kelas WHERE nama_kelas = 'A' AND id_matkul = @mkRPL);

INSERT INTO jadwal_kuliah (id_kelas, hari, jam_mulai, jam_selesai, ruangan) VALUES
(@klsPBO_A, 'SENIN', '08:00', '10:30', '9121'),
(@klsDasproA, 'SENIN', '08:00', '09:00', 'Lab Kom 1'),
(@klsRPL_A, 'SELASA', '13:00', '15:30', '9122'),
(@klsBasdatB, 'RABU', '10:00', '12:30', '9123');

-- G. JADWAL UJIAN
INSERT INTO jadwal_ujian (id_kelas, tanggal, jam_mulai, ruangan) VALUES
(@klsPBO_A, '2026-12-14', '08:00', '9121'),
(@klsDasproA, '2026-12-04', '08:00', 'Lab Kom 1'),
(@klsRPL_A, '2026-12-16', '13:00', '9122'),
(@klsBasdatB, '2026-12-18', '10:00', '9123');

-- H. PENGAMBILAN KELAS (KRS)
INSERT INTO pengambilan_kelas (npm, id_kelas) VALUES
('6182301029', @klsDasproA),
('6182301029', @klsPBO_A),
('6182301029', @klsBasdatB),
('6182301029', @klsRPL_A),
('6182401049', @klsDasproA),
('6182401019', @klsDasproA),
('6182401042', @klsDasproA),
('6182401099', @klsDasproA),
('6182401088', @klsDasproA),
('6182401077', @klsDasproA);

-- I. KELOMPOK MAHASISWA
INSERT INTO kelompok_mahasiswa (id_kelas, nama_kelompok) VALUES
(@klsDasproA, 'Kelompok 1 - ABC'),
(@klsDasproA, 'Kelompok 2 - BCD');

-- J. ANGGOTA KELOMPOK
DECLARE @idKel1 INT = (SELECT id_kelompok FROM kelompok_mahasiswa WHERE nama_kelompok = 'Kelompok 1 - ABC' AND id_kelas = @klsDasproA);
DECLARE @idKel2 INT = (SELECT id_kelompok FROM kelompok_mahasiswa WHERE nama_kelompok = 'Kelompok 2 - BCD' AND id_kelas = @klsDasproA);

-- Kelompok 1: Kendrick, Efrem Andra, Cavell Gunawan
INSERT INTO anggota_kelompok (id_kelompok, id_pengambilan, id_kelas)
SELECT @idKel1, id_pengambilan, @klsDasproA
FROM pengambilan_kelas
WHERE npm IN ('6182401049', '6182401019', '6182301029') AND id_kelas = @klsDasproA;

-- Kelompok 2: Rakha, Steven
INSERT INTO anggota_kelompok (id_kelompok, id_pengambilan, id_kelas)
SELECT @idKel2, id_pengambilan, @klsDasproA
FROM pengambilan_kelas
WHERE npm IN ('6182401042', '6182401099') AND id_kelas = @klsDasproA;

-- K. SLOT PRESENTASI
INSERT INTO slot_presentasi (id_kelas, tanggal, jam_mulai, durasi_menit, lokasi) VALUES
(@klsDasproA, '2026-10-08', '08:00', 60, 'Lab Kom'),
(@klsDasproA, '2026-10-08', '09:00', 60, 'Gedung 10');

-- L. JADWAL PRESENTASI MAHASISWA
DECLARE @idSlotLab INT = (SELECT TOP 1 id_slot FROM slot_presentasi WHERE id_kelas = @klsDasproA AND lokasi = 'Lab Kom');

INSERT INTO jadwal_presentasi_mahasiswa (id_kelompok, id_slot) VALUES
(@idKel1, @idSlotLab);
GO
