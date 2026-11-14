package uts.app;

import java.time.LocalDate;
import java.util.Locale;

public final class Putusan {
    public static final String HEADER =
            "caseId,nomorPerkara,pengadilan,tanggalPutusan,terdakwaKode,jenisNarkotika,"
          + "beratBarangBuktiGram,pasalDakwaan,pasalTerbukti,tuntutanBulan,vonisBulan,"
          + "dendaRupiah,barangBukti,keadaanMemberatkan,keadaanMeringankan,sourcePages";
    public static final int COLUMNS = 16;

    private final String caseId;
    private final String nomorPerkara;
    private final String pengadilan;
    private final LocalDate tanggalPutusan;
    private final String terdakwaKode;
    private final String jenisNarkotika;
    private final double beratBarangBuktiGram;
    private final String pasalDakwaan;
    private final String pasalTerbukti;
    private final int tuntutanBulan;
    private final int vonisBulan;
    private final long dendaRupiah;
    private final String barangBukti;
    private final String keadaanMemberatkan;
    private final String keadaanMeringankan;
    private final String sourcePages;

    public Putusan(String caseId, String nomorPerkara, String pengadilan, LocalDate tanggalPutusan,
                   String terdakwaKode, String jenisNarkotika, double beratBarangBuktiGram,
                   String pasalDakwaan, String pasalTerbukti, int tuntutanBulan, int vonisBulan,
                   long dendaRupiah, String barangBukti, String keadaanMemberatkan,
                   String keadaanMeringankan, String sourcePages) {
        this.caseId = caseId;
        this.nomorPerkara = nomorPerkara;
        this.pengadilan = pengadilan;
        this.tanggalPutusan = tanggalPutusan;
        this.terdakwaKode = terdakwaKode;
        this.jenisNarkotika = jenisNarkotika;
        this.beratBarangBuktiGram = beratBarangBuktiGram;
        this.pasalDakwaan = pasalDakwaan;
        this.pasalTerbukti = pasalTerbukti;
        this.tuntutanBulan = tuntutanBulan;
        this.vonisBulan = vonisBulan;
        this.dendaRupiah = dendaRupiah;
        this.barangBukti = barangBukti;
        this.keadaanMemberatkan = keadaanMemberatkan;
        this.keadaanMeringankan = keadaanMeringankan;
        this.sourcePages = sourcePages;
    }

    public String getCaseId() { return caseId; }
    public String getNomorPerkara() { return nomorPerkara; }
    public String getPengadilan() { return pengadilan; }
    public LocalDate getTanggalPutusan() { return tanggalPutusan; }
    public String getTerdakwaKode() { return terdakwaKode; }
    public String getJenisNarkotika() { return jenisNarkotika; }
    public double getBeratBarangBuktiGram() { return beratBarangBuktiGram; }
    public String getPasalDakwaan() { return pasalDakwaan; }
    public String getPasalTerbukti() { return pasalTerbukti; }
    public int getTuntutanBulan() { return tuntutanBulan; }
    public int getVonisBulan() { return vonisBulan; }
    public long getDendaRupiah() { return dendaRupiah; }
    public String getBarangBukti() { return barangBukti; }
    public String getKeadaanMemberatkan() { return keadaanMemberatkan; }
    public String getKeadaanMeringankan() { return keadaanMeringankan; }
    public String getSourcePages() { return sourcePages; }

    public String toCsvLine() {
        return CsvUtil.join(new String[] {
                caseId, nomorPerkara, pengadilan,
                tanggalPutusan == null ? null : tanggalPutusan.toString(),
                terdakwaKode, jenisNarkotika, Double.toString(beratBarangBuktiGram),
                pasalDakwaan, pasalTerbukti, Integer.toString(tuntutanBulan),
                Integer.toString(vonisBulan), Long.toString(dendaRupiah),
                barangBukti, keadaanMemberatkan, keadaanMeringankan, sourcePages });
    }

    public String ringkas() {
        return String.format(Locale.ROOT, "%s | %s | %s | %s | %s | %.2f g | %s | vonis %d bln | denda Rp%d",
                caseId, nomorPerkara, pengadilan, tanggalPutusan, terdakwaKode,
                beratBarangBuktiGram, jenisNarkotika, vonisBulan, dendaRupiah);
    }

    @Override
    public String toString() { return ringkas(); }
}
