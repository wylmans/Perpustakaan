package database;

import model.Buku;

import java.sql.*;
import java.util.*;

public class DatabaseHelper {

    // ── Konfigurasi koneksi ───────────────────────────────────────────────────────
    // TODO: Sesuaikan URL, username, dan password dengan database-mu
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/nama_database";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";

    /**
     * Membuat koneksi baru ke database.
     *
     * @return objek Connection JDBC
     * @throws SQLException jika koneksi gagal
     */
    public static Connection buatKoneksi() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ── Inverted Index ────────────────────────────────────────────────────────────

    /**
     * Mengambil seluruh inverted index dari tabel keyword dalam satu query.
     * Setiap baris berisi pasangan (keyword → id_buku).
     *
     * @param koneksi objek Connection JDBC yang sudah terbuka
     * @return HashMap berisi pemetaan kata → Set ID buku
     */
    public static HashMap<String, Set<Integer>> mengambilDataDariDatabase(Connection koneksi) {
        HashMap<String, Set<Integer>> mapKeyword = new HashMap<>();

        // Cukup 1 query untuk mengambil semua data inverted index
        String query = "SELECT keyword, value FROM nama_tabel_keyword";

        try (Statement stmt = koneksi.createStatement();
             ResultSet hasilDatabase = stmt.executeQuery(query)) {

            // Iterasi selama masih ada baris yang bisa dibaca
            while (hasilDatabase.next()) {

                String  kataKunci = hasilDatabase.getString("keyword");
                int     idBuku    = hasilDatabase.getInt("value");

                // Jika kata kunci belum ada di hashmap, buatkan Set kosong terlebih dulu
                if (!mapKeyword.containsKey(kataKunci)) {
                    mapKeyword.put(kataKunci, new HashSet<>());
                }

                // Masukkan id_buku ke dalam Set (baik data baru maupun lama)
                mapKeyword.get(kataKunci).add(idBuku);
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil inverted index: " + e.getMessage());
        }

        return mapKeyword;
    }

    // ── Data Buku ─────────────────────────────────────────────────────────────────

    /**
     * Mengambil semua buku dari database, diurutkan berdasarkan judul (A-Z).
     *
     * @param koneksi objek Connection JDBC yang sudah terbuka
     * @return List berisi semua objek Buku
     */
    public static List<Buku> ambilSemuaBukuDariDatabase(Connection koneksi) {
        List<Buku> daftarBuku = new ArrayList<>();

        String query = "SELECT * FROM tabel_buku ORDER BY judul_buku ASC";

        try (Statement stmt = koneksi.createStatement();
             ResultSet hasil = stmt.executeQuery(query)) {

            while (hasil.next()) {
                Buku buku = new Buku();
                buku.setIdBuku(hasil.getInt("id_buku"));
                buku.setJudulBuku(hasil.getString("judul_buku"));
                buku.setPenulisBuku(hasil.getString("penulis_buku"));
                buku.setPenerbitBuku(hasil.getString("penerbit_buku"));
                buku.setLokasiCoverBuku(hasil.getString("lokasi_cover_buku"));
                buku.setTahunTerbitBuku(hasil.getInt("tahun_terbit_buku"));
                daftarBuku.add(buku);
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil semua buku: " + e.getMessage());
        }

        return daftarBuku;
    }

    /**
     * Menghitung jumlah baris di tabel buku.
     * Digunakan untuk menentukan ID buku berikutnya saat insert.
     *
     * @param koneksi objek Connection JDBC yang sudah terbuka
     * @return jumlah baris, atau 0 jika tabel kosong / terjadi error
     */
    public static int hitungJumlahBuku(Connection koneksi) {
        String query = "SELECT COUNT(*) FROM tabel_buku";

        try (Statement stmt = koneksi.createStatement();
             ResultSet hasil = stmt.executeQuery(query)) {

            if (hasil.next()) {
                return hasil.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Gagal menghitung jumlah buku: " + e.getMessage());
        }

        return 0;
    }
}