package uts.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import uts.app.CsvUtil;

public final class PdfExtractor {
    private PdfExtractor() { }

    private static final String WATERMARK = "direktori putusan mahkamah agung republik indonesia putusan.mahkamahagung.go.id";

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Pemakaian: PdfExtractor <folderPDF> <folderKeluaran>");
            return;
        }
        File[] files = new File(args[0]).listFiles((d, n) -> n.toLowerCase().endsWith(".pdf"));
        if (files == null) { System.out.println("Folder tidak ditemukan"); return; }
        java.util.Arrays.sort(files);
        Path out = Path.of(args[1]);
        Files.createDirectories(out);
        try (BufferedWriter w = Files.newBufferedWriter(out.resolve("kandidat.csv"), StandardCharsets.UTF_8)) {
            w.write("berkas,status,nomorPerkara,tanggalPutusan,jenisNarkotika,beratBarangBuktiGram,pasalTerbukti,"
                    + "tuntutanBulan,vonisBulan,dendaRupiah,keadaanMemberatkan,keadaanMeringankan,catatan");
            w.newLine();
            for (File f : files) {
                String[] pages = bacaHalaman(f);
                Path dir = out.resolve("teks").resolve(f.getName().replace(".pdf", ""));
                Files.createDirectories(dir);
                int usable = 0;
                for (int i = 0; i < pages.length; i++) {
                    Files.write(dir.resolve(String.format("halaman-%03d.txt", i + 1)), pages[i].getBytes(StandardCharsets.UTF_8));
                    if (i >= 1 && i <= 3) usable += kataUmum(pages[i]);
                }
                boolean teks = usable >= 15;
                String[] row;
                if (teks) {
                    FieldExtractor.Hasil h = FieldExtractor.extract(pages);
                    row = new String[] {f.getName(), "TEKS", h.nomor, h.tanggal, h.jenis,
                        h.beratAda ? Double.toString(Math.round(h.berat * 10000) / 10000.0) : "0", h.pasalTerbukti,
                        s(h.tuntutanBulan), s(h.vonisBulan), s(h.denda), h.memberatkan, h.meringankan, h.catatan};
                } else {
                    row = new String[] {f.getName(), "SCAN_ISI_MANUAL", "", "", "", "", "", "", "", "", "", "",
                        "tidak ada lapisan teks; PDFBox tidak dapat membaca"};
                }
                w.write(CsvUtil.join(row));
                w.newLine();
                System.out.println((teks ? "TEKS " : "SCAN ") + f.getName());
            }
        }
    }

    static int kataUmum(String t) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\b(yang|dan|bahwa|terdakwa|pidana|dengan|tersebut|untuk|dalam)\\b",
                        java.util.regex.Pattern.CASE_INSENSITIVE).matcher(t);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    private static String s(Object o) { return o == null ? "" : o.toString(); }

    static String[] bacaHalaman(File f) throws IOException {
        PDDocument doc = PDDocument.load(f);
        try {
            PDFTextStripper st = new PDFTextStripper();
            st.setSortByPosition(true);
            int n = doc.getNumberOfPages();
            String[] out = new String[n];
            for (int p = 1; p <= n; p++) {
                st.setStartPage(p);
                st.setEndPage(p);
                StringBuilder sb = new StringBuilder();
                for (String l : st.getText(doc).split("\n")) {
                    String t = l.trim();
                    if (t.length() <= 4 || WATERMARK.contains(t.toLowerCase()) || t.startsWith("Direktori") || t.startsWith("putusan.mahkamah")
                            || t.startsWith("Disclaimer") || t.startsWith("Kepaniteraan") || t.startsWith("pelaksanaan")
                            || t.startsWith("Dalam hal") || t.startsWith("Email") || t.startsWith("Halaman")) continue;
                    sb.append(t).append('\n');
                }
                out[p - 1] = sb.toString();
            }
            return out;
        } finally {
            doc.close();
        }
    }
}
