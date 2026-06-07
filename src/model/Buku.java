package model;

public class Buku {

    private int    idBuku;
    private String judulBuku;
    private String penulisBuku;
    private String penerbitBuku;
    private String lokasiCoverBuku;
    private int    tahunTerbitBuku;
    private String sinopsisBuku;
    private String ketersediaan;     // "Tersedia" | "Dipinjam" | "Stok Habis"

    // ── Constructor ───────────────────────────────────────────────────────────────

    public Buku() {}

    public Buku(int idBuku, String judulBuku, String penulisBuku,
                String penerbitBuku, String lokasiCoverBuku,
                int tahunTerbitBuku, String sinopsisBuku) {
        this.idBuku          = idBuku;
        this.judulBuku       = judulBuku;
        this.penulisBuku     = penulisBuku;
        this.penerbitBuku    = penerbitBuku;
        this.lokasiCoverBuku = lokasiCoverBuku;
        this.tahunTerbitBuku = tahunTerbitBuku;
        this.sinopsisBuku    = sinopsisBuku;
    }

    // ── Getter & Setter ───────────────────────────────────────────────────────────

    public int    getIdBuku()            { return idBuku; }
    public String getJudulBuku()         { return judulBuku; }
    public String getPenulisBuku()       { return penulisBuku; }
    public String getPenerbitBuku()      { return penerbitBuku; }
    public String getLokasiCoverBuku()   { return lokasiCoverBuku; }
    public int    getTahunTerbitBuku()   { return tahunTerbitBuku; }
    public String getSinopsisBuku()      { return sinopsisBuku; }
    public String getKetersediaan()      { return ketersediaan; }

    public void setIdBuku(int idBuku)                       { this.idBuku = idBuku; }
    public void setJudulBuku(String judulBuku)               { this.judulBuku = judulBuku; }
    public void setPenulisBuku(String penulisBuku)           { this.penulisBuku = penulisBuku; }
    public void setPenerbitBuku(String penerbitBuku)         { this.penerbitBuku = penerbitBuku; }
    public void setLokasiCoverBuku(String lokasiCoverBuku)   { this.lokasiCoverBuku = lokasiCoverBuku; }
    public void setTahunTerbitBuku(int tahunTerbitBuku)      { this.tahunTerbitBuku = tahunTerbitBuku; }
    public void setSinopsisBuku(String sinopsisBuku)         { this.sinopsisBuku = sinopsisBuku; }
    public void setKetersediaan(String ketersediaan)         { this.ketersediaan = ketersediaan; }

    @Override
    public String toString() {
        return "Buku{" +
               "id="        + idBuku          +
               ", judul='"  + judulBuku        + '\'' +
               ", penulis='"+ penulisBuku      + '\'' +
               ", penerbit='"+ penerbitBuku    + '\'' +
               ", tahun="   + tahunTerbitBuku  +
               ", sinopsis='"+ sinopsisBuku    + '\'' +
               '}';
    }
}