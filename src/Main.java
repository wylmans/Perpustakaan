import util.AdminAuth;
import database.DatabaseHelper;
import model.Admin;
import ui.MainWindow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.swing.SwingUtilities;

public class Main {

    // ==========================================================
    // VARIABEL TINGKAT APLIKASI (GLOBAL)
    // ==========================================================

    // Koneksi database yang dioper ke seluruh fungsi CRUD dan Search
    private static Connection koneksiBuku;
    private static Connection koneksiKeyword;

    // Array global stop word dimuat sekali saat aplikasi pertama dibuka
    private static String[] globalStopWord;

    // ==========================================================
    // ENTRY POINT
    // ==========================================================

    public static void main(String[] args) {
        System.out.println("Sistem Perpustakaan Digital sedang dinyalakan...");

        // ── 1. Inisialisasi koneksi ke database ───────────────────────────────────
        try {
            koneksiBuku    = DatabaseHelper.buatKoneksi(); // Koneksi tabel buku utama
            koneksiKeyword = DatabaseHelper.buatKoneksi(); // Koneksi tabel inverted index

            System.out.println("Database berhasil dimuat.");

        } catch (SQLException e) {
            System.err.println("Error Fatal: Gagal terhubung ke database. Aplikasi dihentikan.");
            System.err.println("Detail: " + e.getMessage());
            System.exit(1); // keluar_program()
        }

        // ── 2. Load stopword.txt ke memori saat aplikasi pertama dibuka ───────────
        globalStopWord = muatStopWord("resources/stopword.txt");

        if (globalStopWord.length == 0) {
            System.out.println("Peringatan: File stopword.txt tidak ditemukan atau kosong!");
        } else {
            System.out.println("Stop word dimuat: " + globalStopWord.length + " kata.");
        }

        // ── 3. Muat data admin dari file ──────────────────────────────────────────
        Admin dataPetugas = AdminAuth.muatDataAdminDariTxt("resources/admin.txt");

        if (dataPetugas == null) {
            System.err.println("Error Fatal: Gagal memuat data admin. Aplikasi dihentikan.");
            System.exit(1);
        }

        System.out.println("Membuka Antarmuka Grafis (GUI)...");

        // ── 4. Nyalakan GUI di Event Dispatch Thread (wajib untuk Swing) ──────────
        final Admin   adminFinal    = dataPetugas;
        final String[] stopWordFinal = globalStopWord;

        SwingUtilities.invokeLater(() -> {
            new MainWindow(koneksiBuku, stopWordFinal, adminFinal);
        });
    }

    // ==========================================================
    // FUNGSI PENDUKUNG
    // ==========================================================

    /**
     * Membaca file stopword.txt baris demi baris ke dalam array String.
     *
     * @param lokasiFile path ke file stopword
     * @return array berisi daftar stop word, atau array kosong jika gagal
     */
    private static String[] muatStopWord(String lokasiFile) {
        try {
            List<String> baris = Files.readAllLines(Paths.get(lokasiFile));
            return baris.stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new);
        } catch (IOException e) {
            System.err.println("Gagal membaca stopword.txt: " + e.getMessage());
            return new String[0];
        }
    }
}