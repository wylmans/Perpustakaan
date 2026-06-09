package ui;

import controller.BookController;
import controller.CreateBookController;
import database.DatabaseHelper;
import model.Buku;
import model.Peminjam;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HalamanCRUDAdmin extends JFrame {

    private static final int WIDTH  = 1080;
    private static final int HEIGHT = 720;

    // ── Warna tema (mengikuti desain dark sidebar) ────────────────────────────────
    private static final Color SIDEBAR_BG   = new Color(28, 30, 43);
    private static final Color SIDEBAR_AKTIF= new Color(67, 97, 238);
    private static final Color MAIN_BG      = new Color(245, 246, 250);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color TEXT_PRIMER  = new Color(30, 30, 47);
    private static final Color TEXT_SEKUNDER= new Color(120, 120, 140);
    private static final Color HIJAU        = new Color(34, 197, 94);
    private static final Color ORANYE       = new Color(251, 146, 60);
    private static final Color MERAH        = new Color(239, 68, 68);

    // ── Komponen utama ────────────────────────────────────────────────────────────
    private JPanel      panelSidebar;
    private JPanel      panelKonten;
    private JTable      tabelBuku;
    private DefaultTableModel modelTabel;
    private JLabel      lblTotalBuku, lblTersedia, lblDipinjam, lblStokHabis;

    // ── Dependensi ────────────────────────────────────────────────────────────────
    private final Connection koneksi;
    private final String[]   stopWord;
    private final JFrame     halamanSebelumnya;

    // ── Constructor ───────────────────────────────────────────────────────────────
    public HalamanCRUDAdmin(Connection koneksi, String[] stopWord, JFrame halamanSebelumnya) {
        this.koneksi           = koneksi;
        this.stopWord          = stopWord;
        this.halamanSebelumnya = halamanSebelumnya;

        setTitle("Pustaka Admin Panel");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                dispose();
                if (halamanSebelumnya != null) halamanSebelumnya.setVisible(true);
            }
        });

        bangunSidebar();
        bangunKontenKoleksiBuku();
        muatDataBuku();

        setVisible(true);
    }

    // ── SIDEBAR ───────────────────────────────────────────────────────────────────
    private void bangunSidebar() {
        panelSidebar = new JPanel();
        panelSidebar.setPreferredSize(new Dimension(220, HEIGHT));
        panelSidebar.setBackground(SIDEBAR_BG);
        panelSidebar.setLayout(null);

        // Logo & judul
        JLabel lblLogo = new JLabel("Pustaka");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Arial", Font.BOLD, 16));
        lblLogo.setBounds(60, 30, 140, 20);
        panelSidebar.add(lblLogo);

        JLabel lblSubjudul = new JLabel("Admin Panel");
        lblSubjudul.setForeground(new Color(160, 160, 190));
        lblSubjudul.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubjudul.setBounds(60, 50, 140, 18);
        panelSidebar.add(lblSubjudul);

        JLabel lblVersi = new JLabel("v2.1.0");
        lblVersi.setForeground(new Color(100, 100, 130));
        lblVersi.setFont(new Font("Arial", Font.PLAIN, 11));
        lblVersi.setBounds(60, 68, 100, 16);
        panelSidebar.add(lblVersi);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setBounds(20, 100, 180, 1);
        sep.setForeground(new Color(50, 52, 70));
        panelSidebar.add(sep);

        // Label MENU
        JLabel lblMenu = new JLabel("MENU");
        lblMenu.setForeground(new Color(100, 100, 130));
        lblMenu.setFont(new Font("Arial", Font.BOLD, 11));
        lblMenu.setBounds(20, 115, 100, 20);
        panelSidebar.add(lblMenu);

        // Tombol menu navigasi
        buatTombolSidebar("Koleksi Buku", 145, true);
        buatTombolSidebar("Anggota",      195, false);
        buatTombolSidebar("Peminjaman",   245, false);
        buatTombolSidebar("Laporan",      295, false);

        // Label PENGATURAN
        JLabel lblPengaturan = new JLabel("PENGATURAN");
        lblPengaturan.setForeground(new Color(100, 100, 130));
        lblPengaturan.setFont(new Font("Arial", Font.BOLD, 11));
        lblPengaturan.setBounds(20, 355, 140, 20);
        panelSidebar.add(lblPengaturan);

        buatTombolSidebar("Konfigurasi", 383, false);

        // Info admin di bawah
        JLabel lblAdmin = new JLabel("Admin");
        lblAdmin.setForeground(Color.WHITE);
        lblAdmin.setFont(new Font("Arial", Font.BOLD, 13));
        lblAdmin.setBounds(60, HEIGHT - 80, 120, 20);
        panelSidebar.add(lblAdmin);

        JLabel lblRole = new JLabel("Super Admin");
        lblRole.setForeground(new Color(140, 140, 170));
        lblRole.setFont(new Font("Arial", Font.PLAIN, 11));
        lblRole.setBounds(60, HEIGHT - 62, 120, 16);
        panelSidebar.add(lblRole);

        add(panelSidebar, BorderLayout.WEST);
    }

    private void buatTombolSidebar(String teks, int y, boolean aktif) {
        JButton btn = new JButton(teks);
        btn.setBounds(10, y, 200, 38);
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (aktif) {
            btn.setBackground(SIDEBAR_AKTIF);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(SIDEBAR_BG);
            btn.setForeground(new Color(180, 180, 210));
        }

        panelSidebar.add(btn);
    }

    // ── KONTEN UTAMA: KOLEKSI BUKU ────────────────────────────────────────────────
    private void bangunKontenKoleksiBuku() {
        panelKonten = new JPanel();
        panelKonten.setBackground(MAIN_BG);
        panelKonten.setLayout(null);

        // ── Header ────────────────────────────────────────────────────────────────
        JLabel lblJudul = new JLabel("Manajemen Koleksi Buku");
        lblJudul.setFont(new Font("Arial", Font.BOLD, 22));
        lblJudul.setForeground(TEXT_PRIMER);
        lblJudul.setBounds(20, 20, 400, 30);
        panelKonten.add(lblJudul);

        JLabel lblSub = new JLabel("Kelola seluruh data buku perpustakaan");
        lblSub.setFont(new Font("Arial", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_SEKUNDER);
        lblSub.setBounds(20, 50, 400, 20);
        panelKonten.add(lblSub);

        // Tombol Ekspor
        JButton btnEkspor = new JButton("↓  Ekspor");
        btnEkspor.setBounds(WIDTH - 240 - 220, 25, 120, 36);
        btnEkspor.setFont(new Font("Arial", Font.PLAIN, 13));
        btnEkspor.setBackground(CARD_BG);
        btnEkspor.setForeground(TEXT_PRIMER);
        btnEkspor.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 220)));
        btnEkspor.setFocusPainted(false);
        panelKonten.add(btnEkspor);

        // Tombol Tambah Buku
        JButton btnTambah = new JButton("+  Tambah Buku");
        btnTambah.setBounds(WIDTH - 240 - 90, 25, 150, 36);
        btnTambah.setFont(new Font("Arial", Font.BOLD, 13));
        btnTambah.setBackground(SIDEBAR_AKTIF);
        btnTambah.setForeground(Color.WHITE);
        btnTambah.setBorderPainted(false);
        btnTambah.setFocusPainted(false);
        btnTambah.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTambah.addActionListener(e -> bukaFormTambahBuku());
        panelKonten.add(btnTambah);

        // ── Kartu statistik ───────────────────────────────────────────────────────
        buatKartuStatistik("Total Buku",  "0", "42 judul berbeda",  TEXT_PRIMER, 20,  90,  true);
        buatKartuStatistik("Tersedia",    "0", "dari total",        HIJAU,       200, 90,  false);
        buatKartuStatistik("Dipinjam",    "0", "dari total",        ORANYE,      380, 90,  false);
        buatKartuStatistik("Stok Habis",  "0", "Perlu restock",     MERAH,       560, 90,  false);

        // ── Filter bar ────────────────────────────────────────────────────────────
        JComboBox<String> cbStatus = new JComboBox<>(
                new String[]{"Semua status", "Tersedia", "Dipinjam", "Stok Habis"});
        cbStatus.setBounds(20, 220, 180, 34);
        cbStatus.setFont(new Font("Arial", Font.PLAIN, 13));
        panelKonten.add(cbStatus);

        JComboBox<String> cbTahun = new JComboBox<>(
                new String[]{"Semua tahun", "2024", "2023", "2022", "2021", "2020", "Sebelum 2020"});
        cbTahun.setBounds(210, 220, 160, 34);
        cbTahun.setFont(new Font("Arial", Font.PLAIN, 13));
        panelKonten.add(cbTahun);

        cbStatus.addActionListener(e -> filterTabel(
                (String) cbStatus.getSelectedItem(),
                (String) cbTahun.getSelectedItem()));
        cbTahun.addActionListener(e -> filterTabel(
                (String) cbStatus.getSelectedItem(),
                (String) cbTahun.getSelectedItem()));

        // ── Tabel buku ────────────────────────────────────────────────────────────
        String[] kolomHeader = {"#", "Judul Buku", "Penulis", "Penerbit", "Tahun", "Status", "Aksi"};
        modelTabel = new DefaultTableModel(kolomHeader, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 6; }
        };

        tabelBuku = new JTable(modelTabel);
        tabelBuku.setFont(new Font("Arial", Font.PLAIN, 13));
        tabelBuku.setRowHeight(52);
        tabelBuku.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabelBuku.getTableHeader().setBackground(MAIN_BG);
        tabelBuku.setGridColor(new Color(230, 230, 240));
        tabelBuku.setShowVerticalLines(false);
        tabelBuku.setSelectionBackground(new Color(235, 240, 255));

        // Lebar kolom
        tabelBuku.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabelBuku.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabelBuku.getColumnModel().getColumn(2).setPreferredWidth(140);
        tabelBuku.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabelBuku.getColumnModel().getColumn(4).setPreferredWidth(60);
        tabelBuku.getColumnModel().getColumn(5).setPreferredWidth(100);
        tabelBuku.getColumnModel().getColumn(6).setPreferredWidth(120);

        // Renderer kolom status (badge warna)
        tabelBuku.getColumnModel().getColumn(5).setCellRenderer(new BadgeStatusRenderer());

        // Renderer & editor kolom aksi (tombol Edit & Hapus)
        tabelBuku.getColumnModel().getColumn(6).setCellRenderer(new AksiRenderer());
        tabelBuku.getColumnModel().getColumn(6).setCellEditor(new AksiEditor());

        JScrollPane scrollPane = new JScrollPane(tabelBuku);
        scrollPane.setBounds(20, 265, WIDTH - 240, HEIGHT - 290);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 230)));
        scrollPane.getViewport().setBackground(CARD_BG);
        panelKonten.add(scrollPane);

        add(panelKonten, BorderLayout.CENTER);
    }

    private void buatKartuStatistik(String judul, String nilai, String sub,
                                     Color warnaJudul, int x, int y, boolean isPrimer) {
        JPanel kartu = new JPanel();
        kartu.setBounds(x, y, 170, 110);
        kartu.setBackground(CARD_BG);
        kartu.setLayout(null);
        kartu.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 230)));

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font("Arial", Font.PLAIN, 12));
        lblJudul.setForeground(warnaJudul);
        lblJudul.setBounds(12, 12, 150, 18);
        kartu.add(lblJudul);

        JLabel lblNilai = new JLabel(nilai);
        lblNilai.setFont(new Font("Arial", Font.BOLD, 32));
        lblNilai.setForeground(TEXT_PRIMER);
        lblNilai.setBounds(12, 35, 150, 40);
        kartu.add(lblNilai);

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(new Font("Arial", Font.PLAIN, 11));
        lblSub.setForeground(TEXT_SEKUNDER);
        lblSub.setBounds(12, 78, 150, 18);
        kartu.add(lblSub);

        // Simpan referensi label nilai untuk diupdate nanti
        if (judul.equals("Total Buku")) lblTotalBuku  = lblNilai;
        if (judul.equals("Tersedia"))   lblTersedia   = lblNilai;
        if (judul.equals("Dipinjam"))   lblDipinjam   = lblNilai;
        if (judul.equals("Stok Habis")) lblStokHabis  = lblNilai;

        panelKonten.add(kartu);
    }

    // ── MUAT DATA DARI DATABASE ───────────────────────────────────────────────────
    private void muatDataBuku() {
        modelTabel.setRowCount(0);
        List<Buku> daftarBuku = DatabaseHelper.ambilSemuaBukuDariDatabase(koneksi);

        int total = daftarBuku.size();
        int tersedia = 0, dipinjam = 0, stokHabis = 0;

        for (int i = 0; i < daftarBuku.size(); i++) {
            Buku b = daftarBuku.get(i);
            modelTabel.addRow(new Object[]{
                i + 1,
                b.getJudulBuku(),
                b.getPenulisBuku(),
                b.getPenerbitBuku(),
                b.getTahunTerbitBuku(),
                b.getKetersediaan(),
                b.getIdBuku()   // Simpan ID untuk aksi edit/hapus
            });

            switch (b.getKetersediaan()) {
                case "Tersedia":   tersedia++;   break;
                case "Dipinjam":   dipinjam++;   break;
                case "Stok Habis": stokHabis++;  break;
                default: break;
            }
        }

        // Update kartu statistik
        lblTotalBuku.setText(String.valueOf(total));
        lblTersedia.setText(String.valueOf(tersedia));
        lblDipinjam.setText(String.valueOf(dipinjam));
        lblStokHabis.setText(String.valueOf(stokHabis));
    }

    // ── FILTER TABEL ─────────────────────────────────────────────────────────────
    private void filterTabel(String status, String tahun) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelTabel);
        tabelBuku.setRowSorter(sorter);

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!"Semua status".equals(status))
            filters.add(RowFilter.regexFilter(status, 5));
        if (!"Semua tahun".equals(tahun) && !"Sebelum 2020".equals(tahun))
            filters.add(RowFilter.regexFilter(tahun, 4));

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    // ── FORM TAMBAH BUKU ──────────────────────────────────────────────────────────
    private void bukaFormTambahBuku() {
        JDialog dialog = new JDialog(this, "Tambah Buku Baru", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(null);

        String[] labels = {"Judul Buku", "Penulis", "Penerbit", "Tahun Terbit",
                           "Lokasi Cover", "Ketersediaan"};
        JComponent[] fields = new JComponent[labels.length];

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i] + ":");
            lbl.setBounds(20, 20 + i * 55, 120, 25);
            dialog.add(lbl);

            if (labels[i].equals("Ketersediaan")) {
                fields[i] = new JComboBox<>(new String[]{"Tersedia", "Dipinjam", "Stok Habis"});
            } else {
                fields[i] = new JTextField();
            }
            fields[i].setBounds(150, 20 + i * 55, 290, 30);
            dialog.add(fields[i]);
        }

        JLabel lblSinopsis = new JLabel("Sinopsis:");
        lblSinopsis.setBounds(20, 350, 120, 25);
        dialog.add(lblSinopsis);

        JTextArea areaSinopsis = new JTextArea();
        areaSinopsis.setLineWrap(true);
        JScrollPane scrollSinopsis = new JScrollPane(areaSinopsis);
        scrollSinopsis.setBounds(150, 350, 290, 70);
        dialog.add(scrollSinopsis);

        JButton btnSimpan = new JButton("Simpan");
        btnSimpan.setBounds(280, 435, 100, 32);
        btnSimpan.setBackground(SIDEBAR_AKTIF);
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setBorderPainted(false);
        btnSimpan.addActionListener(e -> {
            String judul    = ((JTextField) fields[0]).getText().trim();
            String penulis  = ((JTextField) fields[1]).getText().trim();
            String penerbit = ((JTextField) fields[2]).getText().trim();
            String cover    = ((JTextField) fields[4]).getText().trim();
            String status   = (String) ((JComboBox<?>) fields[5]).getSelectedItem();
            String sinopsis = areaSinopsis.getText().trim();
            int tahun = 0;
            try { tahun = Integer.parseInt(((JTextField) fields[3]).getText().trim()); }
            catch (NumberFormatException ignored) {}

            if (judul.isEmpty() || sinopsis.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Judul dan sinopsis wajib diisi!", "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            CreateBookController.simpanBuku(
                    judul, penulis, penerbit, cover, tahun, sinopsis, status, stopWord, koneksi);
            muatDataBuku();
            dialog.dispose();
        });
        dialog.add(btnSimpan);

        JButton btnBatal = new JButton("Batal");
        btnBatal.setBounds(170, 435, 100, 32);
        btnBatal.addActionListener(e -> dialog.dispose());
        dialog.add(btnBatal);

        dialog.setVisible(true);
    }

    // ── AKSI EDIT ────────────────────────────────────────────────────────────────
    private void aksiEdit(int idBuku) {
        Buku buku = BookController.cariBukuById(idBuku, koneksi);
        if (buku == null) return;

        JDialog dialog = new JDialog(this, "Edit Buku", true);
        dialog.setSize(480, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(null);

        String[] labels = {"Judul Buku", "Penulis", "Penerbit", "Tahun Terbit", "Lokasi Cover", "Ketersediaan"};
        String[] nilai  = {buku.getJudulBuku(), buku.getPenulisBuku(), buku.getPenerbitBuku(),
                           String.valueOf(buku.getTahunTerbitBuku()), buku.getLokasiCoverBuku(),
                           buku.getKetersediaan()};
        JComponent[] fields = new JComponent[labels.length];

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i] + ":");
            lbl.setBounds(20, 20 + i * 55, 120, 25);
            dialog.add(lbl);

            if (labels[i].equals("Ketersediaan")) {
                JComboBox<String> cb = new JComboBox<>(new String[]{"Tersedia", "Dipinjam", "Stok Habis"});
                cb.setSelectedItem(nilai[i]);
                fields[i] = cb;
            } else {
                fields[i] = new JTextField(nilai[i]);
            }
            fields[i].setBounds(150, 20 + i * 55, 290, 30);
            dialog.add(fields[i]);
        }

        JLabel lblSinopsis = new JLabel("Sinopsis:");
        lblSinopsis.setBounds(20, 350, 120, 25);
        dialog.add(lblSinopsis);

        JTextArea areaSinopsis = new JTextArea(buku.getSinopsisBuku());
        areaSinopsis.setLineWrap(true);
        JScrollPane scrollSinopsis = new JScrollPane(areaSinopsis);
        scrollSinopsis.setBounds(150, 350, 290, 70);
        dialog.add(scrollSinopsis);

        JButton btnUpdate = new JButton("Simpan");
        btnUpdate.setBounds(280, 435, 100, 32);
        btnUpdate.setBackground(SIDEBAR_AKTIF);
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setBorderPainted(false);
        btnUpdate.addActionListener(e -> {
            BookController.updateBukuDariForm(
                    idBuku,
                    ((JTextField) fields[0]).getText().trim(),
                    ((JTextField) fields[1]).getText().trim(),
                    ((JTextField) fields[2]).getText().trim(),
                    ((JTextField) fields[4]).getText().trim(),
                    ((JTextField) fields[3]).getText().trim(),
                    areaSinopsis.getText().trim(),
                    (String) ((JComboBox<?>) fields[5]).getSelectedItem(),
                    stopWord, koneksi);
            muatDataBuku();
            dialog.dispose();
        });
        dialog.add(btnUpdate);

        JButton btnBatal = new JButton("Batal");
        btnBatal.setBounds(170, 435, 100, 32);
        btnBatal.addActionListener(e -> dialog.dispose());
        dialog.add(btnBatal);

        dialog.setVisible(true);
    }

    // ── AKSI HAPUS ───────────────────────────────────────────────────────────────
    private void aksiHapus(int idBuku, String judulBuku) {
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Hapus buku \"" + judulBuku + "\"?\nSemua keyword terkait juga akan dihapus.",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            BookController.deleteBukuById(idBuku, koneksi);
            muatDataBuku();
        }
    }

    // ── RENDERER BADGE STATUS ─────────────────────────────────────────────────────
    class BadgeStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = new JLabel(value != null ? value.toString() : "");
            lbl.setOpaque(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 11));

            switch (lbl.getText()) {
                case "Tersedia":
                    lbl.setBackground(new Color(220, 252, 231));
                    lbl.setForeground(HIJAU);
                    break;
                case "Dipinjam":
                    lbl.setBackground(new Color(255, 237, 213));
                    lbl.setForeground(ORANYE);
                    break;
                case "Stok Habis":
                    lbl.setBackground(new Color(254, 226, 226));
                    lbl.setForeground(MERAH);
                    break;
                default:
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(TEXT_SEKUNDER);
                    break;
            }
            return lbl;
        }
    }

    // ── RENDERER TOMBOL AKSI ──────────────────────────────────────────────────────
    class AksiRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
            panel.setBackground(Color.WHITE);
            JButton btnEdit  = new JButton("Edit");
            JButton btnHapus = new JButton("Hapus");
            btnEdit.setFont(new Font("Arial", Font.PLAIN, 11));
            btnHapus.setFont(new Font("Arial", Font.PLAIN, 11));
            btnHapus.setForeground(MERAH);
            panel.add(btnEdit);
            panel.add(btnHapus);
            return panel;
        }
    }

    // ── EDITOR TOMBOL AKSI (agar tombol bisa diklik) ──────────────────────────────
    class AksiEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int col) {
            int idBuku    = (int) modelTabel.getValueAt(row, 6);
            String judul  = (String) modelTabel.getValueAt(row, 1);

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
            panel.setBackground(new Color(235, 240, 255));

            JButton btnEdit  = new JButton("Edit");
            JButton btnHapus = new JButton("Hapus");
            btnEdit.setFont(new Font("Arial", Font.PLAIN, 11));
            btnHapus.setFont(new Font("Arial", Font.PLAIN, 11));
            btnHapus.setForeground(MERAH);

            btnEdit.addActionListener(e  -> { stopCellEditing(); aksiEdit(idBuku); });
            btnHapus.addActionListener(e -> { stopCellEditing(); aksiHapus(idBuku, judul); });

            panel.add(btnEdit);
            panel.add(btnHapus);
            return panel;
        }

        @Override public Object getCellEditorValue() { return null; }
    }
}