package model;

public class Buku {

    // Field sesuai skema tabel dari pseudocode Create
    private int    idBuku;
    private String judulBuku;
    private String penulisBuku;
    private String penerbitBuku;
    private String lokasiCoverBuku;
    private int    tahunTerbitBuku;

    // ── Constructor ───────────────────────────────────────────────────────────────

    public Buku() {}

    public Buku(int idBuku, String judulBuku, String penulisBuku,
                String penerbitBuku, String lokasiCoverBuku, int tahunTerbitBuku) {
        this.idBuku          = idBuku;
        this.judulBuku       = judulBuku;
        this.penulisBuku     = penulisBuku;
        this.penerbitBuku    = penerbitBuku;
        this.lokasiCoverBuku = lokasiCoverBuku;
        this.tahunTerbitBuku = tahunTerbitBuku;
    }

    // ── Getter & Setter ───────────────────────────────────────────────────────────

    public int    getIdBuku()            { return idBuku; }
    public String getJudulBuku()         { return judulBuku; }
    public String getPenulisBuku()       { return penulisBuku; }
    public String getPenerbitBuku()      { return penerbitBuku; }
    public String getLokasiCoverBuku()   { return lokasiCoverBuku; }
    public int    getTahunTerbitBuku()   { return tahunTerbitBuku; }

    public void setIdBuku(int idBuku)                       { this.idBuku = idBuku; }
    public void setJudulBuku(String judulBuku)               { this.judulBuku = judulBuku; }
    public void setPenulisBuku(String penulisBuku)           { this.penulisBuku = penulisBuku; }
    public void setPenerbitBuku(String penerbitBuku)         { this.penerbitBuku = penerbitBuku; }
    public void setLokasiCoverBuku(String lokasiCoverBuku)   { this.lokasiCoverBuku = lokasiCoverBuku; }
    public void setTahunTerbitBuku(int tahunTerbitBuku)      { this.tahunTerbitBuku = tahunTerbitBuku; }

    @Override
    public String toString() {
        return "Buku{" +
               "id="       + idBuku          +
               ", judul='" + judulBuku        + '\'' +
               ", penulis='"+ penulisBuku     + '\'' +
               ", penerbit='"+ penerbitBuku   + '\'' +
               ", tahun="  + tahunTerbitBuku  +
               '}';
    }
}