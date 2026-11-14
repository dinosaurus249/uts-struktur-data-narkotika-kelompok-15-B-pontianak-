package uts.app;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class PutusanParser {
    private PutusanParser() { }

    public static Putusan parse(String line) {
        return fromFields(CsvUtil.parseLine(line));
    }

    public static Putusan fromFields(String[] f) {
        if (f.length != Putusan.COLUMNS) {
            throw new IllegalArgumentException("Jumlah kolom harus " + Putusan.COLUMNS + ", ditemukan " + f.length);
        }
        String caseId = required(f[0], "caseId");
        String nomor = required(f[1], "nomorPerkara");
        String pengadilan = required(f[2], "pengadilan");

        LocalDate tanggal = null;
        if (!isBlank(f[3])) {
            try {
                tanggal = LocalDate.parse(f[3].trim());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("tanggalPutusan tidak valid (format yyyy-MM-dd): " + f[3]);
            }
        }

        String kode = required(f[4], "terdakwaKode");
        if (!kode.matches("[A-Za-z]{1,4}-?\\d{1,4}")) {
            throw new IllegalArgumentException("terdakwaKode harus kode anonim (mis. T001), bukan nama: " + kode);
        }

        return new Putusan(caseId, nomor, pengadilan, tanggal, kode,
                blankToNull(f[5]),
                parseDouble(f[6], "beratBarangBuktiGram"),
                blankToNull(f[7]), blankToNull(f[8]),
                parseInt(f[9], "tuntutanBulan"),
                parseInt(f[10], "vonisBulan"),
                parseLong(f[11], "dendaRupiah"),
                blankToNull(f[12]), blankToNull(f[13]), blankToNull(f[14]), blankToNull(f[15]));
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static String blankToNull(String s) { return isBlank(s) ? null : s.trim(); }

    private static String required(String s, String name) {
        if (isBlank(s)) throw new IllegalArgumentException(name + " wajib diisi");
        return s.trim();
    }

    private static double parseDouble(String s, String name) {
        if (isBlank(s)) return 0;
        try {
            double d = Double.parseDouble(s.trim());
            if (Double.isNaN(d) || Double.isInfinite(d) || d < 0) {
                throw new IllegalArgumentException(name + " harus angka >= 0: " + s);
            }
            return d;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " bukan angka: " + s);
        }
    }

    private static int parseInt(String s, String name) {
        if (isBlank(s)) return 0;
        try {
            int v = Integer.parseInt(s.trim());
            if (v < 0) throw new IllegalArgumentException(name + " tidak boleh negatif: " + s);
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " bukan bilangan bulat: " + s);
        }
    }

    private static long parseLong(String s, String name) {
        if (isBlank(s)) return 0;
        try {
            long v = Long.parseLong(s.trim());
            if (v < 0) throw new IllegalArgumentException(name + " tidak boleh negatif: " + s);
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " bukan bilangan bulat: " + s);
        }
    }
}
