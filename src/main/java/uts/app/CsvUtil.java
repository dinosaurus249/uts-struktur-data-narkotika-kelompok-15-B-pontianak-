package uts.app;

import java.util.Arrays;

public final class CsvUtil {
    private CsvUtil() { }

    public static String[] parseLine(String line) {
        String[] out = new String[16];
        int count = 0;
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                    else inQuotes = false;
                } else cur.append(c);
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                if (count == out.length) out = Arrays.copyOf(out, count * 2);
                out[count++] = cur.toString();
                cur.setLength(0);
            } else cur.append(c);
        }
        if (inQuotes) throw new IllegalArgumentException("Tanda kutip tidak ditutup");
        if (count == out.length) out = Arrays.copyOf(out, count + 1);
        out[count++] = cur.toString();
        return Arrays.copyOf(out, count);
    }

    public static String escape(String s) {
        if (s == null) return "";
        if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    public static String join(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }
}
