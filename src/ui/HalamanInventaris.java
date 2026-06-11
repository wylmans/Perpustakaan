package ui;

import controller.BookController;
import controller.CreateBookController;
import controller.PeminjamanController;
import model.Buku;
import model.Peminjam;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * HalamanInventaris — Halaman utama manajemen buku perpustakaan digital.
 *
 * Tab yang tersedia:
 *   0 → Daftar Buku        : tampilkan semua buku di tabel, bisa cari by ID
 *   1 → Buat Buku Baru     : form input buku baru → simpanBuku()
 *   2 → Ubah Data Buku     : cari by ID → isi form → updateBukuDariForm()
 *   3 → Hapus Data Buku    : cari by ID → konfirmasi → deleteBukuById()
 *   4 → Buat Peminjaman    : form input peminjaman → buatPeminjaman()
 *   5 → Daftar Peminjaman  : tabel semua peminjaman + tombol kembalikan per baris
 */
public class HalamanInventaris extends JFrame {

    // ── Dimensi jendela ───────────────────────────────────────────────────────
    private static final int WIDTH  = 1080;
    private static final int HEIGHT = 720;

    // ── Kolom tabel buku ──────────────────────────────────────────────────────
    private static final String[] COL_TABEL_BUKU = {
        "ID", "Judul", "Penulis", "Penerbit",
        "Tahun", "Sinopsis", "Cover", "Ketersediaan"
    };

    // ── Warna & Font ──────────────────────────────────────────────────────────
    private static final Color  BG_COLOR      = new Color(245, 245, 248);
    private static final Color  ACCENT_COLOR  = new Color(52, 103, 173);
    private static final Color  DANGER_COLOR  = new Color(192, 57, 43);
    private static final Font   FONT_JUDUL    = new Font("SansSerif", Font.BOLD, 28);
    private static final Font   FONT_NORMAL   = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font   FONT_LABEL    = new Font("SansSerif", Font.BOLD, 13);

    // ── State ─────────────────────────────────────────────────────────────────
    private final Connection koneksi;
    private final String[]   stopWord;
    private final JFrame     parentWindow;

    // ── Komponen utama ────────────────────────────────────────────────────────
    private JLabel      lblJudul;
    private JTabbedPane tabbedPane;
    private JButton     btnKembali;

    // Panel tab
    private JPanel panelDaftarBuku;
    private JPanel panelBuatBuku;
    private JPanel panelUbahBuku;
    private JPanel panelHapusBuku;

    // ── Komponen tab Daftar Buku ──────────────────────────────────────────────
    private JTable            tabelBuku;
    private DefaultTableModel modelTabel;
    private JTextField        inputCariId;
    private JButton           btnCariId;
    private JButton           btnMuatSemua;

    // ── Komponen tab Buat Buku ────────────────────────────────────────────────
    private JTextField  createJudul, createPenulis, createPenerbit;
    private JTextField  createTahun, createCover;
    private JTextArea   createSinopsis;
    private ButtonGroup createKetersediaanGroup;
    private JRadioButton createRbTersedia, createRbDipinjam, createRbStokHabis;
    private JButton     btnKirimBuat;
    private JLabel      lblStatusBuat;

    // ── Komponen tab Ubah Buku ────────────────────────────────────────────────
    private JTextField  ubahInputId;
    private JButton     btnCariUbah;
    private JTextField  ubahJudul, ubahPenulis, ubahPenerbit;
    private JTextField  ubahTahun, ubahCover;
    private JTextArea   ubahSinopsis;
    private ButtonGroup ubahKetersediaanGroup;
    private JRadioButton ubahRbTersedia, ubahRbDipinjam, ubahRbStokHabis;
    private JButton     btnSimpanUbah;
    private JLabel      lblStatusUbah;
    private int         idBukuSedangDiubah = -1;

    // ── Komponen tab Hapus Buku ───────────────────────────────────────────────
    private JTextField  hapusInputId;
    private JButton     btnCariHapus;
    private JLabel      lblInfoHapus;
    private JButton     btnKonfirmasiHapus;
    private JLabel      lblStatusHapus;
    private int         idBukuAkanDihapus = -1;

    // ── Komponen tab Buat Peminjaman ──────────────────────────────────────────
    private JPanel     panelBuatPeminjaman;
    private JTextField pinjamNama, pinjamIdBuku, pinjamJudulBuku;
    private JTextField pinjamTanggalDipinjam, pinjamTanggalKembali;
    private JLabel     lblStatusPinjam;
    private JButton    btnKirimPinjam;

    // ── Komponen tab Daftar Peminjaman ────────────────────────────────────────
    private JPanel         panelDaftarPeminjaman;
    private JTable         tabelPeminjaman;
    private DefaultTableModel modelTabelPeminjaman;
    private JButton        btnRefreshPeminjaman;

    private static final String[] COL_TABEL_PEMINJAMAN = {
        "ID", "Nama Peminjam", "ID Buku", "Judul Buku",
        "Tgl Dipinjam", "Tgl Kembali", "Status", "Denda (Rp)", "Aksi"
    };
    // Denda per hari keterlambatan (ubah sesuai kebijakan perpustakaan)
    private static final long DENDA_PER_HARI = 1000L;

    // ═════════════════════════════════════════════════════════════════════════
    // KONSTRUKTOR
    // ═════════════════════════════════════════════════════════════════════════
    public HalamanInventaris(Connection koneksi, JFrame parentWindow, String[] stopWord) {
        this.koneksi      = koneksi;
        this.parentWindow = parentWindow;
        this.stopWord     = stopWord;

        initWindow();
        initKomponenUtama();
        initTabDaftarBuku();
        initTabBuatBuku();
        initTabUbahBuku();
        initTabHapusBuku();
        initTabBuatPeminjaman();
        initTabDaftarPeminjaman();
        initEventHandlers();

        setVisible(true);

        // Muat data buku saat pertama kali dibuka
        muatSemuaBuku();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // INISIALISASI WINDOW
    // ═════════════════════════════════════════════════════════════════════════
    private void initWindow() {
        setTitle("Halaman Inventaris Perpustakaan");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BG_COLOR);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // KOMPONEN UTAMA (Header + TabbedPane + Tombol Kembali)
    // ═════════════════════════════════════════════════════════════════════════
    private void initKomponenUtama() {
        // ── Header ───────────────────────────────────────────────────────────
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(ACCENT_COLOR);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        lblJudul = new JLabel("📚  Inventaris Perpustakaan");
        lblJudul.setFont(FONT_JUDUL);
        lblJudul.setForeground(Color.WHITE);
        panelHeader.add(lblJudul, BorderLayout.WEST);

        add(panelHeader, BorderLayout.NORTH);

        // ── TabbedPane ───────────────────────────────────────────────────────
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(tabbedPane, BorderLayout.CENTER);

        // Inisialisasi panel (konten diisi di method masing-masing)
        panelDaftarBuku        = new JPanel(new BorderLayout(8, 8));
        panelBuatBuku          = new JPanel(new BorderLayout(8, 8));
        panelUbahBuku          = new JPanel(new BorderLayout(8, 8));
        panelHapusBuku         = new JPanel(new BorderLayout(8, 8));
        panelBuatPeminjaman    = new JPanel(new BorderLayout(8, 8));
        panelDaftarPeminjaman  = new JPanel(new BorderLayout(8, 8));

        for (JPanel p : new JPanel[]{
                panelDaftarBuku, panelBuatBuku, panelUbahBuku, panelHapusBuku,
                panelBuatPeminjaman, panelDaftarPeminjaman}) {
            p.setBackground(BG_COLOR);
            p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        }

        tabbedPane.addTab("📋  Daftar Buku",        panelDaftarBuku);
        tabbedPane.addTab("➕  Buat Buku Baru",      panelBuatBuku);
        tabbedPane.addTab("✏️  Ubah Data Buku",      panelUbahBuku);
        tabbedPane.addTab("🗑️  Hapus Data Buku",     panelHapusBuku);
        tabbedPane.addTab("📝  Buat Peminjaman",     panelBuatPeminjaman);
        tabbedPane.addTab("📖  Daftar Peminjaman",   panelDaftarPeminjaman);
        tabbedPane.setSelectedIndex(0);

        // ── Tombol Kembali ───────────────────────────────────────────────────
        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFooter.setBackground(BG_COLOR);
        panelFooter.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));

        btnKembali = buatTombol("← Kembali", ACCENT_COLOR);
        panelFooter.add(btnKembali);

        add(panelFooter, BorderLayout.SOUTH);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 0 — DAFTAR BUKU
    // ═════════════════════════════════════════════════════════════════════════
    private void initTabDaftarBuku() {
        // ── Toolbar pencarian ─────────────────────────────────────────────────
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolBar.setBackground(BG_COLOR);

        toolBar.add(buatLabel("Cari ID :"));
        inputCariId = new JTextField(8);
        inputCariId.setFont(FONT_NORMAL);
        toolBar.add(inputCariId);

        btnCariId    = buatTombol("Cari", ACCENT_COLOR);
        btnMuatSemua = buatTombol("Tampilkan Semua", new Color(80, 140, 80));
        toolBar.add(btnCariId);
        toolBar.add(btnMuatSemua);

        panelDaftarBuku.add(toolBar, BorderLayout.NORTH);

        // ── Tabel ─────────────────────────────────────────────────────────────
        modelTabel = new DefaultTableModel(COL_TABEL_BUKU, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelBuku = new JTable(modelTabel);
        tabelBuku.setFont(FONT_NORMAL);
        tabelBuku.setRowHeight(24);
        tabelBuku.getTableHeader().setFont(FONT_LABEL);
        tabelBuku.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelBuku.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Lebar kolom kecil untuk ID dan Tahun
        tabelBuku.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabelBuku.getColumnModel().getColumn(4).setPreferredWidth(55);
        tabelBuku.getColumnModel().getColumn(7).setPreferredWidth(100);

        JScrollPane scrollTabel = new JScrollPane(tabelBuku);
        panelDaftarBuku.add(scrollTabel, BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 1 — BUAT BUKU BARU
    // ═════════════════════════════════════════════════════════════════════════
    private void initTabBuatBuku() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = defaultGbc();

        createJudul    = new JTextField(28);
        createPenulis  = new JTextField(28);
        createPenerbit = new JTextField(28);
        createTahun    = new JTextField(8);
        createSinopsis = new JTextArea(4, 28);
        createSinopsis.setLineWrap(true);
        createSinopsis.setWrapStyleWord(true);
        createCover    = new JTextField(22);

        JButton btnPilihCover = buatTombol("📂 Pilih File", new Color(100, 100, 100));
        btnPilihCover.addActionListener(e -> pilihFile(createCover));

        createRbTersedia  = new JRadioButton("Tersedia",    true);
        createRbDipinjam  = new JRadioButton("Dipinjam",    false);
        createRbStokHabis = new JRadioButton("Stok Habis",  false);
        createKetersediaanGroup = new ButtonGroup();
        createKetersediaanGroup.add(createRbTersedia);
        createKetersediaanGroup.add(createRbDipinjam);
        createKetersediaanGroup.add(createRbStokHabis);

        // Atur font semua field
        for (JTextField tf : new JTextField[]{createJudul, createPenulis, createPenerbit, createTahun, createCover})
            tf.setFont(FONT_NORMAL);
        createSinopsis.setFont(FONT_NORMAL);

        int baris = 0;
        tambahBarisForms(formPanel, gbc, baris++, "Judul Buku *",     createJudul, null);
        tambahBarisForms(formPanel, gbc, baris++, "Penulis",          createPenulis, null);
        tambahBarisForms(formPanel, gbc, baris++, "Penerbit",         createPenerbit, null);
        tambahBarisForms(formPanel, gbc, baris++, "Tahun Terbit",     createTahun, null);
        tambahBarisForms(formPanel, gbc, baris++, "Sinopsis *",       new JScrollPane(createSinopsis), null);
        tambahBarisCover(formPanel, gbc, baris++, createCover, btnPilihCover);

        // Radio ketersediaan
        gbc.gridy = baris++; gbc.gridx = 0;
        formPanel.add(buatLabel("Ketersediaan"), gbc);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        radioPanel.setBackground(BG_COLOR);
        radioPanel.add(createRbTersedia);
        radioPanel.add(createRbDipinjam);
        radioPanel.add(createRbStokHabis);
        gbc.gridx = 1;
        formPanel.add(radioPanel, gbc);

        // Tombol kirim + status
        baris++;
        gbc.gridy = baris; gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        btnKirimBuat = buatTombol("💾  Simpan Buku", ACCENT_COLOR);
        formPanel.add(btnKirimBuat, gbc);

        baris++;
        gbc.gridy = baris; gbc.gridx = 1;
        lblStatusBuat = new JLabel(" ");
        lblStatusBuat.setFont(FONT_NORMAL);
        formPanel.add(lblStatusBuat, gbc);

        JScrollPane scroll = new JScrollPane(formPanel);
        scroll.setBorder(null);
        panelBuatBuku.add(scroll, BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 2 — UBAH DATA BUKU
    // ═════════════════════════════════════════════════════════════════════════
    private void initTabUbahBuku() {
        // ── Baris cari ID ─────────────────────────────────────────────────────
        JPanel cariPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        cariPanel.setBackground(BG_COLOR);
        cariPanel.add(buatLabel("ID Buku :"));
        ubahInputId = new JTextField(8);
        ubahInputId.setFont(FONT_NORMAL);
        cariPanel.add(ubahInputId);
        btnCariUbah = buatTombol("🔍 Cari & Isi Form", ACCENT_COLOR);
        cariPanel.add(btnCariUbah);

        panelUbahBuku.add(cariPanel, BorderLayout.NORTH);

        // ── Form ubah ─────────────────────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = defaultGbc();

        ubahJudul    = new JTextField(28);
        ubahPenulis  = new JTextField(28);
        ubahPenerbit = new JTextField(28);
        ubahTahun    = new JTextField(8);
        ubahSinopsis = new JTextArea(4, 28);
        ubahSinopsis.setLineWrap(true);
        ubahSinopsis.setWrapStyleWord(true);
        ubahCover    = new JTextField(22);

        JButton btnPilihCoverUbah = buatTombol("📂 Pilih File", new Color(100, 100, 100));
        btnPilihCoverUbah.addActionListener(e -> pilihFile(ubahCover));

        ubahRbTersedia  = new JRadioButton("Tersedia",   true);
        ubahRbDipinjam  = new JRadioButton("Dipinjam",   false);
        ubahRbStokHabis = new JRadioButton("Stok Habis", false);
        ubahKetersediaanGroup = new ButtonGroup();
        ubahKetersediaanGroup.add(ubahRbTersedia);
        ubahKetersediaanGroup.add(ubahRbDipinjam);
        ubahKetersediaanGroup.add(ubahRbStokHabis);

        for (JTextField tf : new JTextField[]{ubahJudul, ubahPenulis, ubahPenerbit, ubahTahun, ubahCover})
            tf.setFont(FONT_NORMAL);
        ubahSinopsis.setFont(FONT_NORMAL);

        setEnabledFormUbah(false); // Nonaktifkan dulu sebelum data dicari

        int baris = 0;
        tambahBarisForms(formPanel, gbc, baris++, "Judul Buku *",  ubahJudul, null);
        tambahBarisForms(formPanel, gbc, baris++, "Penulis",       ubahPenulis, null);
        tambahBarisForms(formPanel, gbc, baris++, "Penerbit",      ubahPenerbit, null);
        tambahBarisForms(formPanel, gbc, baris++, "Tahun Terbit",  ubahTahun, null);
        tambahBarisForms(formPanel, gbc, baris++, "Sinopsis *",    new JScrollPane(ubahSinopsis), null);
        tambahBarisCover(formPanel, gbc, baris++, ubahCover, btnPilihCoverUbah);

        gbc.gridy = baris++; gbc.gridx = 0;
        formPanel.add(buatLabel("Ketersediaan"), gbc);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        radioPanel.setBackground(BG_COLOR);
        radioPanel.add(ubahRbTersedia);
        radioPanel.add(ubahRbDipinjam);
        radioPanel.add(ubahRbStokHabis);
        gbc.gridx = 1;
        formPanel.add(radioPanel, gbc);

        baris++;
        gbc.gridy = baris; gbc.gridx = 1;
        btnSimpanUbah = buatTombol("💾  Simpan Perubahan", ACCENT_COLOR);
        btnSimpanUbah.setEnabled(false);
        formPanel.add(btnSimpanUbah, gbc);

        baris++;
        gbc.gridy = baris; gbc.gridx = 1;
        lblStatusUbah = new JLabel(" ");
        lblStatusUbah.setFont(FONT_NORMAL);
        formPanel.add(lblStatusUbah, gbc);

        JScrollPane scroll = new JScrollPane(formPanel);
        scroll.setBorder(null);
        panelUbahBuku.add(scroll, BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 3 — HAPUS DATA BUKU
    // ═════════════════════════════════════════════════════════════════════════
    private void initTabHapusBuku() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(BG_COLOR);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Cari ID
        JPanel cariPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        cariPanel.setBackground(BG_COLOR);
        cariPanel.add(buatLabel("ID Buku :"));
        hapusInputId = new JTextField(8);
        hapusInputId.setFont(FONT_NORMAL);
        cariPanel.add(hapusInputId);
        btnCariHapus = buatTombol("🔍 Cari Buku", ACCENT_COLOR);
        cariPanel.add(btnCariHapus);
        wrapper.add(cariPanel);

        // Info buku yang ditemukan
        lblInfoHapus = new JLabel("—  Masukkan ID buku yang ingin dihapus.");
        lblInfoHapus.setFont(FONT_NORMAL);
        lblInfoHapus.setBorder(BorderFactory.createEmptyBorder(16, 4, 16, 4));
        wrapper.add(lblInfoHapus);

        // Tombol konfirmasi hapus
        btnKonfirmasiHapus = buatTombol("🗑️  Konfirmasi Hapus", DANGER_COLOR);
        btnKonfirmasiHapus.setEnabled(false);
        JPanel konfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        konfPanel.setBackground(BG_COLOR);
        konfPanel.add(btnKonfirmasiHapus);
        wrapper.add(konfPanel);

        // Label status
        lblStatusHapus = new JLabel(" ");
        lblStatusHapus.setFont(FONT_NORMAL);
        lblStatusHapus.setBorder(BorderFactory.createEmptyBorder(10, 4, 0, 4));
        wrapper.add(lblStatusHapus);

        panelHapusBuku.add(wrapper, BorderLayout.NORTH);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // EVENT HANDLERS
    // ═════════════════════════════════════════════════════════════════════════
    private void initEventHandlers() {

        // ── Tombol Kembali ────────────────────────────────────────────────────
        btnKembali.addActionListener(e -> {
            if (parentWindow != null) {
                parentWindow.setVisible(true);
            }
            dispose();
        });

        // ── Tab: Daftar Buku — Cari by ID ────────────────────────────────────
        btnCariId.addActionListener(e -> {
            String teks = inputCariId.getText().trim();
            if (teks.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan ID terlebih dahulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                int id = Integer.parseInt(teks);
                Buku b = BookController.cariBukuById(id, koneksi);
                modelTabel.setRowCount(0);
                if (b != null) {
                    modelTabel.addRow(new Object[]{
                        b.getIdBuku(), b.getJudulBuku(), b.getPenulisBuku(),
                        b.getPenerbitBuku(), b.getTahunTerbitBuku(),
                        b.getSinopsisBuku(), b.getLokasiCoverBuku(),
                        b.getKetersediaan()
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "Buku dengan ID " + id + " tidak ditemukan.", "Tidak Ditemukan", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID harus berupa angka.", "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ── Tab: Daftar Buku — Muat Semua ────────────────────────────────────
        btnMuatSemua.addActionListener(e -> {
            inputCariId.setText("");
            muatSemuaBuku();
        });

        // ── Tab: Buat Buku — Simpan ───────────────────────────────────────────
        btnKirimBuat.addActionListener(e -> {
            String judul    = createJudul.getText().trim();
            String penulis  = createPenulis.getText().trim();
            String penerbit = createPenerbit.getText().trim();
            String tahunStr = createTahun.getText().trim();
            String sinopsis = createSinopsis.getText().trim();
            String cover    = createCover.getText().trim();
            String ket      = getSelectedRadio(createKetersediaanGroup,
                                new JRadioButton[]{createRbTersedia, createRbDipinjam, createRbStokHabis});

            if (judul.isEmpty() || sinopsis.isEmpty()) {
                lblStatusBuat.setForeground(DANGER_COLOR);
                lblStatusBuat.setText("✗  Judul dan Sinopsis wajib diisi!");
                return;
            }

            int tahun = 0;
            if (!tahunStr.isEmpty()) {
                try { tahun = Integer.parseInt(tahunStr); }
                catch (NumberFormatException ex) {
                    lblStatusBuat.setForeground(DANGER_COLOR);
                    lblStatusBuat.setText("✗  Format tahun tidak valid (contoh: 2023)");
                    return;
                }
            }

            CreateBookController.simpanBuku(
                judul, penulis, penerbit, cover, tahun, sinopsis, ket, stopWord, koneksi
            );

            lblStatusBuat.setForeground(new Color(30, 130, 60));
            lblStatusBuat.setText("✓  Buku \"" + judul + "\" berhasil disimpan.");
            resetFormBuat();
            muatSemuaBuku();
        });

        // ── Tab: Ubah Buku — Cari & Isi Form ─────────────────────────────────
        btnCariUbah.addActionListener(e -> {
            String teks = ubahInputId.getText().trim();
            if (teks.isEmpty()) {
                lblStatusUbah.setForeground(DANGER_COLOR);
                lblStatusUbah.setText("✗  Masukkan ID buku.");
                return;
            }
            try {
                int id = Integer.parseInt(teks);
                Buku b = BookController.cariBukuById(id, koneksi);
                if (b == null) {
                    setEnabledFormUbah(false);
                    btnSimpanUbah.setEnabled(false);
                    idBukuSedangDiubah = -1;
                    lblStatusUbah.setForeground(DANGER_COLOR);
                    lblStatusUbah.setText("✗  Buku dengan ID " + id + " tidak ditemukan.");
                } else {
                    idBukuSedangDiubah = id;
                    isiFormUbah(b);
                    setEnabledFormUbah(true);
                    btnSimpanUbah.setEnabled(true);
                    lblStatusUbah.setForeground(new Color(30, 130, 60));
                    lblStatusUbah.setText("✓  Data dimuat. Silakan ubah dan simpan.");
                }
            } catch (NumberFormatException ex) {
                lblStatusUbah.setForeground(DANGER_COLOR);
                lblStatusUbah.setText("✗  ID harus berupa angka.");
            }
        });

        // ── Tab: Ubah Buku — Simpan Perubahan ────────────────────────────────
        btnSimpanUbah.addActionListener(e -> {
            if (idBukuSedangDiubah == -1) return;

            String judul    = ubahJudul.getText().trim();
            String penulis  = ubahPenulis.getText().trim();
            String penerbit = ubahPenerbit.getText().trim();
            String tahunStr = ubahTahun.getText().trim();
            String sinopsis = ubahSinopsis.getText().trim();
            String cover    = ubahCover.getText().trim();
            String ket      = getSelectedRadio(ubahKetersediaanGroup,
                                new JRadioButton[]{ubahRbTersedia, ubahRbDipinjam, ubahRbStokHabis});

            if (judul.isEmpty() || sinopsis.isEmpty()) {
                lblStatusUbah.setForeground(DANGER_COLOR);
                lblStatusUbah.setText("✗  Judul dan Sinopsis wajib diisi!");
                return;
            }

            // updateBukuDariForm menerima tahun sebagai String (validasi di dalam method)
            BookController.updateBukuDariForm(
                idBukuSedangDiubah, judul, penulis, penerbit,
                cover, tahunStr, sinopsis, ket, stopWord, koneksi
            );

            lblStatusUbah.setForeground(new Color(30, 130, 60));
            lblStatusUbah.setText("✓  Buku ID " + idBukuSedangDiubah + " berhasil diperbarui.");
            muatSemuaBuku();
        });

        // ── Tab: Hapus Buku — Cari ────────────────────────────────────────────
        btnCariHapus.addActionListener(e -> {
            String teks = hapusInputId.getText().trim();
            if (teks.isEmpty()) {
                lblInfoHapus.setText("Masukkan ID buku terlebih dahulu.");
                return;
            }
            try {
                int id = Integer.parseInt(teks);
                Buku b = BookController.cariBukuById(id, koneksi);
                if (b == null) {
                    idBukuAkanDihapus = -1;
                    btnKonfirmasiHapus.setEnabled(false);
                    lblInfoHapus.setText("Buku dengan ID " + id + " tidak ditemukan.");
                    lblStatusHapus.setText(" ");
                } else {
                    idBukuAkanDihapus = id;
                    lblInfoHapus.setText(
                        "<html><b>Ditemukan:</b>  [" + b.getIdBuku() + "]  " +
                        b.getJudulBuku() + "  —  " + b.getPenulisBuku() +
                        "  (" + b.getTahunTerbitBuku() + ")</html>"
                    );
                    btnKonfirmasiHapus.setEnabled(true);
                    lblStatusHapus.setText(" ");
                }
            } catch (NumberFormatException ex) {
                lblInfoHapus.setText("ID harus berupa angka.");
            }
        });

        // ── Tab: Hapus Buku — Konfirmasi ──────────────────────────────────────
        btnKonfirmasiHapus.addActionListener(e -> {
            if (idBukuAkanDihapus == -1) return;

            int pilihan = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus buku ID " + idBukuAkanDihapus + "?\nAksi ini tidak dapat dibatalkan.",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (pilihan == JOptionPane.YES_OPTION) {
                BookController.deleteBukuById(idBukuAkanDihapus, koneksi);
                lblStatusHapus.setForeground(new Color(30, 130, 60));
                lblStatusHapus.setText("✓  Buku ID " + idBukuAkanDihapus + " berhasil dihapus.");
                lblInfoHapus.setText("—  Masukkan ID buku yang ingin dihapus.");
                hapusInputId.setText("");
                idBukuAkanDihapus = -1;
                btnKonfirmasiHapus.setEnabled(false);
                muatSemuaBuku();
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS — Data & UI
    // ═════════════════════════════════════════════════════════════════════════

    // ── TAB 4 — BUAT PEMINJAMAN ───────────────────────────────────────────────
    private void initTabBuatPeminjaman() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = defaultGbc();

        pinjamNama           = new JTextField(28);
        pinjamIdBuku         = new JTextField(10);
        pinjamJudulBuku      = new JTextField(28);
        pinjamTanggalDipinjam = new JTextField(12);
        pinjamTanggalKembali  = new JTextField(12);

        for (JTextField tf : new JTextField[]{
                pinjamNama, pinjamIdBuku, pinjamJudulBuku,
                pinjamTanggalDipinjam, pinjamTanggalKembali}) {
            tf.setFont(FONT_NORMAL);
        }

        // Placeholder format tanggal
        pinjamTanggalDipinjam.setText("YYYY-MM-DD");
        pinjamTanggalKembali.setText("YYYY-MM-DD");

        // Tombol validasi ID buku — isi judul otomatis dari DB
        JButton btnValidasiId = buatTombol("🔍 Cek ID", new Color(80, 140, 80));

        int baris = 0;
        tambahBarisForms(formPanel, gbc, baris++, "Nama Peminjam *",   pinjamNama, null);
        tambahBarisForms(formPanel, gbc, baris++, "ID Buku *",         pinjamIdBuku, btnValidasiId);
        tambahBarisForms(formPanel, gbc, baris++, "Judul Buku",        pinjamJudulBuku, null);
        tambahBarisForms(formPanel, gbc, baris++, "Tgl Dipinjam *",    pinjamTanggalDipinjam, null);
        tambahBarisForms(formPanel, gbc, baris++, "Tgl Kembali *",     pinjamTanggalKembali, null);

        // Catatan format
        gbc.gridy = baris++; gbc.gridx = 1;
        JLabel lblCatatan = new JLabel("* Format tanggal: YYYY-MM-DD  (contoh: 2025-06-15)");
        lblCatatan.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblCatatan.setForeground(Color.GRAY);
        formPanel.add(lblCatatan, gbc);

        // Tombol kirim
        gbc.gridy = baris++; gbc.gridx = 1;
        btnKirimPinjam = buatTombol("💾  Simpan Peminjaman", ACCENT_COLOR);
        formPanel.add(btnKirimPinjam, gbc);

        // Label status
        gbc.gridy = baris; gbc.gridx = 1;
        lblStatusPinjam = new JLabel(" ");
        lblStatusPinjam.setFont(FONT_NORMAL);
        formPanel.add(lblStatusPinjam, gbc);

        // ── Event: validasi ID buku & isi judul otomatis ──────────────────────
        btnValidasiId.addActionListener(e -> {
            String idStr = pinjamIdBuku.getText().trim();
            if (idStr.isEmpty()) {
                lblStatusPinjam.setForeground(DANGER_COLOR);
                lblStatusPinjam.setText("✗  Masukkan ID buku terlebih dahulu.");
                return;
            }
            try {
                int id = Integer.parseInt(idStr);
                Buku b = BookController.cariBukuById(id, koneksi);
                if (b == null) {
                    pinjamJudulBuku.setText("");
                    lblStatusPinjam.setForeground(DANGER_COLOR);
                    lblStatusPinjam.setText("✗  ID buku " + id + " tidak ditemukan di database.");
                    btnKirimPinjam.setEnabled(false);
                } else {
                    pinjamJudulBuku.setText(b.getJudulBuku());
                    String ketersediaan = b.getKetersediaan() != null ? b.getKetersediaan() : "Tersedia";

                    if ("Stok Habis".equalsIgnoreCase(ketersediaan)) {
                        lblStatusPinjam.setForeground(DANGER_COLOR);
                        lblStatusPinjam.setText("✗  Buku \"" + b.getJudulBuku() + "\" stok kosong, tidak dapat dipinjam.");
                        btnKirimPinjam.setEnabled(false);
                        return;
                    }

                    // Cek tambahan: apakah ada record aktif di tabel_peminjam?
                    try {
                        String qAktif = "SELECT COUNT(*) FROM tabel_peminjam WHERE id_buku = ?";
                        try (PreparedStatement stmtAktif = koneksi.prepareStatement(qAktif)) {
                            stmtAktif.setInt(1, id);
                            try (ResultSet rsAktif = stmtAktif.executeQuery()) {
                                if (rsAktif.next() && rsAktif.getInt(1) > 0) {
                                    lblStatusPinjam.setForeground(DANGER_COLOR);
                                    lblStatusPinjam.setText("✗  Buku \"" + b.getJudulBuku() + "\" sedang dipinjam orang lain.");
                                    btnKirimPinjam.setEnabled(false);
                                    return;
                                }
                            }
                        }
                    } catch (SQLException ex) {
                        lblStatusPinjam.setForeground(DANGER_COLOR);
                        lblStatusPinjam.setText("✗  Gagal memeriksa status peminjaman: " + ex.getMessage());
                        btnKirimPinjam.setEnabled(false);
                        return;
                    }

                    if ("Dipinjam".equalsIgnoreCase(ketersediaan)) {
                        lblStatusPinjam.setForeground(DANGER_COLOR);
                        lblStatusPinjam.setText("✗  Buku \"" + b.getJudulBuku() + "\" sedang dipinjam orang lain.");
                        btnKirimPinjam.setEnabled(false);
                    } else {
                        lblStatusPinjam.setForeground(new Color(30, 130, 60));
                        lblStatusPinjam.setText("✓  Buku ditemukan: " + b.getJudulBuku() + "  [Tersedia]");
                        btnKirimPinjam.setEnabled(true);
                    }
                }
            } catch (NumberFormatException ex) {
                lblStatusPinjam.setForeground(DANGER_COLOR);
                lblStatusPinjam.setText("✗  ID buku harus berupa angka.");
            }
        });

        // ── Event: simpan peminjaman ──────────────────────────────────────────
        btnKirimPinjam.addActionListener(e -> {
            String nama    = pinjamNama.getText().trim();
            String idStr   = pinjamIdBuku.getText().trim();
            String judul   = pinjamJudulBuku.getText().trim();
            String tglDiStr = pinjamTanggalDipinjam.getText().trim();
            String tglKbStr = pinjamTanggalKembali.getText().trim();

            // Validasi field wajib
            if (nama.isEmpty() || idStr.isEmpty() || tglDiStr.isEmpty() || tglKbStr.isEmpty()) {
                lblStatusPinjam.setForeground(DANGER_COLOR);
                lblStatusPinjam.setText("✗  Nama, ID Buku, dan kedua tanggal wajib diisi.");
                return;
            }

            int idBuku;
            try {
                idBuku = Integer.parseInt(idStr);
            } catch (NumberFormatException ex) {
                lblStatusPinjam.setForeground(DANGER_COLOR);
                lblStatusPinjam.setText("✗  ID buku harus berupa angka.");
                return;
            }

            // Validasi: judul di form harus cocok dengan data di DB + cek ketersediaan terkini
            Buku bukuDB = BookController.cariBukuById(idBuku, koneksi);
            if (bukuDB == null) {
                lblStatusPinjam.setForeground(DANGER_COLOR);
                lblStatusPinjam.setText("✗  ID buku tidak ditemukan. Klik \"Cek ID\" terlebih dahulu.");
                return;
            }
            if (!judul.equalsIgnoreCase(bukuDB.getJudulBuku())) {
                lblStatusPinjam.setForeground(DANGER_COLOR);
                lblStatusPinjam.setText("✗  Judul tidak sesuai dengan ID buku di database.");
                return;
            }

            // ── Cek 1: ketersediaan di tabel_buku (query fresh langsung ke DB) ──
            try {
                String qKet = "SELECT ketersediaan FROM tabel_buku WHERE id_buku = ?";
                try (PreparedStatement stmtKet = koneksi.prepareStatement(qKet)) {
                    stmtKet.setInt(1, idBuku);
                    try (ResultSet rsKet = stmtKet.executeQuery()) {
                        if (rsKet.next()) {
                            String ket = rsKet.getString("ketersediaan");
                            if ("Stok Habis".equalsIgnoreCase(ket)) {
                                lblStatusPinjam.setForeground(DANGER_COLOR);
                                lblStatusPinjam.setText("✗  Buku \"" + bukuDB.getJudulBuku() + "\" stok kosong, tidak dapat dipinjam.");
                                btnKirimPinjam.setEnabled(false);
                                return;
                            }
                            if ("Dipinjam".equalsIgnoreCase(ket)) {
                                lblStatusPinjam.setForeground(DANGER_COLOR);
                                lblStatusPinjam.setText("✗  Buku \"" + bukuDB.getJudulBuku() + "\" sedang dipinjam orang lain.");
                                btnKirimPinjam.setEnabled(false);
                                return;
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                lblStatusPinjam.setForeground(DANGER_COLOR);
                lblStatusPinjam.setText("✗  Gagal memeriksa ketersediaan buku: " + ex.getMessage());
                return;
            }

            // ── Cek 2: pastikan tidak ada record aktif di tabel_peminjam ─────────
            // (safeguard jika kolom ketersediaan tidak sinkron dengan tabel_peminjam)
            try {
                String qAktif = "SELECT COUNT(*) FROM tabel_peminjam WHERE id_buku = ?";
                try (PreparedStatement stmtAktif = koneksi.prepareStatement(qAktif)) {
                    stmtAktif.setInt(1, idBuku);
                    try (ResultSet rsAktif = stmtAktif.executeQuery()) {
                        if (rsAktif.next() && rsAktif.getInt(1) > 0) {
                            lblStatusPinjam.setForeground(DANGER_COLOR);
                            lblStatusPinjam.setText("✗  Buku \"" + bukuDB.getJudulBuku() + "\" masih tercatat aktif dipinjam.");
                            btnKirimPinjam.setEnabled(false);
                            return;
                        }
                    }
                }
            } catch (SQLException ex) {
                lblStatusPinjam.setForeground(DANGER_COLOR);
                lblStatusPinjam.setText("✗  Gagal memeriksa data peminjaman aktif: " + ex.getMessage());
                return;
            }

            // Parse tanggal
            LocalDate tglDipinjam, tglKembali;
            try {
                tglDipinjam = LocalDate.parse(tglDiStr);
                tglKembali  = LocalDate.parse(tglKbStr);
            } catch (Exception ex) {
                lblStatusPinjam.setForeground(DANGER_COLOR);
                lblStatusPinjam.setText("✗  Format tanggal salah. Gunakan YYYY-MM-DD.");
                return;
            }

            // Kirim ke controller
            boolean berhasil = PeminjamanController.buatPeminjaman(
                nama, idBuku, judul, tglDipinjam, tglKembali, koneksi
            );

            if (berhasil) {
                // Update ketersediaan buku menjadi "Dipinjam"
                try {
                    String queryUpdate = "UPDATE tabel_buku SET ketersediaan = 'Dipinjam' WHERE id_buku = ?";
                    try (PreparedStatement stmtUpdate = koneksi.prepareStatement(queryUpdate)) {
                        stmtUpdate.setInt(1, idBuku);
                        stmtUpdate.executeUpdate();
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Peminjaman tersimpan, tetapi gagal memperbarui status ketersediaan buku:\n" + ex.getMessage(),
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                }

                lblStatusPinjam.setForeground(new Color(30, 130, 60));
                lblStatusPinjam.setText("✓  Peminjaman berhasil disimpan. Status buku diubah menjadi Dipinjam.");
                resetFormPinjam();
                btnKirimPinjam.setEnabled(true);
                muatSemuaPeminjaman(); // refresh tab daftar
                muatSemuaBuku();       // refresh tab daftar buku
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal menyimpan data peminjaman.\nPeriksa koneksi database.",
                    "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });

        JScrollPane scroll = new JScrollPane(formPanel);
        scroll.setBorder(null);
        panelBuatPeminjaman.add(scroll, BorderLayout.CENTER);
    }

    // ── TAB 5 — DAFTAR PEMINJAMAN ─────────────────────────────────────────────
    private void initTabDaftarPeminjaman() {
        // ── Toolbar ───────────────────────────────────────────────────────────
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        toolBar.setBackground(BG_COLOR);
        btnRefreshPeminjaman = buatTombol("🔄  Refresh", new Color(80, 140, 80));
        toolBar.add(btnRefreshPeminjaman);

        JLabel lblKet = new JLabel(
            "Kolom \"Denda\" dihitung otomatis dari keterlambatan  |  " +
            "Denda Rp " + DENDA_PER_HARI + " / hari");
        lblKet.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblKet.setForeground(Color.GRAY);
        toolBar.add(lblKet);

        panelDaftarPeminjaman.add(toolBar, BorderLayout.NORTH);

        // ── Tabel ─────────────────────────────────────────────────────────────
        modelTabelPeminjaman = new DefaultTableModel(COL_TABEL_PEMINJAMAN, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 8; // hanya kolom "Aksi" yang bisa diklik
            }
        };

        tabelPeminjaman = new JTable(modelTabelPeminjaman);
        tabelPeminjaman.setFont(FONT_NORMAL);
        tabelPeminjaman.setRowHeight(32);
        tabelPeminjaman.getTableHeader().setFont(FONT_LABEL);
        tabelPeminjaman.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Lebar kolom
        tabelPeminjaman.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        tabelPeminjaman.getColumnModel().getColumn(2).setPreferredWidth(55);  // ID Buku
        tabelPeminjaman.getColumnModel().getColumn(4).setPreferredWidth(90);  // Tgl Dipinjam
        tabelPeminjaman.getColumnModel().getColumn(5).setPreferredWidth(90);  // Tgl Kembali
        tabelPeminjaman.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status
        tabelPeminjaman.getColumnModel().getColumn(7).setPreferredWidth(90);  // Denda
        tabelPeminjaman.getColumnModel().getColumn(8).setPreferredWidth(110); // Aksi

        // Render & editor tombol "Kembalikan" di kolom Aksi
        tabelPeminjaman.getColumn("Aksi").setCellRenderer(new TombolKembalikanRenderer());
        tabelPeminjaman.getColumn("Aksi").setCellEditor(
            new TombolKembalikanEditor(new JCheckBox(), this));

        panelDaftarPeminjaman.add(new JScrollPane(tabelPeminjaman), BorderLayout.CENTER);

        // ── Event refresh ─────────────────────────────────────────────────────
        btnRefreshPeminjaman.addActionListener(e -> muatSemuaPeminjaman());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS — Data & UI
    // ═════════════════════════════════════════════════════════════════════════

    /** Muat semua data peminjaman ke tabel, hitung denda & status otomatis. */
    void muatSemuaPeminjaman() {
        modelTabelPeminjaman.setRowCount(0);

        String query =
            "SELECT id_peminjam, nama_peminjam, id_buku, judul_buku, " +
            "tanggal_dipinjam, tanggal_kembali " +
            "FROM tabel_peminjam ORDER BY id_peminjam";

        try (PreparedStatement stmt = koneksi.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            LocalDate hari_ini = LocalDate.now();

            while (rs.next()) {
                int    idPeminjam  = rs.getInt("id_peminjam");
                String nama        = rs.getString("nama_peminjam");
                int    idBuku      = rs.getInt("id_buku");
                String judul       = rs.getString("judul_buku");

                Date sqlDipinjam = rs.getDate("tanggal_dipinjam");
                Date sqlKembali  = rs.getDate("tanggal_kembali");

                LocalDate tglDipinjam = sqlDipinjam != null ? sqlDipinjam.toLocalDate() : null;
                LocalDate tglKembali  = sqlKembali  != null ? sqlKembali.toLocalDate()  : null;

                // Hitung status & denda
                String status = "Dipinjam";
                long   denda  = 0;

                if (tglKembali != null && hari_ini.isAfter(tglKembali)) {
                    long hariTerlambat = ChronoUnit.DAYS.between(tglKembali, hari_ini);
                    denda  = hariTerlambat * DENDA_PER_HARI;
                    status = "Terlambat (" + hariTerlambat + " hari)";
                }

                modelTabelPeminjaman.addRow(new Object[]{
                    idPeminjam,
                    nama,
                    idBuku,
                    judul,
                    tglDipinjam != null ? tglDipinjam.toString() : "-",
                    tglKembali  != null ? tglKembali.toString()  : "-",
                    status,
                    denda == 0 ? "-" : "Rp " + denda,
                    "Kembalikan"   // teks tombol di kolom Aksi
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Gagal memuat data peminjaman:\n" + ex.getMessage(),
                "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Kembalikan buku: hapus baris peminjaman + ubah ketersediaan buku ke Tersedia. */
    void kembalikanBuku(int row) {
        int    idPeminjam = (int)    modelTabelPeminjaman.getValueAt(row, 0);
        int    idBuku     = (int)    modelTabelPeminjaman.getValueAt(row, 2);
        String namaPeminjam = (String) modelTabelPeminjaman.getValueAt(row, 1);

        int pilihan = JOptionPane.showConfirmDialog(
            this,
            "Kembalikan buku dari: " + namaPeminjam + "?\n" +
            "Status buku akan diubah menjadi 'Tersedia'.",
            "Konfirmasi Pengembalian",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (pilihan != JOptionPane.YES_OPTION) return;

        try {
            // 1. Hapus baris peminjaman
            String queryHapus = "DELETE FROM tabel_peminjam WHERE id_peminjam = ?";
            try (PreparedStatement stmt = koneksi.prepareStatement(queryHapus)) {
                stmt.setInt(1, idPeminjam);
                stmt.executeUpdate();
            }

            // 2. Update ketersediaan buku menjadi Tersedia
            String queryUpdate = "UPDATE tabel_buku SET ketersediaan = 'Tersedia' WHERE id_buku = ?";
            try (PreparedStatement stmt = koneksi.prepareStatement(queryUpdate)) {
                stmt.setInt(1, idBuku);
                stmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                "Buku berhasil dikembalikan.",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);

            muatSemuaPeminjaman();
            muatSemuaBuku(); // refresh tab daftar buku juga

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Gagal memproses pengembalian:\n" + ex.getMessage(),
                "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Reset form buat peminjaman ke keadaan kosong. */
    private void resetFormPinjam() {
        pinjamNama.setText("");
        pinjamIdBuku.setText("");
        pinjamJudulBuku.setText("");
        pinjamTanggalDipinjam.setText("YYYY-MM-DD");
        pinjamTanggalKembali.setText("YYYY-MM-DD");
    }

    // ── Inner class: Renderer tombol Kembalikan ───────────────────────────────
    private static class TombolKembalikanRenderer implements TableCellRenderer {
        private final JButton btn = new JButton("Kembalikan");

        TombolKembalikanRenderer() {
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setBackground(new Color(192, 57, 43));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return btn;
        }
    }

    // ── Inner class: Editor tombol Kembalikan (agar bisa diklik) ─────────────
    private static class TombolKembalikanEditor extends DefaultCellEditor {
        private final JButton btn;
        private final HalamanInventaris parent;
        private int currentRow;

        TombolKembalikanEditor(JCheckBox checkBox, HalamanInventaris parent) {
            super(checkBox);
            this.parent = parent;
            btn = new JButton("Kembalikan");
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setBackground(new Color(192, 57, 43));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

            btn.addActionListener(e -> {
                fireEditingStopped();
                parent.kembalikanBuku(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return btn;
        }

        @Override
        public Object getCellEditorValue() { return "Kembalikan"; }
    }
    private void muatSemuaBuku() {
        modelTabel.setRowCount(0);
        String query = "SELECT id_buku, judul_buku, penulis_buku, penerbit_buku, " +
                       "tahun_terbit_buku, sinopsis_buku, lokasi_cover_buku, ketersediaan " +
                       "FROM tabel_buku ORDER BY id_buku";
        try (PreparedStatement stmt = koneksi.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                modelTabel.addRow(new Object[]{
                    rs.getInt("id_buku"),
                    rs.getString("judul_buku"),
                    rs.getString("penulis_buku"),
                    rs.getString("penerbit_buku"),
                    rs.getInt("tahun_terbit_buku"),
                    rs.getString("sinopsis_buku"),
                    rs.getString("lokasi_cover_buku"),
                    rs.getString("ketersediaan")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Gagal memuat data buku:\n" + ex.getMessage(),
                "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Isi form ubah dengan data dari objek Buku. */
    private void isiFormUbah(Buku b) {
        ubahJudul.setText(b.getJudulBuku());
        ubahPenulis.setText(b.getPenulisBuku());
        ubahPenerbit.setText(b.getPenerbitBuku());
        ubahTahun.setText(String.valueOf(b.getTahunTerbitBuku()));
        ubahSinopsis.setText(b.getSinopsisBuku());
        ubahCover.setText(b.getLokasiCoverBuku());

        // Set radio ketersediaan
        String ket = b.getKetersediaan() != null ? b.getKetersediaan() : "Tersedia";
        switch (ket) {
            case "Dipinjam":
                ubahRbDipinjam.setSelected(true);
                break;
            case "Stok Habis":
                ubahRbStokHabis.setSelected(true);
                break;
            default:
                ubahRbTersedia.setSelected(true);
                break;
        }
    }

    /** Reset form buat buku ke keadaan kosong. */
    private void resetFormBuat() {
        createJudul.setText("");
        createPenulis.setText("");
        createPenerbit.setText("");
        createTahun.setText("");
        createSinopsis.setText("");
        createCover.setText("");
        createRbTersedia.setSelected(true);
    }

    /** Enable / disable semua field form ubah. */
    private void setEnabledFormUbah(boolean enabled) {
        for (JComponent c : new JComponent[]{
                ubahJudul, ubahPenulis, ubahPenerbit, ubahTahun,
                ubahSinopsis, ubahCover,
                ubahRbTersedia, ubahRbDipinjam, ubahRbStokHabis}) {
            c.setEnabled(enabled);
        }
    }

    /** Buka JFileChooser dan taruh path-nya di field target. */
    private void pilihFile(JTextField target) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Gambar (jpg, png, gif)", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            target.setText(file.getAbsolutePath());
        }
    }

    /** Ambil teks radio button yang sedang dipilih dari ButtonGroup. */
    private String getSelectedRadio(ButtonGroup group, JRadioButton[] radios) {
        for (JRadioButton rb : radios) {
            if (group.isSelected(rb.getModel())) return rb.getText();
        }
        return "Tersedia";
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS — Pembantu UI (Factory Methods)
    // ═════════════════════════════════════════════════════════════════════════

    private JLabel buatLabel(String teks) {
        JLabel lbl = new JLabel(teks);
        lbl.setFont(FONT_LABEL);
        return lbl;
    }

    private JButton buatTombol(String teks, Color warna) {
        JButton btn = new JButton(teks);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(warna);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private GridBagConstraints defaultGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    /** Tambah baris label + komponen ke GridBagLayout. */
    private void tambahBarisForms(JPanel panel, GridBagConstraints gbc,
                                   int baris, String labelTeks,
                                   JComponent komponen, JComponent extra) {
        gbc.gridy  = baris;
        gbc.gridx  = 0;
        gbc.weightx = 0;
        panel.add(buatLabel(labelTeks), gbc);

        gbc.gridx   = 1;
        gbc.weightx = 1.0;
        panel.add(komponen, gbc);

        if (extra != null) {
            gbc.gridx   = 2;
            gbc.weightx = 0;
            panel.add(extra, gbc);
        }
    }

    /** Baris khusus cover: field + tombol pilih file. */
    private void tambahBarisCover(JPanel panel, GridBagConstraints gbc,
                                   int baris, JTextField fieldCover, JButton btnPilih) {
        gbc.gridy   = baris;
        gbc.gridx   = 0;
        gbc.weightx = 0;
        panel.add(buatLabel("Cover Buku"), gbc);

        gbc.gridx   = 1;
        gbc.weightx = 1.0;
        panel.add(fieldCover, gbc);

        gbc.gridx   = 2;
        gbc.weightx = 0;
        panel.add(btnPilih, gbc);
    }
}