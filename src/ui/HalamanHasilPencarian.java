package ui;

import engine.SearchEngine;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;
import java.util.Set;

public class HalamanHasilPencarian extends JFrame {

    private static final int WIDTH  = 1080;
    private static final int HEIGHT = 720;

    // ── Palet Warna Modern (Selaras dengan MainWindow) ────────────────────────────
    private static final Color COLOR_BG_MAIN    = new Color(245, 246, 248); // Abu-abu sangat terang
    private static final Color COLOR_PRIMARY    = new Color(63, 81, 181);    // Indigo modern
    private static final Color COLOR_TEXT_DARK  = new Color(43, 43, 43);     // Hitam lembut
    private static final Color COLOR_TEXT_MUTED = new Color(110, 110, 115);   // Abu-abu teks sekunder
    private static final Color COLOR_CARD_BORDER= new Color(226, 232, 240);   // Border abu-abu tipis

    // Badge Status Ketersediaan (Pastel Style)
    private static final Color HIJAU_BG  = new Color(230, 244, 234);
    private static final Color HIJAU_FG  = new Color(30, 140, 60);
    private static final Color ORANYE_BG = new Color(254, 243, 218);
    private static final Color ORANYE_FG = new Color(180, 110, 10);
    private static final Color MERAH_BG  = new Color(253, 232, 232);
    private static final Color MERAH_FG  = new Color(220, 50, 50);

    // ── Komponen UI ───────────────────────────────────────────────────────────────
    private JTextField inputPencarianUlang;
    private JButton    tombolCariUlang;
    private JButton    tombolKembali;
    private JPanel     containerBuku;
    private JScrollPane scrollPane;

    // ── Dependensi ────────────────────────────────────────────────────────────────
    private final Connection koneksi;
    private final String[]   stopWord;
    private final JFrame     halamanSebelumnya;

    // ── Constructor ───────────────────────────────────────────────────────────────
    public HalamanHasilPencarian(Set<Integer> hasilIdBuku, Connection koneksi,
                                  String[] stopWord, JFrame halamanSebelumnya) {
        this.koneksi           = koneksi;
        this.stopWord          = stopWord;
        this.halamanSebelumnya = halamanSebelumnya;

        setTitle("Hasil Pencarian - Perpustakaan Digital");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(null);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
                if (halamanSebelumnya != null) halamanSebelumnya.setVisible(true);
            }
        });

        inisialisasiKomponen();

        // Konversi Set<Integer> ke int[] untuk di-looping
        int[] arrId = hasilIdBuku.stream().mapToInt(Integer::intValue).toArray();
        tampilkanDaftarBuku(arrId);

        setVisible(true);
    }

    // ── Bangun Komponen UI ────────────────────────────────────────────────────────
    private void inisialisasiKomponen() {

        JLabel labelJudulPage = new JLabel("Hasil Pencarian Buku");
        labelJudulPage.setBounds(40, 20, 400, 40);
        labelJudulPage.setFont(new Font("Segoe UI", Font.BOLD, 28));
        labelJudulPage.setForeground(COLOR_TEXT_DARK);
        add(labelJudulPage);

        // Input Pencarian Ulang
        inputPencarianUlang = new JTextField();
        inputPencarianUlang.setBounds(40, 80, 630, 45); // Tinggi disesuaikan menjadi 45px
        inputPencarianUlang.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputPencarianUlang.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(0, 15, 0, 15))); // Padding text
        add(inputPencarianUlang);

        // Tombol Cari Lagi
        tombolCariUlang = new JButton("Cari Lagi");
        tombolCariUlang.setBounds(685, 80, 140, 45);
        tombolCariUlang.setBackground(COLOR_PRIMARY);
        tombolCariUlang.setForeground(Color.WHITE);
        tombolCariUlang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tombolCariUlang.setBorderPainted(false);
        tombolCariUlang.setFocusPainted(false);
        tombolCariUlang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(tombolCariUlang);

        // Tombol Kembali
        tombolKembali = new JButton("Kembali");
        tombolKembali.setBounds(840, 80, 140, 45);
        tombolKembali.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tombolKembali.setBackground(Color.WHITE);
        tombolKembali.setForeground(COLOR_TEXT_DARK);
        tombolKembali.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        tombolKembali.setFocusPainted(false);
        tombolKembali.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(tombolKembali);

        containerBuku = new JPanel();
        containerBuku.setLayout(null);
        containerBuku.setBackground(COLOR_BG_MAIN);

        scrollPane = new JScrollPane(containerBuku);
        scrollPane.setBounds(40, 150, WIDTH - 80, HEIGHT - 200);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane);

        // ── Event Listener ────────────────────────────────────────────────────────

        // Kembali ke halaman sebelumnya
        tombolKembali.addActionListener(e -> {
            dispose();
            if (halamanSebelumnya != null) halamanSebelumnya.setVisible(true);
        });

        // Cari ulang — buka halaman baru dengan hasil baru
        tombolCariUlang.addActionListener(e -> {
            String kataKunci = inputPencarianUlang.getText().trim();
            if (!kataKunci.isEmpty()) {
                Set<Integer> hasilBaru = SearchEngine.cariMultiKata(
                        new String[]{kataKunci}, stopWord, koneksi);
                dispose();
                new HalamanHasilPencarian(hasilBaru, koneksi, stopWord, halamanSebelumnya);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Masukkan kata pencarian terlebih dahulu.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Enter di field juga memicu pencarian
        inputPencarianUlang.addActionListener(e -> tombolCariUlang.doClick());
    }

    // ── Tampilkan Semua Kartu Buku ────────────────────────────────────────────────
    private void tampilkanDaftarBuku(int[] daftarIdBuku) {
        containerBuku.removeAll();
        int posisiY = 10;

        for (int idBuku : daftarIdBuku) {
            String query =
                "SELECT id_buku, judul_buku, penulis_buku, penerbit_buku, " +
                "lokasi_cover_buku, tahun_terbit_buku, sinopsis_buku, ketersediaan " +
                "FROM tabel_buku WHERE id_buku = " + idBuku;

            try (Statement stmt = koneksi.createStatement();
                 ResultSet rs   = stmt.executeQuery(query)) {

                if (rs.next()) {
                    buatKartuBuku(rs, posisiY);
                    posisiY += 230; // Jarak vertikal antar kartu
                }

            } catch (SQLException ex) {
                System.err.println("Gagal memuat buku ID " + idBuku + ": " + ex.getMessage());
            }
        }

        // Tampilkan pesan jika tidak ada hasil
        if (daftarIdBuku.length == 0) {
            JLabel lblKosong = new JLabel("Tidak ada buku yang ditemukan.", SwingConstants.CENTER);
            lblKosong.setBounds(0, 100, WIDTH - 80, 40);
            lblKosong.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            lblKosong.setForeground(COLOR_TEXT_MUTED);
            containerBuku.add(lblKosong);
            posisiY = 200;
        }

        containerBuku.setPreferredSize(new Dimension(WIDTH - 100, posisiY + 20));
        containerBuku.revalidate();
        containerBuku.repaint();

        // Scroll otomatis kembali ke atas
        SwingUtilities.invokeLater(() ->
            scrollPane.getVerticalScrollBar().setValue(0));
    }

    // ── Buat Satu Kartu Buku ──────────────────────────────────────────────────────
    private void buatKartuBuku(ResultSet rs, int koordinatY) throws SQLException {

        String txtJudul       = rs.getString("judul_buku");
        String txtPenulis     = rs.getString("penulis_buku");
        String txtPenerbit    = rs.getString("penerbit_buku");
        String txtCover       = rs.getString("lokasi_cover_buku");
        int    numTahun       = rs.getInt("tahun_terbit_buku");
        String txtSinopsis    = rs.getString("sinopsis_buku");
        String ketersediaan   = rs.getString("ketersediaan");

        // ── Panel Kartu ───────────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBounds(0, koordinatY, WIDTH - 100, 210);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1));

        // ── Cover Buku ────────────────────────────────────────────────────────────
        JLabel coverLabel = new JLabel("No Cover", SwingConstants.CENTER);
        coverLabel.setBounds(25, 25, 120, 160);
        coverLabel.setOpaque(true);
        coverLabel.setBackground(new Color(235, 237, 240));
        coverLabel.setForeground(COLOR_TEXT_MUTED);
        coverLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        if (txtCover != null && !txtCover.isEmpty()) {
            java.io.File fileCover = new java.io.File(txtCover);
            if (fileCover.exists()) {
                ImageIcon icon   = new ImageIcon(txtCover);
                Image    scaled  = icon.getImage().getScaledInstance(120, 160, Image.SCALE_SMOOTH);
                coverLabel.setIcon(new ImageIcon(scaled));
                coverLabel.setText("");
            }
        }
        card.add(coverLabel);

        // ── Informasi Informasi Buku ──────────────────────────────────────────────
        JLabel lJudul = new JLabel(txtJudul);
        lJudul.setBounds(170, 25, 350, 30);
        lJudul.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lJudul.setForeground(COLOR_TEXT_DARK);
        card.add(lJudul);

        JLabel lPenulis = new JLabel("Penulis: " + txtPenulis);
        lPenulis.setBounds(170, 65, 350, 25);
        lPenulis.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lPenulis.setForeground(COLOR_TEXT_MUTED);
        card.add(lPenulis);

        JLabel lPenerbit = new JLabel("Penerbit: " + txtPenerbit);
        lPenerbit.setBounds(170, 95, 350, 25);
        lPenerbit.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lPenerbit.setForeground(COLOR_TEXT_MUTED);
        card.add(lPenerbit);

        JLabel lTahun = new JLabel("Tahun Terbit: " + numTahun);
        lTahun.setBounds(170, 125, 200, 25);
        lTahun.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lTahun.setForeground(COLOR_TEXT_MUTED);
        card.add(lTahun);

        // ── JTextArea Sinopsis ────────────────────────────────────────────────────
        JTextArea areaSinopsis = new JTextArea(txtSinopsis);
        areaSinopsis.setBounds(550, 25, 380, 80);
        areaSinopsis.setLineWrap(true);
        areaSinopsis.setWrapStyleWord(true);
        areaSinopsis.setEditable(false);
        areaSinopsis.setBackground(Color.WHITE);
        areaSinopsis.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        areaSinopsis.setForeground(COLOR_TEXT_DARK);
        areaSinopsis.setBorder(null);
        card.add(areaSinopsis);

        // ── Badge Status Ketersediaan ─────────────────────────────────────────────
        Color bgStatus, fgStatus;
        switch (ketersediaan) {
            case "Tersedia":
                bgStatus = HIJAU_BG;  fgStatus = HIJAU_FG;   break;
            case "Dipinjam":
                bgStatus = ORANYE_BG; fgStatus = ORANYE_FG;  break;
            default: // Stok Habis
                bgStatus = MERAH_BG;  fgStatus = MERAH_FG;   break;
        }

        JLabel lblStatus = new JLabel(ketersediaan, SwingConstants.CENTER);
        lblStatus.setBounds(550, 135, 140, 35);
        lblStatus.setOpaque(true);
        lblStatus.setBackground(bgStatus);
        lblStatus.setForeground(fgStatus);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setBorder(BorderFactory.createLineBorder(fgStatus, 1));
        card.add(lblStatus);

        // ── Tombol Detail ─────────────────────────────────────────────────────────
        JButton btnDetail = new JButton("Detail");
        btnDetail.setBounds(810, 135, 120, 35);
        btnDetail.setBackground(COLOR_PRIMARY);
        btnDetail.setForeground(Color.WHITE);
        btnDetail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDetail.setBorderPainted(false);
        btnDetail.setFocusPainted(false);
        btnDetail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Menyimpan data final untuk interaksi Lambda Action Listener
        final String judulFinal    = txtJudul;
        final String penulisFinal  = txtPenulis;
        final String penerbitFinal = txtPenerbit;
        final int    tahunFinal    = numTahun;
        final String sinopsisFinal = txtSinopsis;
        final String statusFinal   = ketersediaan;

        btnDetail.addActionListener(e -> {
            String info =
                "Judul\t: " + judulFinal    + "\n" +
                "Penulis\t: " + penulisFinal  + "\n" +
                "Penerbit\t: " + penerbitFinal + "\n" +
                "Tahun Terbit\t: " + tahunFinal    + "\n" +
                "Ketersediaan\t: " + statusFinal   + "\n\n" +
                "Sinopsis Lengkap:\n" + sinopsisFinal;

            JTextArea taDetail = new JTextArea(info);
            taDetail.setEditable(false);
            taDetail.setLineWrap(true);
            taDetail.setWrapStyleWord(true);
            taDetail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            taDetail.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JScrollPane scrollDetail = new JScrollPane(taDetail);
            scrollDetail.setPreferredSize(new Dimension(460, 300));
            scrollDetail.setBorder(null);

            JOptionPane.showMessageDialog(this, scrollDetail,
                "Detail Buku: " + judulFinal, JOptionPane.INFORMATION_MESSAGE);
        });

        card.add(btnDetail);
        containerBuku.add(card);
    }
}