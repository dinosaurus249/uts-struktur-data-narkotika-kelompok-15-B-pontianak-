package uts.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import uts.ds.ArrayStack;
import uts.ds.SearchResult;
import uts.ds.SinglyLinkedList;
import uts.ds.SinglyLinkedList.Node;

public class PutusanRepository {
    public static final int TARGET_RECORD = 50;

    private final SinglyLinkedList<Putusan> arsip = new SinglyLinkedList<>();
    private final ArrayStack<String> audit = new ArrayStack<>();

    public SinglyLinkedList<Putusan> arsip() { return arsip; }
    public int size() { return arsip.size(); }

    public ImportResult importCsv(Path file) throws IOException {
        ImportResult r = new ImportResult();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                lineNo++;
                if (header) { header = false; continue; }
                if (line.trim().isEmpty()) continue;
                r.barisDibaca++;
                try {
                    Putusan p = PutusanParser.parse(line);
                    cekDuplikat(p);
                    arsip.addLast(p);
                    r.valid++;
                } catch (IllegalArgumentException e) {
                    r.errors.addLast("Baris " + lineNo + ": " + e.getMessage());
                }
            }
        }
        catat("IMPOR " + file + " valid=" + r.valid + " error=" + r.getJumlahError());
        return r;
    }

    public void tambah(Putusan p) {
        try {
            cekDuplikat(p);
        } catch (IllegalArgumentException e) {
            catat("GAGAL TAMBAH " + p.getCaseId() + ": " + e.getMessage());
            throw e;
        }
        arsip.addLast(p);
        catat("TAMBAH " + p.getCaseId() + " di posisi " + (arsip.size() - 1));
    }

    public void tambahDariCsv(String line) {
        Putusan p;
        try {
            p = PutusanParser.parse(line);
        } catch (IllegalArgumentException e) {
            catat("GAGAL TAMBAH (baris tidak valid): " + e.getMessage());
            throw e;
        }
        tambah(p);
    }

    public void sisipkan(int position, Putusan p) {
        cekDuplikat(p);
        arsip.insert(position, p);
        catat("SISIP " + p.getCaseId() + " di posisi " + position);
    }

    public Putusan hapus(int position) {
        Putusan p = arsip.remove(position);
        catat("HAPUS " + p.getCaseId() + " dari posisi " + position);
        return p;
    }

    public Putusan hapusCaseId(String caseId) {
        SearchResult<Putusan> r = cariCaseId(caseId);
        if (!r.isFound()) throw new IllegalArgumentException("caseId tidak ditemukan: " + caseId);
        return hapus(r.getPosition());
    }

    public SearchResult<Putusan> cariCaseId(String caseId) {
        return arsip.search(p -> p.getCaseId().equalsIgnoreCase(caseId.trim()));
    }

    public SearchResult<Putusan> cariNomorPerkara(String nomor) {
        SearchResult<Putusan> r = arsip.search(p -> p.getNomorPerkara().equalsIgnoreCase(nomor.trim()));
        catat("CARI nomor=" + nomor + " langkah=" + r.getSteps() + " ditemukan=" + r.isFound());
        return r;
    }

    public SinglyLinkedList<Putusan> filter(FilterCriteria kriteria) {
        SinglyLinkedList<Putusan> hasil = new SinglyLinkedList<>();
        for (Node<Putusan> c = arsip.getHead(); c != null; c = c.getNext()) {
            if (kriteria.matches(c.getValue())) hasil.addLast(c.getValue());
        }
        catat("FILTER hasil=" + hasil.size());
        return hasil;
    }

    public Ringkasan agregasi(SinglyLinkedList<Putusan> sumber) {
        int n = 0;
        long totalVonis = 0;
        long denda = 0;
        SinglyLinkedList<Ringkasan.JenisCount> dist = new SinglyLinkedList<>();
        for (Node<Putusan> c = sumber.getHead(); c != null; c = c.getNext()) {
            Putusan p = c.getValue();
            n++;
            totalVonis += p.getVonisBulan();
            denda += p.getDendaRupiah();
            if (p.getJenisNarkotika() == null) {
                hitung(dist, "(kosong)");
            } else {
                String[] parts = p.getJenisNarkotika().split("[;|]");
                for (String part : parts) {
                    String k = part.trim().toLowerCase();
                    if (!k.isEmpty()) hitung(dist, k);
                }
            }
        }
        return new Ringkasan(n, n == 0 ? 0 : (double) totalVonis / n, denda, dist);
    }

    public Ringkasan agregasi() { return agregasi(arsip); }

    private void hitung(SinglyLinkedList<Ringkasan.JenisCount> dist, String jenis) {
        for (Node<Ringkasan.JenisCount> c = dist.getHead(); c != null; c = c.getNext()) {
            if (c.getValue().getJenis().equals(jenis)) { c.getValue().tambah(); return; }
        }
        Ringkasan.JenisCount jc = new Ringkasan.JenisCount(jenis);
        jc.tambah();
        dist.addLast(jc);
    }

    public SinglyLinkedList<Putusan> urutkanBerdasarkanVonis(boolean menaik) {
        SinglyLinkedList<Putusan> hasil = new SinglyLinkedList<>();
        for (Node<Putusan> c = arsip.getHead(); c != null; c = c.getNext()) {
            Putusan p = c.getValue();
            int pos = 0;
            for (Node<Putusan> h = hasil.getHead(); h != null; h = h.getNext()) {
                int cmp = Integer.compare(h.getValue().getVonisBulan(), p.getVonisBulan());
                boolean taruhSebelum = menaik ? cmp > 0 : cmp < 0;
                if (taruhSebelum) break;
                pos++;
            }
            hasil.insert(pos, p);
        }
        catat("URUT vonis " + (menaik ? "menaik" : "menurun"));
        return hasil;
    }

    private void catat(String aksi) {
        audit.push(LocalDateTime.now().withNano(0) + " | " + aksi);
    }

    public String riwayat() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < audit.size(); i++) {
            sb.append(i + 1).append(". ").append(audit.get(i)).append('\n');
        }
        return sb.toString();
    }

    public int jumlahAudit() { return audit.size(); }

    public SearchResult<String> cariRiwayat(String kataKunci) {
        return audit.searchPreserve(s -> s.toLowerCase().contains(kataKunci.toLowerCase()));
    }

    public void eksporCsv(Path file, SinglyLinkedList<Putusan> data) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write(Putusan.HEADER);
            w.newLine();
            for (Node<Putusan> c = data.getHead(); c != null; c = c.getNext()) {
                w.write(c.getValue().toCsvLine());
                w.newLine();
            }
        }
        catat("EKSPOR " + data.size() + " record ke " + file);
    }

    public void eksporTeks(Path file, String isi) throws IOException {
        Files.write(file, isi.getBytes(StandardCharsets.UTF_8));
        catat("EKSPOR laporan ke " + file);
    }

    public void validateInvariant() {
        arsip.validateInvariant();
        audit.validateInvariant();
    }

    private void cekDuplikat(Putusan p) {
        if (cariCaseId(p.getCaseId()).isFound()) {
            throw new IllegalArgumentException("caseId duplikat: " + p.getCaseId());
        }
    }
}
