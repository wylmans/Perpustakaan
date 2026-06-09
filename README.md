# Aplikasi Perpustaakan

---

## 1. Deskripsi

Aplikasi perpustakaan ini dapat memudahkan pecahrian buku yang ada di database dengan sangat cepat dengan memanfaatkan algoritma inverted index yang sering di jumpai pada search engine browser dengan itu maka sebanyak apapun bukunya proses pecahrian akan O(1).

## 2. Dependensi

1. `stopword.txt`
2. `Admin.txt`
3. `Database Driver`
4. `cover-image`
5. `database (tabel_buku, tabel_keyword, tabel_peminjam)`

## 3. Cara Pakai

1. Setup Database
    1. Download database berbasis relasi (Relation Database contoh: MySQL). Lalu ikuti saja proses setupnya, ketika masuk bagian setup username dan password isi dengan yang di inginkan. **`Catatan: Selalu ingat username dan password yang di gunakan.`**
    2. Download driver database, lalu simpan file `.jar` ke dalam `/lib`.
    3. Buat sebuah database bernama `perpustakaan_digital`. 
    **`Catatan : Bisa mengggunakan nama custom, tetapi perlu penyusuaian dalam kode`**
    4. Buat tabel bernama `tabel_buku`, `tabel_keyword`, dam `tabel_peminjam`
        1. tabel_buku
            - id_buku (Primary Key)
            - judul_buku (varchar)
            - penulis_buku (varchar)
            - penerbit_buku (varchar)
            - lokasi_cover_buku (varchar)
            - tahun_terbit_buku (int)
            - sinopsis_buku (text)
            - ketersediaan (ENUM{Tersedia, Dipinjam,Stok Abis})
        2. tabel_keyword
            - id_keyword (Primary Key)
            - keyword (varchar)
            - value (int FK to tabel_buku.id_buku)
        3. tabel_peminjam
            - id_peminjam (Primary Key)
            - nama_peminjam (varchar)
            - id_buku (int FK to tabel_buku.id_buku)
            - tanggal_pinjam (date)
            - tanggal_kembali (date)
    5. Atur di bagian kode `/src/database/DatabaseHelper.java` pada baris 12 hingga 14, dengan berurut URL database, username database, dan password database. `Contoh url "jdbc:mysql://localhost:1111/perpustakaan_digital"`
2. (Optioal) Ubah nama kredesial untuk login di file `/resources/Admin.txt`, baris pertama adalah `username` baris kedua `password`. **`Catatan : Password Menggunakan SHA256, anda bisa gunakan SHA256 generator di online untuk membuat nya `**.

3. Jalankan Kode pada Main.java