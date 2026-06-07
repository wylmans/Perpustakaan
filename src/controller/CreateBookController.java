package controller;

import database.DatabaseHelper;
import util.StringTokenizer;

import java.sql.*;

public class CreateBookController {

    /**
     * Menyimpan buku baru ke database — dipanggil dari form GUI HalamanCRUDAdmin.
     * Alur: insert tabel_buku → tokenisasi judul → filter stop word → insert tabel_keyword
     *
     * @param judulBuku       judul buku
     * @param penulisBuku     nama penulis
     * @param penerbitBuku    nama penerbit
     * @param lokasiCover     path file cover
     * @param tahunTerbit     tahun terbit
     * @param sinopsis        sinopsis buku
     * @param ketersediaan    "Tersedia" | "Dipinjam" | "Stok Habis"
     * @param stopWord        array stop word untuk filtering keyword
     * @param koneksi         koneksi JDBC yang sudah terbuka
     */
    public static void simpanBuku(String judulBuku, String penulisBuku, String penerbitBuku,
                                  String lokasiCover, int tahunTerbit, String sinopsis,
                                  String ketersediaan, String[] stopWord, Connection koneksi) {
        try {
            // ── TAHAP 1: Simpan data buku ke tabel utama ──────────────────────────
            // Menggunakan AUTO_INCREMENT dari DB — tidak perlu hitung manual
            String queryInsertBuku =
                "INSERT INTO tabel_buku " +
                "(judul_buku, penulis_buku, penerbit_buku, lokasi_cover_buku, " +
                "tahun_terbit_buku, sinopsis_buku, ketersediaan) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

            int idBukuBaru = -1;

            // Gunakan RETURN_GENERATED_KEYS agar bisa dapat ID yang baru dibuat DB
            try (PreparedStatement stmtBuku = koneksi.prepareStatement(
                    queryInsertBuku, Statement.RETURN_GENERATED_KEYS)) {

                stmtBuku.setString(1, judulBuku);
                stmtBuku.setString(2, penulisBuku.isEmpty()  ? "Tidak diketahui" : penulisBuku);
                stmtBuku.setString(3, penerbitBuku.isEmpty() ? "Tidak diketahui" : penerbitBuku);
                stmtBuku.setString(4, lokasiCover.isEmpty()  ? "default_cover.jpg" : lokasiCover);
                stmtBuku.setInt(5, tahunTerbit);
                stmtBuku.setString(6, sinopsis);
                stmtBuku.setString(7, ketersediaan);
                stmtBuku.executeUpdate();

                // Ambil ID yang di-generate oleh AUTO_INCREMENT
                try (ResultSet generatedKeys = stmtBuku.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idBukuBaru = generatedKeys.getInt(1);
                    }
                }
            }

            if (idBukuBaru == -1) {
                System.err.println("Gagal mendapatkan ID buku baru.");
                return;
            }

            System.out.println("Buku berhasil disimpan dengan ID: " + idBukuBaru);

            // ── TAHAP 2: Tokenisasi & Indexing Keyword ────────────────────────────

            // 2A: Pecah judul menjadi array kata
            String[] semuaKataJudul = StringTokenizer.pecahStringBerdasarkanSpasi(judulBuku);

            // 2B: Filter stop word
            String[] wadahBersih = new String[semuaKataJudul.length];
            int jumlahKataBersih = 0;

            for (int i = 0; i < semuaKataJudul.length; i++) {
                boolean adalahStopWord = false;

                for (int j = 0; j < stopWord.length; j++) {
                    if (semuaKataJudul[i].equalsIgnoreCase(stopWord[j])) {
                        adalahStopWord = true;
                        break;
                    }
                }

                if (!adalahStopWord) {
                    wadahBersih[jumlahKataBersih] = semuaKataJudul[i].toLowerCase();
                    jumlahKataBersih++;
                }
            }

            // 2C: Simpan keyword ke tabel inverted index
            String queryInsertKeyword =
                "INSERT INTO tabel_keyword (keyword, value) VALUES (?, ?)";

            try (PreparedStatement stmtKeyword = koneksi.prepareStatement(queryInsertKeyword)) {
                for (int i = 0; i < jumlahKataBersih; i++) {
                    stmtKeyword.setString(1, wadahBersih[i]);
                    stmtKeyword.setInt(2, idBukuBaru);
                    stmtKeyword.executeUpdate();
                }
            }

            System.out.println("Keyword berhasil diindeks: " + jumlahKataBersih + " kata.");

        } catch (SQLException e) {
            System.err.println("Kesalahan database saat simpan buku: " + e.getMessage());
        }
    }
}