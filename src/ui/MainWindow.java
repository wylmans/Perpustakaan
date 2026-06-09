package ui;

import engine.SearchEngine;
import model.Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.util.Set;

public class MainWindow extends JFrame {

    // ── Konstanta ukuran jendela ──────────────────────────────────────────────────
    private static final int WIDTH  = 1080;
    private static final int HEIGHT = 720;

    // ── Komponen UI ───────────────────────────────────────────────────────────────
    private JLabel     labelJudul;
    private JTextField inputPencarian;
    private JButton    tombolMencari;

    private JPanel  navigasiPinggir;
    private JButton lihatNavigasi;
    private JButton navigasiInventaris;
    private JButton navigasiKontribusi;

    // ── State sidebar ─────────────────────────────────────────────────────────────
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
        setLayout(null);

        inisialisasiKomponen();
        aturPosisiAwal();
        daftarkanEventListener();

        setVisible(true);
    }

    // ── Inisialisasi semua komponen ───────────────────────────────────────────────
    private void inisialisasiKomponen() {
        labelJudul = new JLabel("PERPUSTAKAAN DIGITAL", SwingConstants.CENTER);
        labelJudul.setFont(new Font("Arial", Font.BOLD, 48));

        inputPencarian = new JTextField();
        inputPencarian.setFont(new Font("Arial", Font.PLAIN, 16));

        tombolMencari = new JButton("Mencari");
        tombolMencari.setFont(new Font("Arial", Font.PLAIN, 16));

        navigasiPinggir = new JPanel();
        navigasiPinggir.setBackground(new Color(45, 45, 45));
        navigasiPinggir.setLayout(null);

        // Load icon hamburger dari resources/icons/menu.png
        ImageIcon iconMenu = new ImageIcon("resources/icons/menu.png");
        Image iconMenuScaled = iconMenu.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        lihatNavigasi = new JButton(new ImageIcon(iconMenuScaled));
        lihatNavigasi.setToolTipText("Buka/Tutup Menu");
        lihatNavigasi.setBorderPainted(false);
        lihatNavigasi.setContentAreaFilled(false);
        lihatNavigasi.setFocusPainted(false);

        navigasiInventaris = new JButton("Inventaris");
        navigasiInventaris.setVisible(false);

        navigasiKontribusi = new JButton("Kontribusi");
        navigasiKontribusi.setVisible(false);
    }

    // ── Atur posisi awal komponen (sidebar tertutup) ──────────────────────────────
    private void aturPosisiAwal() {
        labelJudul.setBounds(340, 200, 500, 60);
        add(labelJudul);

        inputPencarian.setBounds(290, 300, 500, 40);
        add(inputPencarian);

        tombolMencari.setBounds(800, 300, 100, 40);
        add(tombolMencari);

        // Tombol navigasi ditambah SEBELUM panel sidebar agar tidak tertutup
        lihatNavigasi.setBounds(10, 10, 50, 50);
        add(lihatNavigasi);

        navigasiInventaris.setBounds(10, 100, 150, 40);
        add(navigasiInventaris);

        navigasiKontribusi.setBounds(10, 160, 150, 40);
        add(navigasiKontribusi);

        // Panel sidebar ditambah TERAKHIR agar berada di belakang tombol-tombol
        navigasiPinggir.setBounds(0, 0, 70, HEIGHT);
        add(navigasiPinggir);
    }

    // ── Daftarkan semua event listener ────────────────────────────────────────────
    private void daftarkanEventListener() {

        // 1. Tombol Mencari
        tombolMencari.addActionListener(e -> {
            String kataKunci = inputPencarian.getText().trim();

            if (!kataKunci.isEmpty()) {
                Set<Integer> hasilIdBuku = SearchEngine.cariMultiKata(
                        new String[]{kataKunci}, stopWord, koneksi);

                setVisible(false);

                // ✅ Sudah selaras dengan constructor HalamanHasilPencarian
                new HalamanHasilPencarian(hasilIdBuku, koneksi, stopWord, this);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Masukkan kata pencarian terlebih dahulu.",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Tekan Enter di field pencarian juga memicu pencarian
        inputPencarian.addActionListener(e -> tombolMencari.doClick());

        // 2. Toggle Sidebar
        lihatNavigasi.addActionListener(e -> {
            if (!sidebarTerbuka) {
                navigasiPinggir.setBounds(0, 0, 200, HEIGHT);
                navigasiInventaris.setVisible(true);
                navigasiKontribusi.setVisible(true);
                labelJudul.setBounds(400, 200, 500, 60);
                inputPencarian.setBounds(350, 300, 500, 40);
                tombolMencari.setBounds(860, 300, 100, 40);
                sidebarTerbuka = true;
            } else {
                navigasiPinggir.setBounds(0, 0, 70, HEIGHT);
                navigasiInventaris.setVisible(false);
                navigasiKontribusi.setVisible(false);
                labelJudul.setBounds(340, 200, 500, 60);
                inputPencarian.setBounds(290, 300, 500, 40);
                tombolMencari.setBounds(800, 300, 100, 40);
                sidebarTerbuka = false;
            }
            revalidate();
            repaint();
        });

        // 3. Navigasi Inventaris — wajib login dulu sebelum masuk CRUD
        navigasiInventaris.addActionListener(e -> {
            setVisible(false);
            // ✅ Lewat HalamanLogin, bukan langsung ke HalamanCRUDAdmin
            new HalamanLogin(dataPetugas, koneksi, stopWord, this);
        });

        // 4. Navigasi Kontribusi
        navigasiKontribusi.addActionListener(e -> {
            setVisible(false);
            // TODO: new HalamanKontributor(koneksi, stopWord, this);
        });
    }
}