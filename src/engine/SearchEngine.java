package engine;

import database.DatabaseHelper;
import util.StringTokenizer;

import java.sql.Connection;
import java.util.*;

public class SearchEngine {

    /**
     * Mencari dokumen yang mengandung semua kata dalam query (AND search).
     * Menggunakan inverted index yang di-fetch dari database.
     *
     * @param kataPencarian   array kata / frase yang ingin dicari
     * @param stopWord        array kata-kata yang diabaikan saat pencarian
     * @param koneksi         koneksi JDBC yang sudah terbuka
     * @return set berisi ID dokumen yang relevan, atau set kosong jika tidak ditemukan
     */
    public static Set<Integer> cariMultiKata(
            String[] kataPencarian,
            String[] stopWord,
            Connection koneksi) {

        // Set kosong sebagai nilai kembalian saat pencarian tidak menemukan hasil
        Set<Integer> setKosong = new HashSet<>();

        // ── TAHAP 0: Fetch inverted index dari database ───────────────────────────
        // Delegasi ke DatabaseHelper — tidak ada duplikasi logika koneksi
        Map<String, Set<Integer>> indexMap = DatabaseHelper.mengambilDataDariDatabase(koneksi);

        // ── TAHAP 1: Tokenisasi berdasarkan spasi ─────────────────────────────────
        // Gabungkan array input menjadi satu string lalu pecah per kata
        String queryGabung = String.join(" ", kataPencarian).trim();
        String[] sementara = StringTokenizer.pecahStringBerdasarkanSpasi(queryGabung);

        // ── TAHAP 2: Filtering Stop-Word ──────────────────────────────────────────
        List<String> kataPencarianBersih = new ArrayList<>();

        for (int i = 0; i < sementara.length; i++) {
            boolean adalahStopWord = false;

            for (int j = 0; j < stopWord.length; j++) {
                if (sementara[i].equalsIgnoreCase(stopWord[j])) {
                    adalahStopWord = true;
                    break; // hentikan loop j
                }
            }

            if (!adalahStopWord) {
                // Simpan dalam huruf kecil agar cocok dengan keyword di DB
                kataPencarianBersih.add(sementara[i].toLowerCase());
            }
        }

        // Jika tidak ada kata tersisa setelah disaring, kembalikan set kosong
        if (kataPencarianBersih.isEmpty()) {
            return setKosong;
        }

        // ── TAHAP 3: Kasus khusus — hanya 1 kata pencarian bersih ────────────────
        Set<Integer> sekarang = indexMap.getOrDefault(
                kataPencarianBersih.get(0), new HashSet<>());

        if (kataPencarianBersih.size() == 1) {
            return sekarang;
        }

        // ── TAHAP 4: Proses Irisan Multi-Kata (Intersection) ─────────────────────
        Set<Integer> banding;
        Set<Integer> wadahSementara;

        for (int i = 1; i < kataPencarianBersih.size(); i++) {
            banding = indexMap.getOrDefault(
                    kataPencarianBersih.get(i), new HashSet<>());

            Set<Integer> hasilIrisan = new HashSet<>();

            // Optimasi: pastikan 'sekarang' selalu set yang lebih besar
            // agar loop luar lebih panjang dan loop dalam lebih pendek
            if (sekarang.size() < banding.size()) {
                wadahSementara = sekarang;
                sekarang       = banding;
                banding        = wadahSementara;
            }

            // Bandingkan dan simpan hasil irisan
            List<Integer> sekarangList = new ArrayList<>(sekarang);
            List<Integer> bandingList  = new ArrayList<>(banding);

            for (int j = 0; j < sekarangList.size(); j++) {
                for (int k = 0; k < bandingList.size(); k++) {
                    if (sekarangList.get(j).equals(bandingList.get(k))) {
                        hasilIrisan.add(sekarangList.get(j));
                        break; // hentikan loop k
                    }
                }
            }

            sekarang = hasilIrisan;

            // Jika irisan sudah kosong di tengah jalan, tidak perlu lanjut
            if (sekarang.isEmpty()) {
                return setKosong;
            }
        }

        // Kembalikan hasil akhir setelah semua kata berhasil diiriskan
        return sekarang;
    }
}