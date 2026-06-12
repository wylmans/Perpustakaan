package ui;

import util.AdminAuth;
import model.Admin;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class HalamanLogin extends JFrame {

    private static final int WIDTH  = 400;
    private static final int HEIGHT = 340; // Ditambah sedikit agar layout pesan di bawah lebih lega

    // ── Palet Warna Modern (Selaras dengan Halaman Lain) ──────────────────────────
    private final Color COLOR_BG        = new Color(245, 246, 248); // Abu-abu sangat terang
    private final Color COLOR_PRIMARY   = new Color(63, 81, 181);    // Indigo modern
    private final Color COLOR_TEXT_DARK = new Color(43, 43, 43);     // Hitam lembut
    private final Color COLOR_ERROR     = new Color(220, 50, 50);     // Merah flat

    private JTextField     inputUsername;
    private JPasswordField inputPassword;
    private JButton        tombolLogin;
    private JLabel         labelPesan;

    private final Admin      dataPetugas;
    private final Connection koneksi;
    private final String[]   stopWord;
    private final JFrame     halamanSebelumnya;

    // ── Constructor ───────────────────────────────────────────────────────────────
    public HalamanLogin(Admin dataPetugas, Connection koneksi, String[] stopWord, JFrame halamanSebelumnya) {
        this.dataPetugas       = dataPetugas;
        this.koneksi           = koneksi;
        this.stopWord          = stopWord;
        this.halamanSebelumnya = halamanSebelumnya;

        setTitle("Login Admin — Perpustakaan Digital");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
                if (halamanSebelumnya != null) halamanSebelumnya.setVisible(true);
            }
        });
        
        setLocationRelativeTo(null); // Tampil di tengah layar
        getContentPane().setBackground(COLOR_BG);
        setLayout(null);
        setResizable(false);

        bangunUI();
        setVisible(true);
    }

    // ── Bangun UI Form Login ──────────────────────────────────────────────────────
    private void bangunUI() {

        // Judul Header Login
        JLabel labelJudul = new JLabel("Login Petugas", SwingConstants.CENTER);
        labelJudul.setFont(new Font("Segoe UI", Font.BOLD, 22));
        labelJudul.setForeground(COLOR_TEXT_DARK);
        labelJudul.setBounds(0, 25, WIDTH, 30);
        add(labelJudul);

        // Label Username
        JLabel labelUser = new JLabel("Username");
        labelUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelUser.setForeground(COLOR_TEXT_DARK);
        labelUser.setBounds(50, 75, 100, 20);
        add(labelUser);

        // Input Username
        inputUsername = new JTextField();
        inputUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputUsername.setBounds(50, 100, 300, 35); // Diperlebar dan ditinggikan
        inputUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(0, 10, 0, 10) // Padding teks dalam field
        ));
        add(inputUsername);

        // Label Password
        JLabel labelPass = new JLabel("Password");
        labelPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelPass.setForeground(COLOR_TEXT_DARK);
        labelPass.setBounds(50, 145, 100, 20);
        add(labelPass);

        // Input Password
        inputPassword = new JPasswordField();
        inputPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputPassword.setBounds(50, 170, 300, 35);
        inputPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(0, 10, 0, 10) // Padding teks dalam field
        ));
        add(inputPassword);

        // Tombol Login (Flat Style)
        tombolLogin = new JButton("Masuk");
        tombolLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tombolLogin.setBackground(COLOR_PRIMARY);
        tombolLogin.setForeground(Color.WHITE);
        tombolLogin.setBounds(50, 225, 300, 38);
        tombolLogin.setFocusPainted(false);
        tombolLogin.setBorderPainted(false);
        tombolLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(tombolLogin);

        // Label Pesan Status/Error
        labelPesan = new JLabel("", SwingConstants.CENTER);
        labelPesan.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 12));
        labelPesan.setForeground(COLOR_ERROR);
        labelPesan.setBounds(0, 275, WIDTH, 25);
        add(labelPesan);

        // ── Event Listener ────────────────────────────────────────────────────────
        tombolLogin.addActionListener(e -> prosesLogin());
        inputPassword.addActionListener(e -> prosesLogin());
        inputUsername.addActionListener(e -> prosesLogin()); // Menekan enter di username juga bisa memicu proses login
    }

    // ── Proses Verifikasi Login ───────────────────────────────────────────────────
    private void prosesLogin() {
        String inputNama = inputUsername.getText().trim();
        String inputPass = new String(inputPassword.getPassword()).trim();

        if (inputNama.isEmpty() || inputPass.isEmpty()) {
            labelPesan.setForeground(COLOR_ERROR);
            labelPesan.setText("Username dan password wajib diisi.");
            return;
        }

        boolean berhasil = AdminAuth.loginAdmin(inputNama, inputPass, dataPetugas);

        if (berhasil) {
            labelPesan.setForeground(new Color(30, 140, 60)); // Hijau flat jika sukses
            labelPesan.setText("Login berhasil! Membuka halaman admin...");

            // Menonaktifkan komponen pasca-sukses untuk mencegah double click
            tombolLogin.setEnabled(false);
            inputUsername.setEditable(false);
            inputPassword.setEditable(false);

            // Tutup form login lalu buka halaman CRUD Admin
            Timer timer = new Timer(800, ev -> {
                dispose();
                new HalamanInventaris(koneksi, halamanSebelumnya, stopWord);
            });
            timer.setRepeats(false);
            timer.start();

        } else {
            labelPesan.setForeground(COLOR_ERROR);
            labelPesan.setText("Username atau password salah.");
            inputPassword.setText(""); // Bersihkan field password
            inputPassword.requestFocus(); // Kembalikan fokus kursor ke field password
        }
    }
}