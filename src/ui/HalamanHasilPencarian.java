package ui;

import engine.SearchEngine;
import model.Admin;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;
import java.util.Set;

public class HalamanHasilPencarian extends JFrame {

    private static final int WIDTH  = 1080;
    private static final int HEIGHT = 720;

    // ── Warna ─────────────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(245, 245, 245);
    private static final Color HIJAU_BG  = new Color(212, 239, 223);
    private static final Color HIJAU_FG  = new Color(30, 132, 73);
    private static final Color ORANYE_BG = new Color(255, 243, 205);
    private static final Color ORANYE_FG = new Color(133, 100, 4);
    private static final Color MERAH_BG  = new Color(250, 219, 216);
    private static final Color MERAH_FG  = new Color(192, 57, 43);
    private static final Color BIRU      = new Color(70, 130, 180);

    // ── Komponen ──────────────────────────────────────────────────────────────────
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
    // Parameter Admin dihapus — tidak dibutuhkan di halaman ini
    public HalamanHasilPencarian(Set<Integer> hasilIdBuku, Connection koneksi,
                                  String[] stopWord, JFrame halamanSebelumnya) {
        this.koneksi           = koneksi;
        this.stopWord          = stopWord;
        this.halamanSebelumnya = halamanSebelumnya;

        setTitle("Hasil Pencarian - Perpustakaan Digital");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
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

    // ── Bangun komponen UI ────────────────────────────────────────────────────────
    private void inisialisasiKomponen() {

        JLabel labelJudulPage = new JLabel("Hasil Pencarian Buku");
        labelJudulPage.setBounds(40, 20, 400, 40);
        labelJudulPage.setFont(new Font("Arial", Font.BOLD, 28));
        add(labelJudulPage);

        inputPencarianUlang = new JTextField();
        inputPencarianUlang.setBounds(40, 80, 650, 40);
        inputPencarianUlang.setFont(new Font("Arial", Font.PLAIN, 16));
        inputPencarianUlang.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        add(inputPencarianUlang);

        tombolCariUlang = new JButton("Cari Lagi");
        tombolCariUlang.setBounds(700, 80, 140, 40);
        tombolCariUlang.setBackground(BIRU);
        tombolCariUlang.setForeground(Color.WHITE);
        tombolCariUlang.setFont(new Font("Arial", Font.BOLD, 14));
        tombolCariUlang.setBorderPainted(false);
        tombolCariUlang.setFocusPainted(false);
        tombolCariUlang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(tombolCariUlang);

        tombolKembali = new JButton("Kembali");
        tombolKembali.setBounds(850, 80, 140, 40);
        tombolKembali.setFocusPainted(false);
        tombolKembali.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(tombolKembali);

        containerBuku = new JPanel();
        containerBuku.setLayout(null);
        containerBuku.setBackground(BG);

        scrollPane = new JScrollPane(containerBuku);
        scrollPane.setBounds(40, 150, WIDTH - 80, HEIGHT - 180);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane);

        // ── Event listener ────────────────────────────────────────────────────────

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

    // ── Tampilkan semua kartu buku ────────────────────────────────────────────────
    private void tampilkanDaftarBuku(int[] daftarIdBuku) {
        containerBuku.removeAll();
        int posisiY = 10;

        for (int idBuku : daftarIdBuku) {
            // ✅ Nama kolom disesuaikan dengan schema database kita
            String query =
                "SELECT id_buku, judul_buku, penulis_buku, penerbit_buku, " +
                "lokasi_cover_buku, tahun_terbit_buku, sinopsis_buku, ketersediaan " +
                "FROM tabel_buku WHERE id_buku = " + idBuku;

            try (Statement stmt = koneksi.createStatement();
                 ResultSet rs   = stmt.executeQuery(query)) {

                if (rs.next()) {
                    buatKartuBuku(rs, posisiY);
                    posisiY += 230;
                }

            } catch (SQLException ex) {
                System.err.println("Gagal memuat buku ID " + idBuku + ": " + ex.getMessage());
            }
        }

        // Tampilkan pesan jika tidak ada hasil
        if (daftarIdBuku.length == 0) {
            JLabel lblKosong = new JLabel("Tidak ada buku yang ditemukan.", SwingConstants.CENTER);
            lblKosong.setBounds(0, 100, WIDTH - 80, 40);
            lblKosong.setFont(new Font("Arial", Font.ITALIC, 16));
            lblKosong.setForeground(new Color(150, 150, 150));
            containerBuku.add(lblKosong);
            posisiY = 200;
        }

        containerBuku.setPreferredSize(new Dimension(WIDTH - 100, posisiY + 20));
        containerBuku.revalidate();
        containerBuku.repaint();

        // Scroll ke atas
        SwingUtilities.invokeLater(() ->
            scrollPane.getVerticalScrollBar().setValue(0));
    }

    // ── Buat satu kartu buku ──────────────────────────────────────────────────────
    private void buatKartuBuku(ResultSet rs, int koordinatY) throws SQLException {

        // ✅ Nama kolom sesuai database: judul_buku, penulis_buku, dst
        String txtJudul       = rs.getString("judul_buku");
        String txtPenulis     = rs.getString("penulis_buku");
        String txtPenerbit    = rs.getString("penerbit_buku");
        String txtCover       = rs.getString("lokasi_cover_buku");
        int    numTahun       = rs.getInt("tahun_terbit_buku");
        String txtSinopsis    = rs.getString("sinopsis_buku");
        String ketersediaan   = rs.getString("ketersediaan"); // ✅ ENUM bukan int stok

        // ── Panel kartu ───────────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBounds(0, koordinatY, WIDTH - 100, 210);
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

        // ── Cover ─────────────────────────────────────────────────────────────────
        JLabel coverLabel = new JLabel("Cover", SwingConstants.CENTER);
        coverLabel.setBounds(25, 25, 120, 160);
        coverLabel.setOpaque(true);
        coverLabel.setBackground(new Color(200, 200, 200));
        coverLabel.setForeground(Color.WHITE);
        coverLabel.setFont(new Font("Arial", Font.PLAIN, 13));

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

        // ── Info teks ─────────────────────────────────────────────────────────────
        JLabel lJudul = new JLabel(txtJudul);
        lJudul.setBounds(170, 25, 350, 30);
        lJudul.setFont(new Font("Arial", Font.BOLD, 20));
        card.add(lJudul);

        JLabel lPenulis = new JLabel("Penulis : " + txtPenulis);
        lPenulis.setBounds(170, 65, 350, 25);
        lPenulis.setFont(new Font("Arial", Font.PLAIN, 14));
        card.add(lPenulis);

        JLabel lPenerbit = new JLabel("Penerbit : " + txtPenerbit);
        lPenerbit.setBounds(170, 95, 350, 25);
        lPenerbit.setFont(new Font("Arial", Font.PLAIN, 14));
        card.add(lPenerbit);

        JLabel lTahun = new JLabel("Tahun : " + numTahun);
        lTahun.setBounds(170, 125, 200, 25);
        lTahun.setFont(new Font("Arial", Font.PLAIN, 14));
        card.add(lTahun);

        // ── Sinopsis ──────────────────────────────────────────────────────────────
        JTextArea areaSinopsis = new JTextArea(txtSinopsis);
        areaSinopsis.setBounds(550, 25, 380, 80);
        areaSinopsis.setLineWrap(true);
        areaSinopsis.setWrapStyleWord(true);
        areaSinopsis.setEditable(false);
        areaSinopsis.setBackground(Color.WHITE);
        areaSinopsis.setFont(new Font("Arial", Font.PLAIN, 13));
        areaSinopsis.setBorder(null);
        card.add(areaSinopsis);

        // ── Badge status ketersediaan ─────────────────────────────────────────────
        // ✅ Berdasarkan ENUM "Tersedia"/"Dipinjam"/"Stok Habis", bukan int stok
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
        lblStatus.setFont(new Font("Arial", Font.BOLD, 13));
        lblStatus.setBorder(new LineBorder(fgStatus, 1, true));
        card.add(lblStatus);

        // ── Tombol Detail ─────────────────────────────────────────────────────────
        JButton btnDetail = new JButton("Detail");
        btnDetail.setBounds(810, 135, 120, 35);
        btnDetail.setBackground(BIRU);
        btnDetail.setForeground(Color.WHITE);
        btnDetail.setFont(new Font("Arial", Font.BOLD, 13));
        btnDetail.setBorderPainted(false);
        btnDetail.setFocusPainted(false);
        btnDetail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Simpan data ke variabel final agar bisa dipakai di lambda
        final String judulFinal    = txtJudul;
        final String penulisFinal  = txtPenulis;
        final String penerbitFinal = txtPenerbit;
        final int    tahunFinal    = numTahun;
        final String sinopsisFinal = txtSinopsis;
        final String statusFinal   = ketersediaan;

        btnDetail.addActionListener(e -> {
            String info =
                "Judul        : " + judulFinal    + "\n" +
                "Penulis      : " + penulisFinal  + "\n" +
                "Penerbit     : " + penerbitFinal + "\n" +
                "Tahun Terbit : " + tahunFinal    + "\n" +
                "Ketersediaan : " + statusFinal   + "\n\n" +
                "Sinopsis Lengkap:\n" + sinopsisFinal;

            JTextArea taDetail = new JTextArea(info);
            taDetail.setEditable(false);
            taDetail.setLineWrap(true);
            taDetail.setWrapStyleWord(true);
            taDetail.setFont(new Font("Arial", Font.PLAIN, 13));

            JScrollPane scrollDetail = new JScrollPane(taDetail);
            scrollDetail.setPreferredSize(new Dimension(440, 280));

            JOptionPane.showMessageDialog(this, scrollDetail,
                "Detail: " + judulFinal, JOptionPane.INFORMATION_MESSAGE);
        });

        card.add(btnDetail);
        containerBuku.add(card);
    }
}