package ui;

import util.AdminAuth;
import model.Admin;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class HalamanLogin extends JFrame {

    private static final int WIDTH  = 400;
    private static final int HEIGHT = 300;

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
        setLayout(null);
        setResizable(false);

        bangunUI();
        setVisible(true);
    }

    // ── Bangun UI form login ──────────────────────────────────────────────────────
    private void bangunUI() {

        JLabel labelJudul = new JLabel("Login Petugas", SwingConstants.CENTER);
        labelJudul.setFont(new Font("Arial", Font.BOLD, 20));
        labelJudul.setBounds(0, 20, WIDTH, 30);
        add(labelJudul);

        JLabel labelUser = new JLabel("Username:");
        labelUser.setBounds(60, 80, 100, 25);
        add(labelUser);

        inputUsername = new JTextField();
        inputUsername.setBounds(160, 80, 180, 25);
        add(inputUsername);

        JLabel labelPass = new JLabel("Password:");
        labelPass.setBounds(60, 120, 100, 25);
        add(labelPass);

        inputPassword = new JPasswordField();
        inputPassword.setBounds(160, 120, 180, 25);
        add(inputPassword);

        tombolLogin = new JButton("Masuk");
        tombolLogin.setBounds(140, 170, 120, 35);
        add(tombolLogin);

        // Label pesan error/sukses
        labelPesan = new JLabel("", SwingConstants.CENTER);
        labelPesan.setFont(new Font("Arial", Font.ITALIC, 12));
        labelPesan.setForeground(Color.RED);
        labelPesan.setBounds(0, 220, WIDTH, 25);
        add(labelPesan);

        // ── Event listener tombol login ───────────────────────────────────────────
        tombolLogin.addActionListener(e -> prosesLogin());

        // Tekan Enter di field password juga memicu login
        inputPassword.addActionListener(e -> prosesLogin());
    }

    // ── Proses verifikasi login ───────────────────────────────────────────────────
    private void prosesLogin() {
        String inputNama = inputUsername.getText().trim();
        String inputPass = new String(inputPassword.getPassword()).trim();

        if (inputNama.isEmpty() || inputPass.isEmpty()) {
            labelPesan.setText("Username dan password wajib diisi.");
            return;
        }

        boolean berhasil = AdminAuth.loginAdmin(inputNama, inputPass, dataPetugas);

        if (berhasil) {
            labelPesan.setForeground(new Color(0, 128, 0));
            labelPesan.setText("Login berhasil! Membuka halaman admin...");

            // Tutup form login lalu buka halaman CRUD Admin
            Timer timer = new Timer(800, ev -> {
                dispose();
                // TODO: new HalamanCRUDAdmin(koneksi, stopWord);
            });
            timer.setRepeats(false);
            timer.start();

        } else {
            labelPesan.setForeground(Color.RED);
            labelPesan.setText("Username atau password salah.");
            inputPassword.setText(""); // Bersihkan field password
        }
    }
}