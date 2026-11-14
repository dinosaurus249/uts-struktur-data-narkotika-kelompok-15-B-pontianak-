package uts.app;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;
import uts.ds.SearchResult;
import uts.ds.SinglyLinkedList;
import uts.ds.SinglyLinkedList.Node;

public class Main {
    private static SinglyLinkedList<Putusan> hasilTerakhir;

    public static void main(String[] args) {
        PutusanRepository repo = new PutusanRepository();
        Scanner in = new Scanner(System.in);
        String csv = args.length > 0 ? args[0] : "data/putusan-50.csv";

        while (true) {
            System.out.println("\n=== ARSIP PUTUSAN NARKOTIKA (Singly Linked List + Stack Audit) ===");
            System.out.println("Size arsip: " + repo.size());
            System.out.println("1 Impor CSV      2 Tampil        3 Tambah       4 Hapus (posisi/caseId)");
            System.out.println("5 Cari           6 Filter        7 Agregasi     8 Urutkan vonis");
            System.out.println("9 Audit          10 Ekspor CSV   11 Cari riwayat (Stack)   12 Cek invariant");
            System.out.println("13 Ekspor laporan teks (agregasi + riwayat audit)              0 Keluar");
            System.out.print("Pilih: ");
            if (!in.hasNextLine()) return;
            String c = in.nextLine().trim();
            try {
                switch (c) {
                    case "1" -> {
                        String p = ask(in, "Path CSV [" + csv + "]: ", csv);
                        ImportResult r = repo.importCsv(Path.of(p));
                        System.out.println("Dibaca " + r.getBarisDibaca() + ", valid " + r.getValid()
                                + ", ditolak " + r.getJumlahError());
                        for (SinglyLinkedList.Node<String> n = r.getErrors().getHead(); n != null; n = n.getNext()) {
                            System.out.println("  ! " + n.getValue());
                        }
                        if (r.getValid() != PutusanRepository.TARGET_RECORD) {
                            System.out.println("PERINGATAN: record valid " + r.getValid()
                                    + " (target " + PutusanRepository.TARGET_RECORD + ")");
                        }
                    }
                    case "2" -> tampil(repo.arsip());
                    case "3" -> {
                        System.out.println("Format: " + Putusan.HEADER);
                        System.out.print("Baris CSV: ");
                        repo.tambahDariCsv(in.nextLine());
                        System.out.println("Berhasil ditambahkan. Size = " + repo.size());
                    }
                    case "4" -> {
                        String s = ask(in, "Posisi (0-based) atau caseId: ", "");
                        Putusan p = s.matches("\\d+") ? repo.hapus(Integer.parseInt(s)) : repo.hapusCaseId(s);
                        System.out.println("Dihapus: " + p.ringkas() + " | size = " + repo.size());
                    }
                    case "5" -> {
                        String mode = ask(in, "Cari berdasarkan (1=caseId, 2=nomorPerkara): ", "1");
                        String key = ask(in, "Nilai: ", "");
                        SearchResult<Putusan> r = mode.equals("2") ? repo.cariNomorPerkara(key) : repo.cariCaseId(key);
                        System.out.println(r.isFound() ? "Ditemukan di posisi " + r.getPosition() + ": " + r.getValue().ringkas()
                                : "Tidak ditemukan");
                        System.out.println("Jumlah langkah: " + r.getSteps());
                    }
                    case "6" -> {
                        FilterCriteria f = new FilterCriteria();
                        String jenis = ask(in, "Jenis narkotika (kosong=lewati): ", "");
                        if (!jenis.isEmpty()) f.jenis(jenis);
                        String bMin = ask(in, "Berat min (gram): ", "");
                        String bMax = ask(in, "Berat max (gram): ", "");
                        f.berat(bMin.isEmpty() ? null : Double.parseDouble(bMin), bMax.isEmpty() ? null : Double.parseDouble(bMax));
                        String pasal = ask(in, "Pasal terbukti mengandung: ", "");
                        if (!pasal.isEmpty()) f.pasal(pasal);
                        String vMin = ask(in, "Vonis min (bulan): ", "");
                        String vMax = ask(in, "Vonis max (bulan): ", "");
                        f.vonis(vMin.isEmpty() ? null : Integer.parseInt(vMin), vMax.isEmpty() ? null : Integer.parseInt(vMax));
                        hasilTerakhir = repo.filter(f);
                        tampil(hasilTerakhir);
                    }
                    case "7" -> System.out.print(repo.agregasi(hasilTerakhir != null ? hasilTerakhir : repo.arsip()));
                    case "8" -> {
                        boolean asc = !ask(in, "Urutan (1=menaik, 2=menurun): ", "1").equals("2");
                        hasilTerakhir = repo.urutkanBerdasarkanVonis(asc);
                        tampil(hasilTerakhir);
                    }
                    case "9" -> System.out.print(repo.riwayat());
                    case "10" -> {
                        String p = ask(in, "File tujuan [hasil.csv]: ", "hasil.csv");
                        repo.eksporCsv(Path.of(p), hasilTerakhir != null ? hasilTerakhir : repo.arsip());
                        System.out.println("Tersimpan di " + p);
                    }
                    case "11" -> {
                        System.out.println("Sebelum:\n" + repo.riwayat());
                        SearchResult<String> r = repo.cariRiwayat(ask(in, "Kata kunci: ", ""));
                        System.out.println(r.isFound() ? "Ditemukan (kedalaman " + r.getPosition() + "): " + r.getValue()
                                : "Tidak ditemukan");
                        System.out.println("Langkah: " + r.getSteps());
                        System.out.println("Sesudah:\n" + repo.riwayat());
                    }
                    case "12" -> { repo.validateInvariant(); System.out.println("Invariant OK"); }
                    case "13" -> {
                        String p = ask(in, "File tujuan [laporan.txt]: ", "laporan.txt");
                        String isi = "LAPORAN ARSIP PUTUSAN NARKOTIKA\n\n"
                                + repo.agregasi(hasilTerakhir != null ? hasilTerakhir : repo.arsip())
                                + "\nRIWAYAT AUDIT (terbaru di atas)\n" + repo.riwayat();
                        repo.eksporTeks(Path.of(p), isi);
                        System.out.println("Tersimpan di " + p);
                    }
                    case "0" -> { return; }
                    default -> System.out.println("Pilihan tidak dikenal");
                }
            } catch (IllegalArgumentException | IndexOutOfBoundsException | IOException e) {
                System.out.println("Kesalahan: " + e.getMessage());
            }
        }
    }

    private static void tampil(SinglyLinkedList<Putusan> list) {
        int i = 0;
        for (Node<Putusan> c = list.getHead(); c != null; c = c.getNext()) {
            System.out.println("[" + i++ + "] " + c.getValue().ringkas());
        }
        System.out.println("Total: " + list.size());
    }

    private static String ask(Scanner in, String prompt, String def) {
        System.out.print(prompt);
        String s = in.nextLine().trim();
        return s.isEmpty() ? def : s;
    }
}
