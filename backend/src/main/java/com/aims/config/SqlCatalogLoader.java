package com.aims.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlCatalogLoader {

    private final JdbcTemplate jdbcTemplate;

    private static final int  BATCH_SIZE = 500;
    private static final long HASH_MASK  = 0x7FFF_FFFFL;

    public void loadAll() {
        log.info("SqlCatalogLoader: loading catalog from SQL dump files…");
        loadBooks();
        loadCDs();
        loadDVDs();
        loadNewspapers();
        log.info("SqlCatalogLoader: catalog load complete.");
    }

    private void loadBooks() {
        ParsedSql p = parseSqlFile("db/Book_rows.sql");
        if (p == null) return;
        Map<String, Integer> idx = buildIndex(p.columns());

        List<Object[]> media = new ArrayList<>(p.rows().size());
        List<Object[]> sub   = new ArrayList<>(p.rows().size());

        for (List<String> row : p.rows()) {
            String barcode = col(row, idx, "productId");
            if (barcode == null) continue;

            String author  = trunc(col(row, idx, "authors"),        255);
            String cover   = trunc(col(row, idx, "coverType"),       255);
            String pub     = trunc(col(row, idx, "publisher"),       255);
            String pubDate = trunc(col(row, idx, "publicationDate"),  50);
            Integer pages  = colInt(row, idx, "numberOfPages");
            String lang    = trunc(col(row, idx, "language"),        255);
            String genre   = trunc(col(row, idx, "genre"),           255);

            int    orig  = price(barcode, 60_000,  500_000);
            int    curr  = discounted(barcode, orig);

            media.add(new Object[]{
                barcode, bookTitle(author, genre), "Book", orig, curr,
                bookDesc(author, pub, genre, pages, pubDate),
                bookDims(pages), bookWeight(pages),
                "https://picsum.photos/seed/" + barcode + "/400/600",
                stock(barcode), rushDelivery(barcode)
            });
            sub.add(new Object[]{author, cover, pubDate, pub, genre, lang, pages, barcode});
        }

        batchMedia(media);
        batchExec(
            "INSERT INTO book (id,author,cover_type,publication_date,publisher,genre,language,number_of_pages) " +
            "SELECT m.id,?,?,?,?,?,?,? FROM media m WHERE m.barcode=? ON CONFLICT (id) DO NOTHING",
            sub
        );
        log.info("SqlCatalogLoader: {} book rows processed", p.rows().size());
    }

    private void loadCDs() {
        ParsedSql p = parseSqlFile("db/CD_rows.sql");
        if (p == null) return;
        Map<String, Integer> idx = buildIndex(p.columns());

        List<Object[]> media = new ArrayList<>(p.rows().size());
        List<Object[]> sub   = new ArrayList<>(p.rows().size());

        for (List<String> row : p.rows()) {
            String barcode = col(row, idx, "productId");
            if (barcode == null) continue;

            String artist  = trunc(col(row, idx, "artists"),    255);
            String label   = trunc(col(row, idx, "recordLabel"), 255);
            String tracks  = col(row, idx, "tracksList");
            String genre   = trunc(col(row, idx, "genre"),       255);
            String relDate = normCdDate(col(row, idx, "releaseDate"));

            int orig = price(barcode, 80_000, 300_000);
            int curr = discounted(barcode, orig);

            media.add(new Object[]{
                barcode, cdTitle(artist, tracks, genre), "CD", orig, curr,
                cdDesc(artist, genre, tracks, relDate),
                "14×12×0.5 cm", 0.1,
                "https://picsum.photos/seed/" + barcode + "/400/400",
                stock(barcode), true
            });
            sub.add(new Object[]{artist, genre, label, tracks, relDate, barcode});
        }

        batchMedia(media);
        batchExec(
            "INSERT INTO cd (id,artist,genre,record_label,track_list,release_date) " +
            "SELECT m.id,?,?,?,?,? FROM media m WHERE m.barcode=? ON CONFLICT (id) DO NOTHING",
            sub
        );
        log.info("SqlCatalogLoader: {} CD rows processed", p.rows().size());
    }

    private void loadDVDs() {
        ParsedSql p = parseSqlFile("db/DVD_rows.sql");
        if (p == null) return;
        Map<String, Integer> idx = buildIndex(p.columns());

        List<Object[]> media = new ArrayList<>(p.rows().size());
        List<Object[]> sub   = new ArrayList<>(p.rows().size());

        for (List<String> row : p.rows()) {
            String barcode  = col(row, idx, "productId");
            if (barcode == null) continue;

            String disc     = trunc(col(row, idx, "discType"),   255);
            String director = trunc(col(row, idx, "director"),   255);
            Integer runtime = colInt(row, idx, "runtime");
            String studio   = trunc(col(row, idx, "studio"),     255);
            String lang     = trunc(col(row, idx, "language"),   255);
            String subs     = trunc(col(row, idx, "subtitles"),  255);
            String relDate  = trunc(col(row, idx, "releaseDate"), 50);
            String genre    = trunc(col(row, idx, "genre"),      255);

            int    orig = price(barcode, 100_000, 450_000);
            int    curr = discounted(barcode, orig);
            // ~67 % of DVDs support rush delivery
            boolean rush = (hash(barcode) % 3) != 0;

            media.add(new Object[]{
                barcode, dvdTitle(director, genre, relDate), "DVD", orig, curr,
                dvdDesc(director, genre, runtime, relDate),
                "19×13×1.5 cm", 0.15,
                "https://picsum.photos/seed/" + barcode + "/400/580",
                stock(barcode), rush
            });
            sub.add(new Object[]{director, disc, lang, runtime, studio, subs, genre, relDate, barcode});
        }

        batchMedia(media);
        batchExec(
            "INSERT INTO dvd (id,director,disc_type,language,runtime_minutes,studio,subtitles,genre,release_date) " +
            "SELECT m.id,?,?,?,?,?,?,?,? FROM media m WHERE m.barcode=? ON CONFLICT (id) DO NOTHING",
            sub
        );
        log.info("SqlCatalogLoader: {} DVD rows processed", p.rows().size());
    }

    private void loadNewspapers() {
        ParsedSql p = parseSqlFile("db/Newspaper_rows.sql");
        if (p == null) return;
        Map<String, Integer> idx = buildIndex(p.columns());

        List<Object[]> media = new ArrayList<>(p.rows().size());
        List<Object[]> sub   = new ArrayList<>(p.rows().size());

        for (List<String> row : p.rows()) {
            String barcode  = col(row, idx, "productId");
            if (barcode == null) continue;

            String editor   = trunc(col(row, idx, "editorInChief"),  255);
            String pub      = trunc(col(row, idx, "publisher"),       255);
            String pubDate  = trunc(col(row, idx, "publicationDate"),  50);
            String issueNum = trunc(col(row, idx, "issueNumber"),     255);
            String issn     = trunc(col(row, idx, "issn"),            255);
            String lang     = trunc(col(row, idx, "language"),        255);
            String sections = col(row, idx, "section");

            int orig = price(barcode, 8_000, 50_000);
            int curr = discounted(barcode, orig);

            // Newspapers are fragile — no rush delivery
            media.add(new Object[]{
                barcode, newsTitle(pub, sections), "Newspaper", orig, curr,
                newsDesc(pub, editor, sections, pubDate),
                "40×30×0.2 cm", 0.3,
                "https://picsum.photos/seed/" + barcode + "/400/550",
                stock(barcode), false
            });
            sub.add(new Object[]{editor, pubDate, pub, issn, issueNum, lang, "Daily", sections, barcode});
        }

        batchMedia(media);
        batchExec(
            "INSERT INTO newspaper " +
            "(id,editor_in_chief,publication_date,publisher,issn,issue_number,language,publication_frequency,sections) " +
            "SELECT m.id,?,?,?,?,?,?,?,? FROM media m WHERE m.barcode=? ON CONFLICT (id) DO NOTHING",
            sub
        );
        log.info("SqlCatalogLoader: {} Newspaper rows processed", p.rows().size());
    }

    private void batchMedia(List<Object[]> rows) {
        batchExec(
            "INSERT INTO media " +
            "(barcode,title,category,original_price,current_price," +
            "general_description,dimensions,weight,image_url,quantity_in_stock,status,support_rush_delivery) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,'ACTIVE',?) ON CONFLICT (barcode) DO NOTHING",
            rows
        );
    }

    private void batchExec(String sql, List<Object[]> rows) {
        for (int i = 0; i < rows.size(); i += BATCH_SIZE) {
            jdbcTemplate.batchUpdate(sql, rows.subList(i, Math.min(i + BATCH_SIZE, rows.size())));
        }
    }

    private record ParsedSql(String[] columns, List<List<String>> rows) {}

    private ParsedSql parseSqlFile(String resourcePath) {
        String content;
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("SqlCatalogLoader: cannot read '{}': {}", resourcePath, e.getMessage());
            return null;
        }

        content = content.replace("\r\n", " ").replace('\n', ' ').replace('\r', ' ').trim();
        if (content.endsWith(";")) content = content.substring(0, content.length() - 1);

        int valuesIdx = content.toUpperCase().indexOf(" VALUES");
        if (valuesIdx < 0) {
            log.warn("SqlCatalogLoader: no VALUES clause found in '{}'", resourcePath);
            return null;
        }

        String[]           columns = parseColumns(content, valuesIdx);
        List<List<String>> rows    = parseRows(content, valuesIdx + " VALUES".length());

        log.debug("SqlCatalogLoader: '{}' → {} columns, {} rows", resourcePath, columns.length, rows.size());
        return new ParsedSql(columns, rows);
    }

    private String[] parseColumns(String sql, int valuesIdx) {
        String header     = sql.substring(0, valuesIdx);
        int    openParen  = header.lastIndexOf('(');
        int    closeParen = header.lastIndexOf(')');
        if (openParen < 0 || closeParen <= openParen) return new String[0];

        return Arrays.stream(header.substring(openParen + 1, closeParen).split(","))
                     .map(s -> s.trim().replace("\"", "").replace("`", ""))
                     .toArray(String[]::new);
    }

    private List<List<String>> parseRows(String sql, int start) {
        List<List<String>> rows = new ArrayList<>();
        StringBuilder      cur  = new StringBuilder();
        boolean inQuote = false;
        int     depth   = 0;

        char[] chars = sql.toCharArray();
        for (int i = start; i < chars.length; i++) {
            char c = chars[i];

            if (c == '\'' && !inQuote) {
                inQuote = true;
                if (depth >= 1) cur.append(c);

            } else if (c == '\'' /* && inQuote */) {
                if (i + 1 < chars.length && chars[i + 1] == '\'') {
                    // Escaped single-quote: preserve '' so parseValues can unescape it
                    if (depth >= 1) { cur.append('\''); cur.append('\''); }
                    i++;
                } else {
                    inQuote = false;
                    if (depth >= 1) cur.append(c);
                }

            } else if (c == '(' && !inQuote) {
                depth++;
                if (depth == 1) cur.setLength(0);   // begin new row
                else            cur.append(c);

            } else if (c == ')' && !inQuote) {
                if (depth == 1) rows.add(parseValues(cur.toString()));
                else if (depth > 1) cur.append(c);
                if (depth > 0) depth--;

            } else if (depth >= 1) {
                cur.append(c);
            }
        }
        return rows;
    }

    private List<String> parseValues(String rawRow) {
        List<String>  values = new ArrayList<>();
        StringBuilder cur    = new StringBuilder();
        boolean       inQ    = false;

        char[] chars = rawRow.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '\'' && !inQ) {
                inQ = true;
            } else if (c == '\'' /* && inQ */) {
                if (i + 1 < chars.length && chars[i + 1] == '\'') {
                    cur.append('\'');
                    i++;
                } else {
                    inQ = false;
                }
            } else if (c == ',' && !inQ) {
                values.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        values.add(cur.toString());
        return values;
    }

    private Map<String, Integer> buildIndex(String[] columns) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < columns.length; i++) map.put(columns[i], i);
        return map;
    }

    private String col(List<String> row, Map<String, Integer> idx, String colName) {
        Integer i = idx.get(colName);
        if (i == null || i >= row.size()) return null;
        String v = row.get(i);
        if (v == null) return null;
        String t = v.trim();
        return (t.isEmpty() || t.equalsIgnoreCase("NULL")) ? null : t;
    }

    private Integer colInt(List<String> row, Map<String, Integer> idx, String colName) {
        String v = col(row, idx, colName);
        if (v == null) return null;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return null; }
    }

    private long hash(String barcode) {
        return Math.abs((long) barcode.hashCode()) & HASH_MASK;
    }

    private int price(String barcode, int min, int max) {
        return (int) (min + hash(barcode) % (max - min));
    }

    private int discounted(String barcode, int orig) {
        int raw = (int) (orig * (0.85 + (hash(barcode) % 16) / 100.0));
        return (raw / 1_000) * 1_000;
    }

    private int stock(String barcode) {
        return (int) (5 + hash(barcode) % 95);
    }

    private boolean rushDelivery(String barcode) {
        return hash(barcode) % 2 == 0;
    }

    private String trunc(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max);
    }

    private String orUnknown(String s) {
        return s != null ? s : "Unknown";
    }

    private String firstOf(String csv, String fallback) {
        if (csv == null || csv.isBlank()) return fallback;
        String first = csv.split(",")[0].trim();
        return first.isEmpty() ? fallback : first;
    }

    private String normCdDate(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        return t.matches("\\d{4}") ? t + "-01-01" : t;
    }

    private double bookWeight(Integer pages) {
        if (pages == null || pages <= 0) return 0.3;
        return Math.round((0.05 + pages * 0.001) * 100.0) / 100.0;
    }

    private String bookDims(Integer pages) {
        if (pages == null || pages <= 0) return "21×14×2 cm";
        return "21×14×" + Math.max(1, pages / 150) + " cm";
    }

    private String bookTitle(String author, String genre) {
        return trunc(orUnknown(author) + " — " + firstOf(genre, "General"), 255);
    }

    private String bookDesc(String author, String pub, String genre, Integer pages, String pubDate) {
        String year = (pubDate != null && pubDate.length() >= 4) ? pubDate.substring(0, 4) : "";
        return String.format("%s book by %s, published by %s%s. %s",
            firstOf(genre, "General"), orUnknown(author), orUnknown(pub),
            year.isEmpty() ? "" : " (" + year + ")",
            (pages != null && pages > 0) ? pages + " pages." : "");
    }

    private String cdTitle(String artist, String tracks, String genre) {
        String a = orUnknown(artist);
        if (tracks != null && !tracks.isBlank()) return trunc(a + " — " + firstOf(tracks, ""), 255);
        return trunc(a + " — " + firstOf(genre, "Music"), 255);
    }

    private String cdDesc(String artist, String genre, String tracks, String relDate) {
        String year = (relDate != null && relDate.length() >= 4) ? relDate.substring(0, 4) : "";
        return String.format("%s music by %s%s. %s",
            firstOf(genre, "Music"), orUnknown(artist),
            year.isEmpty() ? "" : " (" + year + ")",
            (tracks != null && !tracks.isBlank()) ? "Track: " + trunc(tracks, 200) + "." : "");
    }

    private String dvdTitle(String director, String genre, String releaseDate) {
        String year = (releaseDate != null && releaseDate.length() >= 4) ? releaseDate.substring(0, 4) : "";
        String g    = firstOf(genre, "Film");
        boolean unknownDir = (director == null || director.equalsIgnoreCase("Unknown"));
        if (!unknownDir) return trunc(director + " — " + g + (year.isEmpty() ? "" : " (" + year + ")"), 255);
        return trunc(g + (year.isEmpty() ? "" : " (" + year + ")"), 255);
    }

    private String dvdDesc(String director, String genre, Integer runtime, String releaseDate) {
        String year   = (releaseDate != null && releaseDate.length() >= 4) ? releaseDate.substring(0, 4) : "";
        boolean unknownDir = (director == null || director.equalsIgnoreCase("Unknown"));
        String  dirPart    = unknownDir ? "" : "Directed by " + director + ". ";
        return String.format("%s%s film%s.%s",
            dirPart, firstOf(genre, ""),
            year.isEmpty() ? "" : " (" + year + ")",
            (runtime != null && runtime > 0) ? " Runtime: " + runtime + " min." : "");
    }

    private String newsTitle(String publisher, String sections) {
        String p = orUnknown(publisher);
        String s = firstOf(sections, "");
        return trunc(p + (s.isEmpty() ? "" : " — " + s), 255);
    }

    private String newsDesc(String publisher, String editor, String sections, String pubDate) {
        return String.format("Published by %s. Chief Editor: %s. Sections: %s. Date: %s.",
            orUnknown(publisher), orUnknown(editor),
            sections != null ? trunc(sections, 200) : "General",
            pubDate != null ? pubDate : "");
    }
}
