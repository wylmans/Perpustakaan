package controller;

import database.DatabaseHelper;
import model.Buku;
import util.StringTokenizer;

import java.sql.*;
import java.util.Scanner;

public class CreateBookController {

    /**
     * Menjalankan alur penambahan buku baru:
     *  1. Menerima input dari user
     *  2. Menyimpan data buku ke tabel utama
     *  3. Melakukan tokenisasi judul, filter stop word,
     *     lalu menyimpan keyword ke tabel inverted index
     *
     * @param stopWord array kata-kata yang diabaikan saat pengindeksan
     */
    public static void tambahBukuBaru(String[] stopWord) {

        // ── Deklarasi variabel input ──────────────────────────────────────────────
        String judulBuku;
        String penulisBuku;
        String penerbitBuku;
        String lokasiCoverBuku;
        int    tahunTerbitBuku;
        int    idBuku;

        Scanner scanner = new Scanner(System.in);

        // ── INPUT dari user ───────────────────────────────────────────────────────
        System.out.print("Judul Buku       : ");
        judulBuku = scanner.nextLine().trim();

        System.out.print("Penulis Buku     : ");
        penulisBuku = scanner.nextLine().trim();

        System.out.print("Penerbit Buku    : ");
        penerbitBuku = scanner.nextLine().trim();

        System.out.print("Lokasi Cover     : ");
        lokasiCoverBuku = scanner.nextLine().trim();

        System.out.print("Tahun Terbit     : ");
        tahunTerbitBuku = Integer.parseInt(scanner.nextLine().trim());

        try {
            Connection koneksi = DatabaseHelper.buatKoneksi();

            // ── Tentukan ID buku ──────────────────────────────────────────────────
            int jumlahBuku = DatabaseHelper.hitungJumlahBuku(koneksi);

            if (jumlahBuku == 0) {
                idBuku = 1;                 // Tabel kosong, mulai dari 1
            } else {
                idBuku = jumlahBuku + 1;    // Auto-increment manual
            }

            // ── TAHAP 1: Simpan data buku ke tabel utama ──────────────────────────
            String queryInsertBuku =
                "INSERT INTO tabel_buku (id_buku, judul_buku, penulis_buku, " +
                "penerbit_buku, lokasi_cover_buku, tahun_terbit_buku) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmtBuku = koneksi.prepareStatement(queryInsertBuku)) {
                stmtBuku.setInt(1, idBuku);
                stmtBuku.setString(2, judulBuku);
                stmtBuku.setString(3, penulisBuku);
                stmtBuku.setString(4, penerbitBuku);
                stmtBuku.setString(5, lokasiCoverBuku);
                stmtBuku.setInt(6, tahunTerbitBuku);
                stmtBuku.executeUpdate();
            }

            System.out.println("Buku berhasil disimpan dengan ID: " + idBuku);

            // ── TAHAP 2: Proses Pembedahan Kata & Pengindeksan ────────────────────

            // PROSES 2A: Tokenisasi — pecah judul menjadi array kata
            String[] semuaKataJudul = StringTokenizer.pecahStringBerdasarkanSpasi(judulBuku);

            // PROSES 2B: Filter stop word
            String[] wadahBersih = new String[semuaKataJudul.length];
            int jumlahKataBersih = 0;

            for (int i = 0; i < semuaKataJudul.length; i++) {
                boolean adalahStopWord = false;

                for (int j = 0; j < stopWord.length; j++) {
                    if (semuaKataJudul[i].equalsIgnoreCase(stopWord[j])) {
                        adalahStopWord = true;
                        break; // hentikan loop j
                    }
                }

                if (!adalahStopWord) {
                    wadahBersih[jumlahKataBersih] = semuaKataJudul[i];
                    jumlahKataBersih++;
                }
            }

            // PROSES 2C: Simpan setiap keyword ke tabel inverted index
            String queryInsertKeyword =
                "INSERT INTO nama_tabel_keyword (keyword, value) VALUES (?, ?)";

            try (PreparedStatement stmtKeyword = koneksi.prepareStatement(queryInsertKeyword)) {
                for (int i = 0; i < jumlahKataBersih; i++) {
                    String kataKunci = wadahBersih[i].toLowerCase(); // simpan dalam huruf kecil

                    stmtKeyword.setString(1, kataKunci);
                    stmtKeyword.setInt(2, idBuku);
                    stmtKeyword.executeUpdate();
                }
            }

            System.out.println("Keyword berhasil diindeks: " + jumlahKataBersih + " kata.");

            koneksi.close();

        } catch (SQLException e) {
            System.err.println("Kesalahan database: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Input tahun tidak valid: " + e.getMessage());
        }
    }

    // ── Contoh penggunaan ─────────────────────────────────────────────────────────
    public static void main(String[] args) {
        String[] stopWord = {"dan", "atau", "di", "ke", "dari", "yang", "ini", "itu",
                             "untuk", "dengan", "pada", "adalah", "dalam", "oleh"};

        tambahBukuBaru(stopWord);
    }
}