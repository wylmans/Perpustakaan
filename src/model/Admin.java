package model;

/**
 * Kelas Admin — murni menyimpan data petugas.
 * Password disimpan dalam bentuk hash SHA-256, bukan plain text.
 */
public class Admin {

    private String username;
    private String passwordHash; // Bukan plain text — sudah di-hash SHA-256

    // ── Constructor ───────────────────────────────────────────────────────────────
    public Admin() {}

    public Admin(String username, String passwordHash) {
        this.username     = username;
        this.passwordHash = passwordHash;
    }

    // ── Getter & Setter ───────────────────────────────────────────────────────────
    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }

    public void setUsername(String username)         { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}