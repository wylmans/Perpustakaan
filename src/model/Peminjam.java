package model;

import java.time.LocalDate;

public class Peminjam {

    private int       idPeminjam;
    private String    namaPeminjam;
    private int       idBuku;
    private String    judulBuku;      // Join dari tabel_buku, untuk tampilan
    private LocalDate tanggalDipinjam;
    private LocalDate tanggalKembali;

    public Peminjam() {}

    public Peminjam(int idPeminjam, String namaPeminjam, int idBuku,
                    String judulBuku, LocalDate tanggalDipinjam, LocalDate tanggalKembali) {
        this.idPeminjam      = idPeminjam;
        this.namaPeminjam    = namaPeminjam;
        this.idBuku          = idBuku;
        this.judulBuku       = judulBuku;
        this.tanggalDipinjam = tanggalDipinjam;
        this.tanggalKembali  = tanggalKembali;
    }

    public int       getIdPeminjam()      { return idPeminjam; }
    public String    getNamaPeminjam()    { return namaPeminjam; }
    public int       getIdBuku()          { return idBuku; }
    public String    getJudulBuku()       { return judulBuku; }
    public LocalDate getTanggalDipinjam() { return tanggalDipinjam; }
    public LocalDate getTanggalKembali()  { return tanggalKembali; }

    public void setIdPeminjam(int idPeminjam)           { this.idPeminjam = idPeminjam; }
    public void setNamaPeminjam(String namaPeminjam)     { this.namaPeminjam = namaPeminjam; }
    public void setIdBuku(int idBuku)                   { this.idBuku = idBuku; }
    public void setJudulBuku(String judulBuku)           { this.judulBuku = judulBuku; }
    public void setTanggalDipinjam(LocalDate tanggal)   { this.tanggalDipinjam = tanggal; }
    public void setTanggalKembali(LocalDate tanggal)    { this.tanggalKembali = tanggal; }
}