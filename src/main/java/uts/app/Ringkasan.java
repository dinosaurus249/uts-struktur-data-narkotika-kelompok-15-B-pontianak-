package uts.app;

import java.util.Locale;
import uts.ds.SinglyLinkedList;

public final class Ringkasan {
    public static final class JenisCount {
        private final String jenis;
        private int jumlah;
        JenisCount(String jenis) { this.jenis = jenis; }
        void tambah() { jumlah++; }
        public String getJenis() { return jenis; }
        public int getJumlah() { return jumlah; }
    }

    private final int jumlahPerkara;
    private final double rataRataVonis;
    private final long totalDenda;
    private final SinglyLinkedList<JenisCount> distribusi;

    Ringkasan(int jumlahPerkara, double rataRataVonis, long totalDenda, SinglyLinkedList<JenisCount> distribusi) {
        this.jumlahPerkara = jumlahPerkara;
        this.rataRataVonis = rataRataVonis;
        this.totalDenda = totalDenda;
        this.distribusi = distribusi;
    }

    public int getJumlahPerkara() { return jumlahPerkara; }
    public double getRataRataVonis() { return rataRataVonis; }
    public long getTotalDenda() { return totalDenda; }
    public SinglyLinkedList<JenisCount> getDistribusi() { return distribusi; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Jumlah perkara   : ").append(jumlahPerkara).append('\n');
        sb.append(String.format(Locale.ROOT, "Rata-rata vonis  : %.2f bulan%n", rataRataVonis));
        sb.append("Total denda      : Rp").append(totalDenda).append('\n');
        sb.append("Distribusi jenis narkotika:\n");
        for (SinglyLinkedList.Node<JenisCount> c = distribusi.getHead(); c != null; c = c.getNext()) {
            sb.append("  - ").append(c.getValue().getJenis()).append(": ").append(c.getValue().getJumlah()).append('\n');
        }
        return sb.toString();
    }
}
