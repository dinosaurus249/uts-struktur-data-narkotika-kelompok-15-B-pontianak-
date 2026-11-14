package uts.app;

public class FilterCriteria {
    private String jenisNarkotika;
    private Double beratMin;
    private Double beratMax;
    private String pasalTerbukti;
    private Integer vonisMin;
    private Integer vonisMax;

    public FilterCriteria jenis(String v) { this.jenisNarkotika = v; return this; }
    public FilterCriteria berat(Double min, Double max) { this.beratMin = min; this.beratMax = max; return this; }
    public FilterCriteria pasal(String v) { this.pasalTerbukti = v; return this; }
    public FilterCriteria vonis(Integer min, Integer max) { this.vonisMin = min; this.vonisMax = max; return this; }

    public boolean matches(Putusan p) {
        if (jenisNarkotika != null && !containsIgnoreCase(p.getJenisNarkotika(), jenisNarkotika)) return false;
        if (pasalTerbukti != null && !containsIgnoreCase(p.getPasalTerbukti(), pasalTerbukti)) return false;
        if (beratMin != null && p.getBeratBarangBuktiGram() < beratMin) return false;
        if (beratMax != null && p.getBeratBarangBuktiGram() > beratMax) return false;
        if (vonisMin != null && p.getVonisBulan() < vonisMin) return false;
        if (vonisMax != null && p.getVonisBulan() > vonisMax) return false;
        return true;
    }

    private static boolean containsIgnoreCase(String text, String part) {
        return text != null && text.toLowerCase().contains(part.toLowerCase());
    }
}
