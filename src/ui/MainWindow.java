package ui;

import engine.SearchEngine;
import model.Admin;
import ui.HalamanLogin;

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

    private JPanel     navigasiPinggir;
    private JButton    lihatNavigasi;
    private JButton    navigasiInventaris;
    private JButton    navigasiKontribusi;

    // ── State sidebar ─────────────────────────────────────────────────────────────
    private boolean sidebarTerbuka = false;

    // ── Dependensi ────────────────────────────────────────────────────────────────
    private final Connection  koneksi;
    private final String[]    stopWord;
    private final Admin       dataPetugas;

    // ── Constructor ───────────────────────────────────────────────────────────────
    public MainWindow(Connection koneksi, String[] stopWord, Admin dataPetugas) {
        this.koneksi      = koneksi;
        this.stopWord     = stopWord;
        this.dataPetugas  = dataPetugas;

        setTitle("Perpustakaan Digital");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // Absolute layout sesuai pseudocode

        inisialisasiKomponen();
        aturPosisiAwal();
        daftarkanEventListener();

        setVisible(true);
    }

    // ── Inisialisasi semua komponen ───────────────────────────────────────────────
    private void inisialisasiKomponen() {

        // Label judul utama
        labelJudul = new JLabel("PERPUSTAKAAN DIGITAL", SwingConstants.CENTER);
        labelJudul.setFont(new Font("Arial", Font.BOLD, 48));

        // Input pencarian
        inputPencarian = new JTextField();
        inputPencarian.setFont(new Font("Arial", Font.PLAIN, 16));

        // Tombol cari
        tombolMencari = new JButton("Mencari");
        tombolMencari.setFont(new Font("Arial", Font.PLAIN, 16));

        // Sidebar panel
        navigasiPinggir = new JPanel();
        navigasiPinggir.setBackground(new Color(45, 45, 45));
        navigasiPinggir.setLayout(null);

        // Tombol hamburger
        lihatNavigasi = new JButton("☰");
        lihatNavigasi.setFont(new Font("Arial", Font.PLAIN, 20));

        // Tombol navigasi menu
        navigasiInventaris = new JButton("Inventaris");
        navigasiInventaris.setVisible(false); // Sembunyikan saat sidebar mengecil

        navigasiKontribusi = new JButton("Kontribusi");
        navigasiKontribusi.setVisible(false);
    }

    // ── Atur posisi awal komponen (sidebar tertutup) ──────────────────────────────
    private void aturPosisiAwal() {

        // Label judul
        labelJudul.setBounds(340, 200, 500, 60);
        add(labelJudul);

        // Input pencarian
        inputPencarian.setBounds(290, 300, 500, 40);
        add(inputPencarian);

        // Tombol cari
        tombolMencari.setBounds(800, 300, 100, 40);
        add(tombolMencari);

        // Tombol hamburger & navigasi (ditambahkan sebelum panel agar tidak tertutup)
        lihatNavigasi.setBounds(10, 10, 50, 50);
        add(lihatNavigasi);

        navigasiInventaris.setBounds(10, 100, 150, 40);
        add(navigasiInventaris);

        navigasiKontribusi.setBounds(10, 160, 150, 40);
        add(navigasiKontribusi);

        // Sidebar panel (ditambahkan terakhir agar berada di belakang tombol)
        navigasiPinggir.setBounds(0, 0, 70, HEIGHT);
        add(navigasiPinggir);
    }

    // ── Daftarkan semua event listener ───────────────────────────────────────────
    private void daftarkanEventListener() {

        // 1. Tombol Mencari
        tombolMencari.addActionListener(e -> {
            String kataKunci = inputPencarian.getText().trim();

            if (!kataKunci.isEmpty()) {
                // Jalankan pencarian, hasilnya Set<Integer> berisi ID buku
                Set<Integer> hasilIdBuku = SearchEngine.cariMultiKata(
                        new String[]{kataKunci}, stopWord, koneksi);

                // Sembunyikan halaman utama
                setVisible(false);

                // Buka halaman hasil pencarian
                new HalamanHasilPencarian(hasilIdBuku, koneksi, this);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Masukkan kata pencarian terlebih dahulu.",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        // 2. Toggle Sidebar
        lihatNavigasi.addActionListener(e -> {
            if (!sidebarTerbuka) {
                // Buka sidebar
                navigasiPinggir.setBounds(0, 0, 200, HEIGHT);
                navigasiInventaris.setVisible(true);
                navigasiKontribusi.setVisible(true);

                // Geser komponen utama ke kanan
                labelJudul.setBounds(400, 200, 500, 60);
                inputPencarian.setBounds(350, 300, 500, 40);
                tombolMencari.setBounds(860, 300, 100, 40);

                sidebarTerbuka = true;
            } else {
                // Tutup sidebar
                navigasiPinggir.setBounds(0, 0, 70, HEIGHT);
                navigasiInventaris.setVisible(false);
                navigasiKontribusi.setVisible(false);

                // Kembalikan komponen utama ke posisi semula
                labelJudul.setBounds(340, 200, 500, 60);
                inputPencarian.setBounds(290, 300, 500, 40);
                tombolMencari.setBounds(800, 300, 100, 40);

                sidebarTerbuka = false;
            }
            revalidate();
            repaint();
        });

        // 3. Navigasi Inventaris — buka HalamanLogin dulu sebelum CRUD
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