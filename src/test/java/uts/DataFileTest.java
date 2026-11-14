package uts;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import uts.app.ImportResult;
import uts.app.PutusanRepository;

class DataFileTest {
    @Test
    void berkasDatasetValid() throws Exception {
        PutusanRepository repo = new PutusanRepository();
        ImportResult r = repo.importCsv(Path.of("data/putusan-50.csv"));
        assertEquals(0, r.getJumlahError(), "Baris ditolak: " + r.getErrors().getHead());
        assertEquals(r.getBarisDibaca(), r.getValid());
        assertTrue(repo.size() >= 1);
        repo.validateInvariant();
        assertEquals(PutusanRepository.TARGET_RECORD, repo.size());
    }
}
