package util;

import model.Admin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AdminAuth {

    // ── 1. Hash Password dengan SHA-256 ──────────────────────────────────────────

    /**
     * Mengubah string password menjadi hash SHA-256.
     * Digunakan saat menyimpan password ke admin.txt
     * dan saat membandingkan input login.
     *
     * @param password string plain text yang ingin di-hash
     * @return string hash SHA-256 dalam format hexadecimal
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Konversi byte array ke string hexadecimal
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 selalu tersedia di Java — ini tidak akan pernah terjadi
            throw new RuntimeException("SHA-256 tidak tersedia: " + e.getMessage());
        }
    }

    // ── 2. Muat Data Admin dari File .txt ────────────────────────────────────────

    /**
     * Membaca data admin dari file .txt.
     * Format file (2 baris):
     *   Baris 1 → username
     *   Baris 2 → passwordHash (SHA-256, bukan plain text)
     *
     * Contoh isi admin.txt:
     *   admin
     *   5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8
     *
     * @param lokasiFile path ke file admin.txt, misal "resources/admin.txt"
     * @return objek Admin berisi username dan passwordHash, atau null jika gagal
     */
    public static Admin muatDataAdminDariTxt(String lokasiFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(lokasiFile))) {

            String username     = reader.readLine(); // Baris 1
            String passwordHash = reader.readLine(); // Baris 2

            if (username == null || passwordHash == null) {
                System.err.println("Format admin.txt tidak valid. Pastikan ada 2 baris.");
                return null;
            }

            return new Admin(username.trim(), passwordHash.trim());

        } catch (IOException e) {
            System.err.println("Gagal membaca file admin: " + e.getMessage());
            return null;
        }
    }

    // ── 3. Fungsi Login — Cocokkan Input dengan Data Admin ───────────────────────

    /**
     * Memverifikasi login admin.
     * Input password di-hash dulu sebelum dibandingkan dengan hash di file,
     * sehingga plain text password tidak pernah disimpan atau dibandingkan langsung.
     *
     * @param inputNama     username yang dimasukkan user
     * @param inputPassword password plain text yang dimasukkan user
     * @param dataPetugas   objek Admin yang dimuat dari file
     * @return true jika username dan password cocok, false jika tidak
     */
    public static boolean loginAdmin(String inputNama, String inputPassword, Admin dataPetugas) {
        if (dataPetugas == null || inputNama == null || inputPassword == null) {
            return false;
        }

        // Hash input password dulu sebelum dibandingkan
        String inputPasswordHash = hashPassword(inputPassword);

        return inputNama.equals(dataPetugas.getUsername()) &&
               inputPasswordHash.equals(dataPetugas.getPasswordHash());
    }
}