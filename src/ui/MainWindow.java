package ui;

import engine.SearchEngine;
import model.Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.util.Set;

public class MainWindow extends JFrame {

    // ── Konstanta Ukuran Jendela ──────────────────────────────────────────────────
    private static final int WIDTH  = 1080;
    private static final int HEIGHT = 720;

    // ── Palet Warna Modern ────────────────────────────────────────────────────────
    private final Color COLOR_BG_MAIN   = new Color(245, 246, 248); // Abu-abu sangat terang
    private final Color COLOR_SIDEBAR   = new Color(30, 30, 36);     // Dark charcoal
    private final Color COLOR_TEXT_DARK = new Color(43, 43, 43);     // Hitam lembut
    private final Color COLOR_PRIMARY   = new Color(63, 81, 181);    // Indigo modern
    private final Color COLOR_NAV_BTN   = new Color(48, 48, 56);     // Sedikit lebih terang dari sidebar

    // ── Komponen UI ───────────────────────────────────────────────────────────────
    private JLabel     labelJudul;
    private JTextField inputPencarian;
    private JButton    tombolMencari;

    private JPanel     navigasiPinggir;
    private JButton    lihatNavigasi;
    private JButton    navigasiInventaris;
    private JButton    navigasiKontribusi;

    // ── State Sidebar ─────────────────────────────────────────────────────────────
    private boolean sidebarTerbuka = false;

    // ── Dependensi ────────────────────────────────────────────────────────────────
    private final Connection koneksi;
    private final String[]   stopWord;
    private final Admin      dataPetugas;

    // ── Constructor ───────────────────────────────────────────────────────────────
    public MainWindow(Connection koneksi, String[] stopWord, Admin dataPetugas) {
        this.koneksi     = koneksi;
        this.stopWord    = stopWord;
        this.dataPetugas = dataPetugas;

        setTitle("Perpustakaan Digital");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Jendela muncul di tengah layar
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(null);

        inisialisasiKomponen();
        aturPosisiAwal();
        daftarkanEventListener();

        setVisible(true);
    }

    // ── Inisialisasi Semua Komponen ───────────────────────────────────────────────
    private void inisialisasiKomponen() {
        // Judul Utama (Lebar komponen diperbesar agar font 44 tidak terpotong)
        labelJudul = new JLabel("PERPUSTAKAAN DIGITAL", SwingConstants.CENTER);
        labelJudul.setFont(new Font("Segoe UI", Font.BOLD, 44));
        labelJudul.setForeground(COLOR_TEXT_DARK);

        // Input Pencarian
        inputPencarian = new JTextField();
        inputPencarian.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputPencarian.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(0, 15, 0, 15) // Padding teks di dalam input
        ));

        // Tombol Cari
        tombolMencari = new JButton("Cari");
        tombolMencari.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tombolMencari.setBackground(COLOR_PRIMARY);
        tombolMencari.setForeground(Color.WHITE);
        tombolMencari.setFocusPainted(false);
        tombolMencari.setBorderPainted(false);
        tombolMencari.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Sidebar Panel
        navigasiPinggir = new JPanel();
        navigasiPinggir.setBackground(COLOR_SIDEBAR);
        navigasiPinggir.setLayout(null);

        // Tombol Hamburger Menu
        ImageIcon iconMenu = new ImageIcon("resources/icons/menu.png");
        Image iconMenuScaled = iconMenu.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        lihatNavigasi = new JButton(new ImageIcon(iconMenuScaled));
        lihatNavigasi.setToolTipText("Buka/Tutup Menu");
        lihatNavigasi.setBorderPainted(false);
        lihatNavigasi.setContentAreaFilled(false);
        lihatNavigasi.setFocusPainted(false);
        lihatNavigasi.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Tombol Navigasi Inventaris (Gaya Flat Modern)
        navigasiInventaris = new JButton("Inventaris");
        formatTombolSidebar(navigasiInventaris);

        // Tombol Navigasi Kontribusi (Gaya Flat Modern)
        navigasiKontribusi = new JButton("Kontribusi");
        formatTombolSidebar(navigasiKontribusi);
    }

    // ── Helper untuk Formatting Tombol Sidebar ────────────────────────────────────
    private void formatTombolSidebar(JButton tombol) {
        tombol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tombol.setBackground(COLOR_NAV_BTN);
        tombol.setForeground(Color.WHITE);
        tombol.setFocusPainted(false);
        tombol.setBorderPainted(false);
        tombol.setVisible(false);
        tombol.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ── Atur Posisi Awal Komponen (Sidebar Tertutup) ──────────────────────────────
    private void aturPosisiAwal() {
        // Koordinat tengah untuk konten utama saat posisi awal (X awal seimbang)
        labelJudul.setBounds(265, 200, 650, 60);
        add(labelJudul);

        inputPencarian.setBounds(265, 300, 450, 45); // Tinggi disamakan (45px)
        add(inputPencarian);

        tombolMencari.setBounds(725, 300, 100, 45);  // Pas di sebelah input tanpa gap renggang
        add(tombolMencari);

        // Komponen Navigasi ditaruh di atas Panel Sidebar
        lihatNavigasi.setBounds(10, 15, 50, 40);
        add(lihatNavigasi);

        navigasiInventaris.setBounds(10, 100, 180, 40); // Lebar disesuaikan penuh saat menu terbuka
        add(navigasiInventaris);

        navigasiKontribusi.setBounds(10, 150, 180, 40);
        add(navigasiKontribusi);

        // Panel utama sidebar diletakkan di layer paling belakang
        navigasiPinggir.setBounds(0, 0, 70, HEIGHT);
        add(navigasiPinggir);
    }

    // ── Daftarkan Semua Event Listener ────────────────────────────────────────────
    private void daftarkanEventListener() {

        // 1. Tombol Mencari
        tombolMencari.addActionListener(e -> {
            String kataKunci = inputPencarian.getText().trim();

            if (!kataKunci.isEmpty()) {
                Set<Integer> hasilIdBuku = SearchEngine.cariMultiKata(
                        new String[]{kataKunci}, stopWord, koneksi);

                setVisible(false);
                new HalamanHasilPencarian(hasilIdBuku, koneksi, stopWord, this);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Masukkan kata pencarian terlebih dahulu.",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Enter Key Trigger
        inputPencarian.addActionListener(e -> tombolMencari.doClick());

        // 2. Toggle Sidebar (Animasi Pergeseran Posisi Komponen)
        lihatNavigasi.addActionListener(e -> {
            if (!sidebarTerbuka) {
                // Saat Menu Terbuka
                navigasiPinggir.setBounds(0, 0, 200, HEIGHT);
                navigasiInventaris.setVisible(true);
                navigasiKontribusi.setVisible(true);

                // Geser konten utama ke kanan agar tetap center-balanced
                labelJudul.setBounds(330, 200, 650, 60);
                inputPencarian.setBounds(330, 300, 450, 45);
                tombolMencari.setBounds(790, 300, 100, 45);
                sidebarTerbuka = true;
            } else {
                // Saat Menu Tertutup
                navigasiPinggir.setBounds(0, 0, 70, HEIGHT);
                navigasiInventaris.setVisible(false);
                navigasiKontribusi.setVisible(false);

                // Kembalikan posisi konten utama ke kiri awal
                labelJudul.setBounds(265, 200, 650, 60);
                inputPencarian.setBounds(265, 300, 450, 45);
                tombolMencari.setBounds(725, 300, 100, 45);
                sidebarTerbuka = false;
            }
            revalidate();
            repaint();
        });

        // 3. Navigasi Inventaris
        navigasiInventaris.addActionListener(e -> {
            setVisible(false);
            new HalamanLogin(dataPetugas, koneksi, stopWord, this);
        });

        // 4. Navigasi Kontribusi
        navigasiKontribusi.addActionListener(e -> {
            setVisible(false);
            // TODO: new HalamanKontributor(koneksi, stopWord, this);
        });
    }
}