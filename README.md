# Aplikasi Perpustakaan Digital

Aplikasi perpustakaan digital yang memanfaatkan algoritma **Inverted Index** untuk mempercepat proses pencarian buku. Teknologi ini umum digunakan pada mesin pencari (*search engine*) sehingga pencarian dapat dilakukan secara sangat efisien meskipun jumlah data buku terus bertambah.

---

## Deskripsi

Aplikasi ini dirancang untuk membantu pengguna mencari buku dengan cepat melalui kata kunci tertentu. Sistem pencarian menggunakan struktur data **Inverted Index**, yang memungkinkan pencarian dilakukan tanpa harus memeriksa seluruh data buku satu per satu.

### ✨ Fitur

* 🔍 Pencarian buku menggunakan algoritma Inverted Index
* 📖 Manajemen data buku
* 👤 Manajemen data peminjam
* 🖼️ Dukungan cover buku
* 🔐 Login administrator
* 💾 Penyimpanan data menggunakan database relasional
* ⚡ Pencarian cepat berbasis keyword

---

## Dependensi

Sebelum menjalankan aplikasi, pastikan beberapa komponen berikut tersedia:

| Komponen                 | Keterangan                                      |
| ------------------------ | ----------------------------------------------- |
| `stopword.txt`           | Daftar kata yang diabaikan saat proses indexing |
| `Admin.txt`              | File konfigurasi akun administrator             |
| Database Driver (`.jar`) | Driver JDBC untuk database yang digunakan       |
| `cover/`           | Folder penyimpanan gambar sampul buku           |
| Database                 | Berisi tabel buku, keyword, dan peminjam        |
| OpenJDK                  | Runtime Java untuk menjalankan aplikasi         |

---

## ⚙️ Instalasi dan Konfigurasi

### 1. Setup Database

#### 1.1 Instal Database

Gunakan database relasional seperti:

* MySQL
* MariaDB
* PostgreSQL (dengan penyesuaian driver)

Saat proses instalasi, tentukan:

* Username
* Password

> **Penting:** Simpan username dan password yang digunakan karena akan diperlukan saat konfigurasi aplikasi.

---

#### 1.2 Tambahkan Driver Database

Unduh driver JDBC sesuai database yang digunakan, kemudian simpan file `.jar` ke dalam folder:

```text
/lib
```

---

#### 1.3 Buat Database

Buat database baru dengan nama:

```sql
perpustakaan_digital
```

> Nama database dapat diubah, namun Anda harus menyesuaikan konfigurasi koneksi pada kode program.

---

#### 1.4 Buat Struktur Tabel

##### Tabel `tabel_buku`

| Kolom             | Tipe                                    |
| ----------------- | --------------------------------------- |
| id_buku           | Primary Key                             |
| judul_buku        | VARCHAR                                 |
| penulis_buku      | VARCHAR                                 |
| penerbit_buku     | VARCHAR                                 |
| lokasi_cover_buku | VARCHAR                                 |
| tahun_terbit_buku | INT                                     |
| sinopsis_buku     | TEXT                                    |
| ketersediaan      | ENUM('Tersedia','Dipinjam','Stok Abis') |

---

##### Tabel `tabel_keyword`

| Kolom      | Tipe                          |
| ---------- | ----------------------------- |
| id_keyword | Primary Key                   |
| keyword    | VARCHAR                       |
| value      | INT (FK → tabel_buku.id_buku) |

---

##### Tabel `tabel_peminjam`

| Kolom           | Tipe                          |
| --------------- | ----------------------------- |
| id_peminjam     | Primary Key                   |
| nama_peminjam   | VARCHAR                       |
| id_buku         | INT (FK → tabel_buku.id_buku) |
| tanggal_pinjam  | DATE                          |
| tanggal_kembali | DATE                          |

---

#### 1.5 Konfigurasi Koneksi Database

Buka file:

```text
/src/database/DatabaseHelper.java
```

Kemudian sesuaikan konfigurasi pada baris koneksi database:

```java
private static final String DB_URL      = "jdbc:mysql://localhost:111/perpustakaan_digital";
private static final String DB_USER     = "root";
private static final String DB_PASSWORD = "123456";
```

Contoh URL:

```text
jdbc:mysql://localhost:3306/perpustakaan_digital
```

---

#### 1.6 (Opsional) Tambahkan Data Awal

Anda dapat langsung mengisi tabel database dengan data buku agar aplikasi dapat segera digunakan dan fitur pencarian dapat diuji.

---

### 2. Konfigurasi Akun Administrator (Opsional)

Buka file:

```text
/resources/Admin.txt
```

Format file:

```text
username
password_hash
```

Contoh:

```text
admin
240be518fabd2724ddb6f04eebf2e2f...
```

> Password disimpan menggunakan algoritma **SHA-256**. Anda dapat menggunakan generator SHA-256 online untuk membuat hash password baru.

---

## ▶️ Menjalankan Aplikasi

Setelah seluruh konfigurasi selesai:

1. Pastikan database aktif.
2. Pastikan driver JDBC telah ditambahkan ke folder `/lib`.
3. Jalankan file:

```text
Main.java
```

Atau melalui terminal:

```bash
javac Main.java
java Main
```

---

## 🏗️ Arsitektur Proyek

```text
PERPUSTAKAAN/
│
├── lib/
│   └── Driver JDBC dan library eksternal
│
├── resources/
│   ├── Admin.txt
│   └── stopword.txt
│
├── src/
│   │
│   ├── controller/
│   │   └── Logika aplikasi
│   │
│   ├── database/
│   │   └── Koneksi dan helper database
│   │
│   ├── engine/
│   │   └── Implementasi Inverted Index
│   │
│   ├── model/
│   │   └── Representasi data
│   │
│   ├── ui/
│   │   └── Tampilan Java Swing
│   │
│   ├── util/
│   │   └── Utility dan helper
│   │
│   └── Main.java
│
├── LICENSE
└── README.md
```

---

## 🔍 Cara Kerja Pencarian

Sistem menggunakan pendekatan **Inverted Index**:

1. Judul buku dipecah menjadi kata-kata (*tokenization*).
2. Kata yang termasuk stopword akan diabaikan.
3. Setiap kata disimpan ke tabel `tabel_keyword`.
4. Saat pengguna melakukan pencarian, sistem langsung mengambil daftar buku berdasarkan keyword yang dicari.

Pendekatan ini memungkinkan proses pencarian menjadi jauh lebih cepat dibandingkan pencarian linear pada seluruh data buku.

---

## 🛠️ Teknologi yang Digunakan

* Java
* Java Swing
* JDBC
* MySQL / Database Relasional
* SHA-256
* Inverted Index

---

## 👨‍💻 Pengembang

Proyek ini dibuat sebagai aplikasi perpustakaan digital untuk mendemonstrasikan penggunaan algoritma **Inverted Index** dalam sistem pencarian buku.
