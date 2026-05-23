package controller;

import database.DatabaseHelper;
import engine.SearchEngine;
import model.Buku;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class BookSearchBridge {

    // ── Baca Stop Word dari file ──────────────────────────────────────────────────

    /**
     * Membaca daftar stop word dari file teks (satu kata per baris).
     *
     * @param namaFile path ke file stopword, misal "resources/stopword.txt"
     * @return array berisi daftar stop word
     */
    private static String[] bacaStopWord(String namaFile) {
        try {
            List<String> baris = Files.readAllLines(Paths.get(namaFile));
            return baris.toArray(new String[0]);
        } catch (IOException e) {
            System.err.println("Gagal membaca file stop word: " + e.getMessage());
            return new String[0];
        }
    }

    // ── Render buku ke konsol ─────────────────────────────────────────────────────

    /**
     * Menampilkan data buku ke layar.
     * Ganti isi method ini dengan logika render UI Swing/JavaFX-mu.
     *
     * @param buku objek Buku yang akan ditampilkan
     */
    private static void render(Buku buku) {
        System.out.println("─────────────────────────────────────");
        System.out.println("Judul    : " + buku.getJudulBuku());
        System.out.println("Penulis  : " + buku.getPenulisBuku());
        System.out.println("Penerbit : " + buku.getPenerbitBuku());
        System.out.println("Cover    : " + buku.getLokasiCoverBuku());
        System.out.println("Tahun    : " + buku.getTahunTerbitBuku());
        System.out.println("─────────────────────────────────────");
    }

    // ── Main ──────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {

        Set<Integer> bukuDitampilkan;
        String[]     kataPencarian;
        List<Buku>   tampilBuku = new ArrayList<>();

        try {
            // Satu koneksi dipakai bersama untuk hashmap dan data buku
            Connection koneksi = DatabaseHelper.buatKoneksi();

            // Baca stop word dari folder resources
            String[] stopWord = bacaStopWord("resources/stopword.txt");

            // ── INPUT: Terima kata pencarian dari user ────────────────────────────
            System.out.print("Masukan Kata Pencarian anda : ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
            kataPencarian = new String[]{input};

            // ── PENCARIAN: Ambil inverted index lalu cari ID buku yang relevan ────
            // Hasilnya adalah Set berisi ID Buku, misal: [5, 12]
            bukuDitampilkan = SearchEngine.cariMultiKata(kataPencarian, stopWord, koneksi);

            // ── FETCH DATA BUKU berdasarkan ID dari Set ───────────────────────────
            List<Integer> listIdBuku = new ArrayList<>(bukuDitampilkan);

            for (int i = 0; i < listIdBuku.size(); i++) {

                // 1. Ambil ID buku asli dari list pada indeks i
                int idBukuTarget = listIdBuku.get(i);

                // 2. Query satu buku berdasarkan ID — kolom disesuaikan dengan tabel
                String query = "SELECT id_buku, judul_buku, penulis_buku, " +
                               "penerbit_buku, lokasi_cover_buku, tahun_terbit_buku " +
                               "FROM tabel_buku WHERE id_buku = ?";

                try (PreparedStatement stmt = koneksi.prepareStatement(query)) {
                    stmt.setInt(1, idBukuTarget);

                    try (ResultSet barisBuku = stmt.executeQuery()) {

                        // 3. Masukkan data dari ResultSet ke objek Buku via setter
                        if (barisBuku.next()) {
                            Buku sementara = new Buku();
                            sementara.setIdBuku(barisBuku.getInt("id_buku"));
                            sementara.setJudulBuku(barisBuku.getString("judul_buku"));
                            sementara.setPenulisBuku(barisBuku.getString("penulis_buku"));
                            sementara.setPenerbitBuku(barisBuku.getString("penerbit_buku"));
                            sementara.setLokasiCoverBuku(barisBuku.getString("lokasi_cover_buku"));
                            sementara.setTahunTerbitBuku(barisBuku.getInt("tahun_terbit_buku"));

                            // 4. Masukkan objek Buku ke list untuk dirender
                            tampilBuku.add(sementara);
                        }
                    }
                }
            }

            // ── RENDER: Tampilkan semua buku yang ditemukan ───────────────────────
            if (tampilBuku.isEmpty()) {
                System.out.println("Tidak ada buku yang ditemukan.");
            } else {
                System.out.println("\nDitemukan " + tampilBuku.size() + " buku:\n");
                for (int i = 0; i < tampilBuku.size(); i++) {
                    render(tampilBuku.get(i));
                }
            }

            // ── Tutup resource ────────────────────────────────────────────────────
            koneksi.close();
            scanner.close();

        } catch (SQLException e) {
            System.err.println("Kesalahan database: " + e.getMessage());
        }
    }
}