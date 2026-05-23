package ui;

import controller.BookController;
import model.Buku;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HalamanHasilPencarian extends JFrame {

    // ── Konstanta ─────────────────────────────────────────────────────────────────
    private static final int WIDTH  = 1080;
    private static final int HEIGHT = 720;

    // ── Dependensi ────────────────────────────────────────────────────────────────
    private final Set<Integer> kumpulanIdBuku;
    private final Connection   koneksi;
    private final JFrame       halamanSebelumnya; // Referensi ke MainWindow untuk tombol kembali

    // ── Constructor ───────────────────────────────────────────────────────────────
    public HalamanHasilPencarian(Set<Integer> kumpulanIdBuku, Connection koneksi, JFrame halamanSebelumnya) {
        this.kumpulanIdBuku    = kumpulanIdBuku;
        this.koneksi           = koneksi;
        this.halamanSebelumnya = halamanSebelumnya;

        setTitle("Hasil Pencarian Buku");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        bangunUI();
        setVisible(true);
    }

    // ── Bangun seluruh UI halaman hasil ──────────────────────────────────────────
    private void bangunUI() {

        // ── Tombol kembali ────────────────────────────────────────────────────────
        JButton tombolKembali = new JButton("Kembali ke Menu Utama");
        tombolKembali.setBounds(20, 20, 200, 40);
        add(tombolKembali);

        tombolKembali.addActionListener(e -> {
            setVisible(false);
            dispose();
            halamanSebelumnya.setVisible(true); // Tampilkan kembali MainWindow
        });

        // ── Area scroll untuk kartu buku ─────────────────────────────────────────
        // Menggunakan JScrollPane agar bisa di-scroll jika hasil banyak
        JPanel panelKonten = new JPanel();
        panelKonten.setLayout(null);

        // Hitung total tinggi konten berdasarkan jumlah hasil
        int totalTinggi = Math.max(HEIGHT, kumpulanIdBuku.size() * 120 + 100);
        panelKonten.setPreferredSize(new Dimension(WIDTH - 30, totalTinggi));

        // ── Loop kartu buku ───────────────────────────────────────────────────────
        List<Integer> listIdBuku = new ArrayList<>(kumpulanIdBuku);
        int posisiYBox = 10; // Koordinat Y awal kartu pertama di dalam panel konten

        for (int i = 0; i < listIdBuku.size(); i++) {

            // 1. Ambil ID buku asli dari list
            int idBukuAktif = listIdBuku.get(i);

            // 2. Ambil detail buku dari database
            Buku dataBuku = BookController.cariBukuById(idBukuAktif, koneksi);

            if (dataBuku != null) {

                // 3. Buat panel kartu sebagai container visual buku
                JPanel kartuBuku = new JPanel();
                kartuBuku.setBounds(50, posisiYBox, 980, 100);
                kartuBuku.setLayout(null);
                kartuBuku.setBackground(new Color(240, 240, 240));
                kartuBuku.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

                // 4. Label teks judul
                JLabel teksJudul = new JLabel("Judul: " + dataBuku.getJudulBuku());
                teksJudul.setBounds(10, 10, 500, 30);
                teksJudul.setFont(new Font("Arial", Font.BOLD, 14));

                // Label teks penulis
                JLabel teksPenulis = new JLabel("Penulis: " + dataBuku.getPenulisBuku());
                teksPenulis.setBounds(10, 50, 500, 30);
                teksPenulis.setFont(new Font("Arial", Font.PLAIN, 13));

                // Tambahan info penerbit & tahun
                JLabel teksPenerbit = new JLabel(
                        "Penerbit: " + dataBuku.getPenerbitBuku() +
                        "   |   Tahun: " + dataBuku.getTahunTerbitBuku());
                teksPenerbit.setBounds(520, 10, 440, 30);
                teksPenerbit.setFont(new Font("Arial", Font.PLAIN, 12));

                // Masukkan semua label ke dalam kartu
                kartuBuku.add(teksJudul);
                kartuBuku.add(teksPenulis);
                kartuBuku.add(teksPenerbit);

                // Masukkan kartu ke panel konten
                panelKonten.add(kartuBuku);

                // 5. Geser koordinat Y untuk kartu berikutnya
                posisiYBox += 120;
            }
        }

        // Pesan jika tidak ada hasil
        if (listIdBuku.isEmpty()) {
            JLabel labelKosong = new JLabel("Tidak ada buku yang ditemukan.", SwingConstants.CENTER);
            labelKosong.setBounds(0, 100, WIDTH, 40);
            labelKosong.setFont(new Font("Arial", Font.ITALIC, 16));
            panelKonten.add(labelKosong);
        }

        // Bungkus panel konten dengan JScrollPane
        JScrollPane scrollPane = new JScrollPane(panelKonten);
        scrollPane.setBounds(0, 80, WIDTH - 15, HEIGHT - 80);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane);
    }
}