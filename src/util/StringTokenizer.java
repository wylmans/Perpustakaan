package util;

public class StringTokenizer {

    /**
     * Memecah string judul buku menjadi array kata berdasarkan spasi.
     * Mengabaikan spasi ganda dan spasi di awal/akhir string.
     *
     * @param judulBuku string yang ingin dipecah
     * @return array kata hasil tokenisasi
     */
    public static String[] pecahStringBerdasarkanSpasi(String judulBuku) {
        if (judulBuku == null || judulBuku.isEmpty()) {
            return new String[0];
        }

        String[] sementara = new String[judulBuku.length()];
        int jumlahKata = 0;
        StringBuilder kataSaatIni = new StringBuilder();

        for (int i = 0; i < judulBuku.length(); i++) {
            char karakter = judulBuku.charAt(i);

            // Jika karakter adalah spasi/whitespace, simpan kata yang terkumpul
            if (Character.isWhitespace(karakter)) {
                if (kataSaatIni.length() > 0) {
                    sementara[jumlahKata] = kataSaatIni.toString();
                    jumlahKata++;
                    kataSaatIni.setLength(0); // reset buffer
                }
            } else {
                // Kumpulkan karakter ke buffer kata
                kataSaatIni.append(karakter);
            }
        }

        // Jangan lupa kata terakhir jika tidak diakhiri spasi
        if (kataSaatIni.length() > 0) {
            sementara[jumlahKata] = kataSaatIni.toString();
            jumlahKata++;
        }

        // Salin ke array dengan ukuran tepat
        String[] hasil = new String[jumlahKata];
        for (int i = 0; i < jumlahKata; i++) {
            hasil[i] = sementara[i];
        }

        return hasil;
    }
}