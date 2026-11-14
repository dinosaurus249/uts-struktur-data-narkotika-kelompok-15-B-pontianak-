package uts;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uts.app.CsvUtil;
import uts.app.FilterCriteria;
import uts.app.Putusan;
import uts.app.PutusanRepository;
import uts.ds.SinglyLinkedList;

class RealDataScenarioTest {

    PutusanRepository repo;
    Map<String, String[]> provenance;

    @BeforeEach
    void muat() throws IOException {
        repo = new PutusanRepository();
        repo.importCsv(Path.of("data/putusan-50.csv"));
        provenance = bacaProvenance(Path.of("docs/data-provenance.csv"));
    }

    @Test
    void filterGabunganCocokDenganProvenance() {
        FilterCriteria f = new FilterCriteria().jenis("sabu").berat(1.0, 10.0).pasal("114").vonis(60, 96);
        SinglyLinkedList<Putusan> hasil = repo.filter(f);

        assertEquals(3, hasil.size());
        assertEquals("P015", hasil.get(0).getCaseId());
        assertEquals("P022", hasil.get(1).getCaseId());
        assertEquals("P045", hasil.get(2).getCaseId());

        for (int i = 0; i < hasil.size(); i++) {
            Putusan p = hasil.get(i);
            assertEquals(p.getJenisNarkotika(), nilaiProvenance(p, "jenisNarkotika"));
            assertEquals(p.getBeratBarangBuktiGram(), Double.parseDouble(nilaiProvenance(p, "beratBarangBuktiGram")), 1e-9);
            assertEquals(p.getPasalTerbukti(), nilaiProvenance(p, "pasalTerbukti"));
            assertEquals(p.getVonisBulan(), Integer.parseInt(nilaiProvenance(p, "vonisBulan")));
            assertFalse(provenance.get(p.getCaseId() + "|vonisBulan")[2].isBlank());
        }
        assertEquals(50, repo.size());
        repo.validateInvariant();
    }

    @Test
    void setiapRecordPunyaProvenanceYangKonsisten() {
        for (int i = 0; i < repo.size(); i++) {
            Putusan p = repo.arsip().get(i);
            assertEquals(p.getNomorPerkara(), nilaiProvenance(p, "nomorPerkara"));
            assertEquals(p.getVonisBulan(), Integer.parseInt(nilaiProvenance(p, "vonisBulan")));
            assertEquals(p.getDendaRupiah(), Long.parseLong(nilaiProvenance(p, "dendaRupiah")));
        }
    }

    private String nilaiProvenance(Putusan p, String field) {
        String[] row = provenance.get(p.getCaseId() + "|" + field);
        assertNotNull(row, "Provenance tidak ada untuk " + p.getCaseId() + "." + field);
        return row[0].isEmpty() ? null : row[0];
    }

    private static Map<String, String[]> bacaProvenance(Path file) throws IOException {
        Map<String, String[]> m = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = CsvUtil.parseLine(line);
                m.put(f[0] + "|" + f[1], new String[] {f[2], f[3], f[4], f[5], f[6], f[7]});
            }
        }
        return m;
    }
}
