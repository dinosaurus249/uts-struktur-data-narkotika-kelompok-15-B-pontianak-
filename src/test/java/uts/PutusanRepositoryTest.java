package uts;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uts.app.FilterCriteria;
import uts.app.ImportResult;
import uts.app.Putusan;
import uts.app.PutusanRepository;
import uts.app.Ringkasan;
import uts.ds.SearchResult;
import uts.ds.SinglyLinkedList;

class PutusanRepositoryTest {

    @TempDir Path tmp;
    PutusanRepository repo;

    static String baris(int i) {
        return String.join(",",
                String.format("P%03d", i), "NOMOR-" + i, "PN Dummy " + i, "2025-01-10",
                String.format("T%03d", i), (i % 2 == 0 ? "Sabu" : "Ganja"), String.valueOf(i + 0.5),
                "Pasal 114", (i % 3 == 0 ? "Pasal 112" : "Pasal 114"),
                String.valueOf(60 + i), String.valueOf(48 + i), "800000000",
                "barang", "memberatkan", "meringankan", "identitas:1;amar:20");
    }

    @BeforeEach
    void muat() throws IOException {
        StringBuilder sb = new StringBuilder(Putusan.HEADER).append('\n');
        for (int i = 1; i <= 50; i++) sb.append(baris(i)).append('\n');
        sb.append(baris(60).replace("2025-01-10", "10-01-2025")).append('\n');
        sb.append(baris(1)).append('\n');
        Path f = tmp.resolve("data.csv");
        Files.write(f, sb.toString().getBytes(StandardCharsets.UTF_8));
        repo = new PutusanRepository();
        ImportResult r = repo.importCsv(f);
        assertEquals(50, r.getValid());
        assertEquals(2, r.getJumlahError());
    }

    @Test
    void skenario1_size50DanInvariant() {
        assertEquals(50, repo.size());
        repo.validateInvariant();
    }

    @Test
    void skenario2_cariAdaTidakAdaDanBatas() {
        SearchResult<Putusan> awal = repo.cariCaseId("P001");
        assertEquals(1, awal.getSteps());
        SearchResult<Putusan> akhir = repo.cariCaseId("P050");
        assertEquals(49, akhir.getPosition());
        assertEquals(50, akhir.getSteps());
        SearchResult<Putusan> tidak = repo.cariNomorPerkara("TIDAK-ADA");
        assertFalse(tidak.isFound());
        assertEquals(50, tidak.getSteps());
    }

    @Test
    void skenario3dan4_hapusLaluTambahKembali() {
        assertEquals("P001", repo.hapus(0).getCaseId());
        repo.hapus(24);
        repo.hapus(repo.size() - 1);
        assertEquals(47, repo.size());
        assertEquals("P002", repo.arsip().get(0).getCaseId());
        repo.validateInvariant();

        repo.tambahDariCsv(baris(51));
        repo.tambahDariCsv(baris(52));
        repo.tambahDariCsv(baris(53));
        assertEquals(50, repo.size());
        assertEquals("P053", repo.arsip().get(49).getCaseId());
        repo.validateInvariant();
    }

    @Test
    void skenario5_filterGabungan() {
        SinglyLinkedList<Putusan> h = repo.filter(new FilterCriteria().jenis("ganja").berat(10.0, 12.0));
        assertEquals(1, h.size());
        assertEquals("P011", h.get(0).getCaseId());

        SinglyLinkedList<Putusan> h2 = repo.filter(new FilterCriteria().jenis("sabu").vonis(50, 60));
        assertEquals(6, h2.size());
        assertEquals(50, repo.size());
    }

    @Test
    void skenario6_pencarianStackTidakMengubahUrutan() {
        String sebelum = repo.riwayat();
        int n = repo.jumlahAudit();
        SearchResult<String> r = repo.cariRiwayat("IMPOR");
        assertTrue(r.isFound());
        assertEquals(sebelum, repo.riwayat());
        assertEquals(n, repo.jumlahAudit());
        repo.validateInvariant();
    }

    @Test
    void skenario7_recordTidakValidDitolakStrukturTetapKonsisten() {
        assertThrows(IllegalArgumentException.class, () -> repo.tambahDariCsv("rusak,hanya,tiga"));
        assertThrows(IllegalArgumentException.class,
                () -> repo.tambahDariCsv(baris(70).replace("T070", "Budi Santoso")));
        assertThrows(IllegalArgumentException.class, () -> repo.tambahDariCsv(baris(1)));
        assertEquals(50, repo.size());
        repo.validateInvariant();
        assertTrue(repo.riwayat().contains("GAGAL TAMBAH"));
    }

    @Test
    void urutanVonisTidakMerusakArsip() {
        SinglyLinkedList<Putusan> desc = repo.urutkanBerdasarkanVonis(false);
        assertEquals(50, desc.size());
        assertEquals("P050", desc.get(0).getCaseId());
        for (int i = 1; i < desc.size(); i++) {
            assertTrue(desc.get(i - 1).getVonisBulan() >= desc.get(i).getVonisBulan());
        }
        assertEquals("P001", repo.arsip().get(0).getCaseId());
        desc.validateInvariant();
        repo.validateInvariant();
    }

    @Test
    void agregasi() {
        Ringkasan r = repo.agregasi();
        assertEquals(50, r.getJumlahPerkara());
        assertEquals(73.5, r.getRataRataVonis(), 1e-9);
        assertEquals(50L * 800000000L, r.getTotalDenda());
        assertEquals(2, r.getDistribusi().size());
    }

    @Test
    void eksporCsvDapatDibacaUlang() throws IOException {
        Path out = tmp.resolve("hasil.csv");
        repo.eksporCsv(out, repo.filter(new FilterCriteria().jenis("sabu")));
        PutusanRepository lain = new PutusanRepository();
        ImportResult r = lain.importCsv(out);
        assertEquals(25, r.getValid());
        assertEquals(0, r.getJumlahError());
    }
}
