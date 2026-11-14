package uts.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FieldExtractor {
    private FieldExtractor() { }

    public static final class Hasil {
        public String nomor, tanggal, jenis, pasalTerbukti, memberatkan, meringankan;
        public Integer tuntutanBulan, vonisBulan;
        public Long denda;
        public double berat;
        public boolean beratAda;
        public String catatan = "";
    }

    private static final String[] BULAN = {"januari", "februari", "maret", "april", "mei", "juni", "juli",
        "agustus", "september", "oktober", "november", "desember"};

    private static String norm(String s) { return s.replace('_', ' ').replaceAll("\\s+", " ").trim(); }

    private static int bulan(String nama) {
        String n = nama.toLowerCase();
        if (n.equals("nopember")) return 11;
        if (n.equals("pebruari")) return 2;
        for (int i = 0; i < BULAN.length; i++) if (BULAN[i].equals(n)) return i + 1;
        return -1;
    }

    static Integer durasi(String t) {
        Matcher m = Pattern.compile("(\\d+)\\s*\\([^)]*\\)\\s*tahun(?:\\s*(?:dan)?\\s*(\\d+)\\s*\\([^)]*\\)\\s*bulan)?",
                Pattern.CASE_INSENSITIVE).matcher(t);
        if (m.find()) return Integer.parseInt(m.group(1)) * 12 + (m.group(2) != null ? Integer.parseInt(m.group(2)) : 0);
        m = Pattern.compile("(\\d+)\\s*\\([^)]*\\)\\s*bulan", Pattern.CASE_INSENSITIVE).matcher(t);
        if (m.find()) return Integer.parseInt(m.group(1));
        m = Pattern.compile("(\\d+)\\s*tahun(?:\\s*(?:dan)?\\s*(\\d+)\\s*bulan)?", Pattern.CASE_INSENSITIVE).matcher(t);
        if (m.find()) return Integer.parseInt(m.group(1)) * 12 + (m.group(2) != null ? Integer.parseInt(m.group(2)) : 0);
        return null;
    }

    static Long rupiah(String t) {
        Matcher m = Pattern.compile("Rp\\.?\\s*([\\d.,]+)").matcher(t);
        if (!m.find()) return null;
        String s = m.group(1).replaceAll(",\\d{2}$", "").replaceAll("[.,]", "");
        return s.matches("\\d+") ? Long.valueOf(s) : null;
    }

    public static Hasil extract(String[] raw) {
        Hasil h = new Hasil();
        String[] p = new String[raw.length];
        for (int i = 0; i < raw.length; i++) p[i] = norm(raw[i]);

        if (p.length > 0) {
            Matcher m = Pattern.compile("Nomor\\s*(\\d+/Pid\\.Sus/\\d{4}/PN\\s*Ptk)", Pattern.CASE_INSENSITIVE).matcher(p[0]);
            if (m.find()) h.nomor = m.group(1);
        }

        for (int i = 0; i < Math.min(6, p.length) && h.tuntutanBulan == null; i++) {
            int k = p[i].toLowerCase().indexOf("tuntutan");
            if (k < 0) continue;
            Matcher m = Pattern.compile("Menjatuhkan\\s+(?:pidana|hukuman).{0,400}?penjara\\s+selama\\s+(.{0,70})",
                    Pattern.CASE_INSENSITIVE).matcher(p[i].substring(k));
            if (m.find()) h.tuntutanBulan = durasi(m.group(1));
        }
        if (h.tuntutanBulan == null) h.catatan += "tuntutan tidak ditemukan; ";

        int ap = -1;
        String amar = "";
        for (int i = 3; i < p.length && ap < 0; i++) {
            Matcher m = Pattern.compile("M\\s?E\\s?N\\s?G\\s?A\\s?D\\s?I\\s?L\\s?[I1l:]").matcher(p[i]);
            if (m.find()) { ap = i; amar = p[i].substring(m.start()) + " " + at(p, i + 1) + " " + at(p, i + 2); }
        }
        if (ap < 0) {
            for (int i = 3; i < p.length && ap < 0; i++) {
                Matcher m = Pattern.compile("Menyatakan\\s+Terdakwa.{0,250}terbukti\\s+secara\\s+sah").matcher(p[i]);
                if (m.find()) { ap = i; amar = p[i].substring(m.start()) + " " + at(p, i + 1) + " " + at(p, i + 2); }
            }
        }
        if (ap < 0) h.catatan += "amar tidak ditemukan; ";
        else {
            Matcher m = Pattern.compile("Menjatuhkan\\s+pidana.{0,220}", Pattern.CASE_INSENSITIVE).matcher(amar);
            if (m.find()) {
                h.vonisBulan = durasi(m.group(0));
                h.denda = rupiah(m.group(0));
            } else h.catatan += "vonis tidak ditemukan; ";

            int j = indexOfIgnoreCase(amar, "Menetapkan barang bukti");
            String seg = j >= 0 ? amar.substring(j) : amar;
            Matcher cut = Pattern.compile("Membebankan|Demikian").matcher(seg);
            if (cut.find()) seg = seg.substring(0, cut.start());
            if (seg.toLowerCase().contains("netto awal")) {
                Matcher a = Pattern.compile("netto awal\\s*(\\d+(?:[.,]\\d+)?)", Pattern.CASE_INSENSITIVE).matcher(seg);
                while (a.find()) { h.berat += Double.parseDouble(a.group(1).replace(',', '.')); h.beratAda = true; }
                h.catatan += "berat memakai netto awal; ";
            } else {
                Matcher w = Pattern.compile("(?:netto|bersih)\\s*(?:sebesar|seberat)?\\s*[:;]?\\s*\\(?\\s*(\\d+(?:[.,]\\d+)?)\\s*\\)?\\s*(?:\\([^)]{0,90}\\)\\s*)?(?:gram|gr)",
                        Pattern.CASE_INSENSITIVE).matcher(seg);
                while (w.find()) { h.berat += Double.parseDouble(w.group(1).replace(',', '.')); h.beratAda = true; }
            }
            h.jenis = jenis(seg);

            StringBuilder tail = new StringBuilder(amar);
            for (int i = ap; i < p.length; i++) tail.append(' ').append(p[i]);
            Matcher t = Pattern.compile("diputuskan.{0,350}?tanggal\\s+(\\d{1,2})\\s+([A-Za-z]+)\\s+(\\d{4})",
                    Pattern.CASE_INSENSITIVE).matcher(tail);
            if (t.find() && bulan(t.group(2)) > 0) {
                h.tanggal = String.format("%s-%02d-%02d", t.group(3), bulan(t.group(2)), Integer.parseInt(t.group(1)));
            }
        }
        if (h.tanggal == null) h.catatan += "tanggal tidak ditemukan; ";
        if (!h.beratAda) h.catatan += "berat tidak ditemukan; ";
        if (h.jenis == null) h.catatan += "jenis tidak ditemukan; ";

        for (int i = 3; i < p.length; i++) {
            Matcher m = Pattern.compile("Memperhatikan,?\\s*(.{0,300})").matcher(p[i]);
            if (m.find()) {
                String s = m.group(1).split(",?\\s*(?:dan|serta)\\s+Undang|MENGADILI|\\bserta\\b")[0];
                s = s.replaceAll("Undang[- ]*[Uu]ndang(?: Republik Indonesia| RI| R\\.I)?\\s+Nomor\\s+35\\s+Tahun\\s+2009(?:\\s+[Tt]entang\\s+(?:Tindak Pidana\\s+)?[Nn]arkotika)?", "UU 35/2009");
                h.pasalTerbukti = s.trim();
                break;
            }
        }
        if (h.pasalTerbukti == null) h.catatan += "pasal terbukti tidak ditemukan; ";
        String stop = "(?=(?:Keadaan|Hal-hal|Hal)\\s+yang\\s+(?:memberatkan|meringankan)|Menimbang|Memperhatikan|Mengingat|M\\s?E\\s?N\\s?G\\s?A\\s?D\\s?I|$)";
        h.memberatkan = sekitar(p, "memberatkan", stop);
        h.meringankan = sekitar(p, "meringankan", stop);
        if (h.memberatkan == null) h.catatan += "memberatkan tidak ditemukan; ";
        if (h.meringankan == null) h.catatan += "meringankan tidak ditemukan; ";
        return h;
    }

    private static String sekitar(String[] p, String kata, String stop) {
        for (int i = 3; i < p.length; i++) {
            String t = p[i] + " " + (i + 1 < p.length ? p[i + 1].substring(0, Math.min(700, p[i + 1].length())) : "");
            Matcher m = Pattern.compile("(?:Keadaan|Hal-hal|Hal)\\s+yang\\s+" + kata + "\\s*[:;]\\s*(.*?)" + stop,
                    Pattern.CASE_INSENSITIVE).matcher(t);
            if (m.find() && m.group(1).trim().length() > 15) {
                String s = m.group(1).split("Menimbang|Disclaimer|Halaman|Putusan Nomor|Hal\\.")[0];
                s = s.replaceAll("\\s*[—–-]\\s+", "; ").trim().replaceAll("^[;: -]+", "");
                return s.length() > 220 ? s.substring(0, 220) : s;
            }
        }
        return null;
    }

    private static String jenis(String seg) {
        StringBuilder sb = new StringBuilder();
        String[][] k = {{"sabu", "sh?abu|metamfetamin|methamphetamine"}, {"ganja", "ganja|cannabis"},
            {"ekstasi", "ekstasi|ecstasy|MDMA"}, {"tembakau sintetis", "tembakau (?:sintetis|gorila)"},
            {"kokain", "kokain"}, {"heroin", "heroin|putau"}};
        for (String[] e : k) {
            if (Pattern.compile(e[1], Pattern.CASE_INSENSITIVE).matcher(seg).find()) {
                if (sb.length() > 0) sb.append(';');
                sb.append(e[0]);
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private static String at(String[] p, int i) { return i < p.length ? p[i] : ""; }

    private static int indexOfIgnoreCase(String s, String k) { return s.toLowerCase().indexOf(k.toLowerCase()); }
}
