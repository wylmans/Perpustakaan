package controller;

import database.DatabaseHelper;
import model.Peminjam;

import java.sql.*;
import java.time.LocalDate;

/**
 * PeminjamanController — mengelola operasi CRUD untuk data peminjaman buku.
 *
 * Pola koneksi mengikuti BookController: Connection diterima sebagai parameter
 * di setiap method (stateless), bukan disimpan sebagai field instance.
 *
 * Method yang tersedia:
 *   buatPeminjaman()    → INSERT satu baris ke tabel_peminjam
 *   dataPeminjaman()    → SELECT satu baris by id_peminjaman
 */
public class PeminjamanController {

    // ── BUAT PEMINJAMAN BARU ──────────────────────────────────────────────────

    /**
     * Menyimpan data peminjaman baru ke database.
     *
     * @param namaPeminjam    nama orang yang meminjam
     * @param idBuku          ID buku yang dipinjam
     * @param judulBuku       judul buku (disimpan denormalisasi untuk kemudahan tampil)
     * @param tanggalDipinjam tanggal buku mulai dipinjam
     * @param tanggalKembali  tanggal buku harus dikembalikan
     * @param koneksi         koneksi JDBC yang sudah terbuka
     * @return true jika berhasil disimpan, false jika gagal
     */
    public static boolean buatPeminjaman(String namaPeminjam, int idBuku, String judulBuku,
                                         LocalDate tanggalDipinjam, LocalDate tanggalKembali,
                                         Connection koneksi) {

        // Validasi input wajib sebelum menyentuh database
        if (namaPeminjam == null || namaPeminjam.trim().isEmpty()) {
            System.err.println("Gagal: Nama peminjam tidak boleh kosong.");
            return false;
        }
        if (tanggalDipinjam == null || tanggalKembali == null) {
            System.err.println("Gagal: Tanggal dipinjam dan tanggal kembali wajib diisi.");
            return false;
        }
        if (tanggalKembali.isBefore(tanggalDipinjam)) {
            System.err.println("Gagal: Tanggal kembali tidak boleh sebelum tanggal dipinjam.");
            return false;
        }

        // Gunakan PreparedStatement — aman dari SQL injection
        String queryInsert =
            "INSERT INTO tabel_peminjam " +
            "(nama_peminjam, id_buku, judul_buku, tanggal_dipinjam, tanggal_kembali) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = koneksi.prepareStatement(
                queryInsert, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, namaPeminjam.trim());
            stmt.setInt(2, idBuku);
            stmt.setString(3, judulBuku != null ? judulBuku : "");
            stmt.setDate(4, Date.valueOf(tanggalDipinjam));
            stmt.setDate(5, Date.valueOf(tanggalKembali));
            stmt.executeUpdate();

            // Ambil ID peminjaman yang di-generate AUTO_INCREMENT
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idBaru = generatedKeys.getInt(1);
                    System.out.println("Peminjaman berhasil disimpan dengan ID: " + idBaru);
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error input data ke tabel_peminjam: " + e.getMessage());
            return false;
        }
    }

    // ── AMBIL DATA PEMINJAMAN BY ID ───────────────────────────────────────────

    /**
     * Mengambil satu data peminjaman berdasarkan ID-nya.
     *
     * @param idPeminjam ID peminjaman yang dicari
     * @param koneksi      koneksi JDBC yang sudah terbuka
     * @return objek Peminjam jika ditemukan, null jika tidak ada
     */
    public static Peminjam dataPeminjaman(int idPeminjam, Connection koneksi) {

        String querySelect =
            "SELECT id_peminjam, nama_peminjam, id_buku, judul_buku, " +
            "tanggal_dipinjam, tanggal_kembali " +
            "FROM tabel_peminjam " +
            "WHERE id_peminjam = ?";

        try (PreparedStatement stmt = koneksi.prepareStatement(querySelect)) {
            stmt.setInt(1, idPeminjam);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Peminjam hasil = new Peminjam();
                    hasil.setIdPeminjam(rs.getInt("id_peminjam"));
                    hasil.setNamaPeminjam(rs.getString("nama_peminjam"));
                    hasil.setIdBuku(rs.getInt("id_buku"));
                    hasil.setJudulBuku(rs.getString("judul_buku"));

                    // Konversi java.sql.Date → LocalDate
                    Date tglDipinjam = rs.getDate("tanggal_dipinjam");
                    Date tglKembali  = rs.getDate("tanggal_kembali");
                    hasil.setTanggalDipinjam(tglDipinjam != null ? tglDipinjam.toLocalDate() : null);
                    hasil.setTanggalKembali(tglKembali   != null ? tglKembali.toLocalDate()  : null);

                    return hasil;

                } else {
                    System.out.println("Peminjaman dengan ID " + idPeminjam + " tidak ditemukan.");
                    return null;
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil data peminjaman: " + e.getMessage());
            return null;
        }
    }
}