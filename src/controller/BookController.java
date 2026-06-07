package controller;

import database.DatabaseHelper;
import model.Buku;
import util.StringTokenizer;

import java.sql.*;
import java.util.Scanner;

public class BookController {

    // ── CARI BUKU BERDASARKAN ID ──────────────────────────────────────────────────

    /**
     * Mengambil satu data buku lengkap berdasarkan ID-nya.
     * Digunakan untuk mengisi form edit sebelum data diubah.
     *
     * @param idBuku  ID buku yang ingin dicari
     * @param koneksi koneksi JDBC yang sudah terbuka
     * @return objek Buku jika ditemukan, null jika tidak ada
     */
    public static Buku cariBukuById(int idBuku, Connection koneksi) {

        String query = "SELECT judul_buku, penulis_buku, penerbit_buku, " +
                       "lokasi_cover_buku, tahun_terbit_buku, sinopsis_buku " +
                       "FROM tabel_buku WHERE id_buku = ?";

        try (PreparedStatement stmt = koneksi.prepareStatement(query)) {
            stmt.setInt(1, idBuku);

            try (ResultSet barisData = stmt.executeQuery()) {

                // TAHAP 2: Cek apakah data buku ditemukan
                if (barisData.next()) {
                    Buku sementara = new Buku();
                    sementara.setIdBuku(idBuku);
                    sementara.setJudulBuku(barisData.getString("judul_buku"));
                    sementara.setPenulisBuku(barisData.getString("penulis_buku"));
                    sementara.setPenerbitBuku(barisData.getString("penerbit_buku"));
                    sementara.setLokasiCoverBuku(barisData.getString("lokasi_cover_buku"));
                    sementara.setTahunTerbitBuku(barisData.getInt("tahun_terbit_buku"));
                    sementara.setSinopsisBuku(barisData.getString("sinopsis_buku"));
                    return sementara;

                } else {
                    System.out.println("Buku dengan ID " + idBuku + " tidak ditemukan.");
                    return null;
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mencari buku by ID: " + e.getMessage());
            return null;
        }
    }

    // ── DELETE BUKU ───────────────────────────────────────────────────────────────

    /**
     * Menghapus buku beserta seluruh keyword-nya dari database.
     * Keyword di inverted index dihapus dahulu untuk menghindari orphan data.
     *
     * @param judulBuku judul buku yang akan dihapus
     * @param koneksi   koneksi JDBC yang sudah terbuka
     * @return true jika berhasil, false jika buku tidak ditemukan
     */
    public static boolean deleteBuku(String judulBuku, Connection koneksi) {

        int idBukuDihapus;

        try {
            // TAHAP 0: Cari ID buku berdasarkan judul
            String queryCariId = "SELECT id_buku FROM tabel_buku WHERE judul_buku = ?";
            try (PreparedStatement stmtCari = koneksi.prepareStatement(queryCariId)) {
                stmtCari.setString(1, judulBuku);

                try (ResultSet rs = stmtCari.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Data buku tidak ditemukan di database.");
                        return false;
                    }
                    idBukuDihapus = rs.getInt("id_buku");
                }
            }

            // TAHAP 1: Periksa apakah buku dengan ID tersebut benar-benar ada
            String queryCount = "SELECT COUNT(*) FROM tabel_buku WHERE id_buku = ?";
            try (PreparedStatement stmtCount = koneksi.prepareStatement(queryCount)) {
                stmtCount.setInt(1, idBukuDihapus);

                try (ResultSet rsCount = stmtCount.executeQuery()) {
                    rsCount.next();
                    int jumlahDitemukan = rsCount.getInt(1);

                    if (jumlahDitemukan > 0) {

                        // 1A. Hapus keyword di inverted index terlebih dahulu
                        //     (menghindari orphan data)
                        String queryHapusKeyword =
                            "DELETE FROM tabel_keyword WHERE value = ?";
                        try (PreparedStatement stmtKw = koneksi.prepareStatement(queryHapusKeyword)) {
                            stmtKw.setInt(1, idBukuDihapus);
                            stmtKw.executeUpdate();
                        }

                        // 1B. Hapus data utama buku
                        String queryHapusBuku =
                            "DELETE FROM tabel_buku WHERE id_buku = ?";
                        try (PreparedStatement stmtBuku = koneksi.prepareStatement(queryHapusBuku)) {
                            stmtBuku.setInt(1, idBukuDihapus);
                            stmtBuku.executeUpdate();
                        }

                        System.out.println("Buku dan seluruh keyword berhasil dihapus.");
                        return true;

                    } else {
                        System.out.println("Data buku tidak ditemukan di database.");
                        return false;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal menghapus buku: " + e.getMessage());
            return false;
        }
    }

    // ── UPDATE BUKU DARI FORM GUI ─────────────────────────────────────────────────

    /**
     * Versi update yang dipanggil langsung dari form GUI (HalamanCRUDAdmin).
     * Parameter sudah bersih dari form, tidak perlu Scanner.
     */
    public static void updateBukuDariForm(int idBuku, String judul, String penulis,
                                          String penerbit, String cover, String tahunStr,
                                          String sinopsis, String ketersediaan,
                                          String[] stopWord, Connection koneksi) {
        int tahun = 0;
        try { tahun = Integer.parseInt(tahunStr); } catch (NumberFormatException ignored) {}

        if (judul.isEmpty()) judul = "Tidak diketahui";
        if (penulis.isEmpty()) penulis = "Tidak diketahui";
        if (penerbit.isEmpty()) penerbit = "Tidak diketahui";
        if (cover.isEmpty()) cover = "default_cover.jpg";

        String queryUpdate =
            "UPDATE tabel_buku SET judul_buku=?, penulis_buku=?, penerbit_buku=?, " +
            "lokasi_cover_buku=?, tahun_terbit_buku=?, sinopsis_buku=?, ketersediaan=? " +
            "WHERE id_buku=?";

        try (PreparedStatement stmt = koneksi.prepareStatement(queryUpdate)) {
            stmt.setString(1, judul);
            stmt.setString(2, penulis);
            stmt.setString(3, penerbit);
            stmt.setString(4, cover);
            stmt.setInt(5, tahun);
            stmt.setString(6, sinopsis);
            stmt.setString(7, ketersediaan);
            stmt.setInt(8, idBuku);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Gagal update buku: " + e.getMessage());
            return;
        }

        // Hapus keyword lama lalu reindex
        try (PreparedStatement stmtHapus = koneksi.prepareStatement(
                "DELETE FROM tabel_keyword WHERE value=?")) {
            stmtHapus.setInt(1, idBuku);
            stmtHapus.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Gagal hapus keyword lama: " + e.getMessage());
        }

        reindexKeyword(idBuku, judul, stopWord, koneksi);
        System.out.println("Buku berhasil diperbarui.");
    }

    // ── DELETE BUKU BERDASARKAN ID ────────────────────────────────────────────────

    /**
     * Hapus buku berdasarkan ID langsung (dipanggil dari GUI).
     */
    public static void deleteBukuById(int idBuku, Connection koneksi) {
        try (PreparedStatement stmt = koneksi.prepareStatement(
                "DELETE FROM tabel_buku WHERE id_buku=?")) {
            stmt.setInt(1, idBuku);
            stmt.executeUpdate();
            System.out.println("Buku ID " + idBuku + " berhasil dihapus.");
        } catch (SQLException e) {
            System.err.println("Gagal hapus buku: " + e.getMessage());
        }
    }

    // ── HELPER: REINDEX KEYWORD ───────────────────────────────────────────────────
    private static void reindexKeyword(int idBuku, String judul, String[] stopWord, Connection koneksi) {
        String[] kata = util.StringTokenizer.pecahStringBerdasarkanSpasi(judul);
        String queryInsert = "INSERT INTO tabel_keyword (keyword, value) VALUES (?, ?)";

        try (PreparedStatement stmt = koneksi.prepareStatement(queryInsert)) {
            for (String k : kata) {
                boolean isStop = false;
                for (String sw : stopWord) if (k.equalsIgnoreCase(sw)) { isStop = true; break; }
                if (!isStop) {
                    stmt.setString(1, k.toLowerCase());
                    stmt.setInt(2, idBuku);
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal reindex keyword: " + e.getMessage());
        }
    }

    /**
     * Memperbarui data buku dan me-reindex keyword inverted index-nya.
     * Alur: cari ID → validasi → update tabel buku → hapus keyword lama
     *       → tokenisasi judul baru → filter stopword → simpan keyword baru.
     *
     * @param judulBukuLama judul buku yang ingin diperbarui
     * @param stopWord      array stop word untuk filtering
     * @param koneksi       koneksi JDBC yang sudah terbuka
     * @return true jika berhasil, false jika gagal
     */
    public static boolean updateBuku(String judulBukuLama, String[] stopWord, Connection koneksi) {

        int    idBukuUpdate;
        String judulBukuBaru;
        String penulisBukuBaru;
        String penerbitBukuBaru;
        String sinopsisBukuBaru;
        String lokasiCoverBukuBaru;
        int    tahunBukuBaru;
        boolean statusValid = true;

        Scanner scanner = new Scanner(System.in);

        try {
            // TAHAP 1: Cari ID buku berdasarkan judul lama
            String queryCariId = "SELECT id_buku FROM tabel_buku WHERE judul_buku = ?";
            try (PreparedStatement stmtCari = koneksi.prepareStatement(queryCariId)) {
                stmtCari.setString(1, judulBukuLama);

                try (ResultSet rs = stmtCari.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Data tidak ditemukan di database.");
                        return false;
                    }
                    idBukuUpdate = rs.getInt("id_buku");
                }
            }

            // TAHAP 2: Konfirmasi data ditemukan, minta input baru dari user
            System.out.println("Data Buku Ditemukan. Silakan masukkan data baru:");

            System.out.print("Judul Baru       : ");
            judulBukuBaru = scanner.nextLine().trim();

            System.out.print("Penulis Baru     : ");
            penulisBukuBaru = scanner.nextLine().trim();

            System.out.print("Penerbit Baru    : ");
            penerbitBukuBaru = scanner.nextLine().trim();

            System.out.print("Sinopsis Baru    : ");
            sinopsisBukuBaru = scanner.nextLine().trim();

            System.out.print("Lokasi Cover Baru: ");
            lokasiCoverBukuBaru = scanner.nextLine().trim();

            System.out.print("Tahun Baru       : ");
            String tahunInput = scanner.nextLine().trim();

            // TAHAP 3: Validasi input
            if (judulBukuBaru.isEmpty()) {
                System.out.println("Error: Judul buku baru wajib diisi!");
                statusValid = false;
            }
            if (sinopsisBukuBaru.isEmpty()) {
                System.out.println("Error: Sinopsis baru wajib diisi!");
                statusValid = false;
            }

            // Nilai default untuk field opsional
            if (penulisBukuBaru.isEmpty())      penulisBukuBaru      = "Tidak diketahui";
            if (penerbitBukuBaru.isEmpty())     penerbitBukuBaru     = "Tidak diketahui";
            if (lokasiCoverBukuBaru.isEmpty())  lokasiCoverBukuBaru  = "default_cover.jpg";

            tahunBukuBaru = tahunInput.isEmpty() ? 0 : Integer.parseInt(tahunInput);

            // TAHAP 4: Eksekusi jika semua validasi lolos
            if (statusValid) {

                // 4A. Update data utama buku dalam 1 query sekaligus
                String queryUpdate =
                    "UPDATE tabel_buku SET " +
                    "judul_buku = ?, penulis_buku = ?, penerbit_buku = ?, " +
                    "lokasi_cover_buku = ?, tahun_terbit_buku = ?, sinopsis_buku = ? " +
                    "WHERE id_buku = ?";

                try (PreparedStatement stmtUpdate = koneksi.prepareStatement(queryUpdate)) {
                    stmtUpdate.setString(1, judulBukuBaru);
                    stmtUpdate.setString(2, penulisBukuBaru);
                    stmtUpdate.setString(3, penerbitBukuBaru);
                    stmtUpdate.setString(4, lokasiCoverBukuBaru);
                    stmtUpdate.setInt(5, tahunBukuBaru);
                    stmtUpdate.setString(6, sinopsisBukuBaru);
                    stmtUpdate.setInt(7, idBukuUpdate);
                    stmtUpdate.executeUpdate();
                }

                // 4B. Bersihkan keyword lama (sangat krusial!)
                String queryHapusKeyword = "DELETE FROM tabel_keyword WHERE value = ?";
                try (PreparedStatement stmtHapus = koneksi.prepareStatement(queryHapusKeyword)) {
                    stmtHapus.setInt(1, idBukuUpdate);
                    stmtHapus.executeUpdate();
                }

                // 4C. Tokenisasi judul buku baru
                String[] semuaKataJudul = StringTokenizer.pecahStringBerdasarkanSpasi(judulBukuBaru);

                // Filtering stop word
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

                // Simpan keyword baru ke inverted index
                String queryInsertKeyword =
                    "INSERT INTO tabel_keyword (keyword, value) VALUES (?, ?)";

                try (PreparedStatement stmtKw = koneksi.prepareStatement(queryInsertKeyword)) {
                    for (int i = 0; i < jumlahKataBersih; i++) {
                        stmtKw.setString(1, wadahBersih[i]);
                        stmtKw.setInt(2, idBukuUpdate);
                        stmtKw.executeUpdate();
                    }
                }

                System.out.println("Buku dan Inverted Index berhasil diperbarui!");
                return true;

            } else {
                System.out.println("Gagal memperbarui buku karena input tidak valid.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Gagal memperbarui buku: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            System.err.println("Input tahun tidak valid: " + e.getMessage());
            return false;
        }
    }
}