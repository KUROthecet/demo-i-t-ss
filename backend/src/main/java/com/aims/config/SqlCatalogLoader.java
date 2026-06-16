package com.aims.config;

import com.aims.dto.FieldSchema;
import com.aims.repository.MediaRepository;
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
    private final MediaRepository mediaRepository;

    private static final int  BATCH_SIZE = 500;
    private static final long HASH_MASK  = 0x7FFF_FFFFL;

    private record ProductMeta(
        String title, int currentPrice, int originalPrice,
        String description, String imageUrl, int quantity
    ) {}

    public void loadAll() {
        if (mediaRepository.count() > 0) {
            log.info("SqlCatalogLoader: Database already has data, skipping sync.");
            return;
        }
        log.info("SqlCatalogLoader: loading catalog from SQL dump files…");
        Map<String, ProductMeta> productMap = loadProductMetaMap();
        log.info("SqlCatalogLoader: loaded {} product metadata entries", productMap.size());
        loadBooks(productMap);
        loadCDs(productMap);
        loadDVDs(productMap);
        loadNewspapers(productMap);
        log.info("SqlCatalogLoader: catalog load complete.");
    }

    private Map<String, ProductMeta> loadProductMetaMap() {
        ParsedSql p = parseSqlFile("db/Product_rows.sql");
        if (p == null) return Collections.emptyMap();
        Map<String, Integer> idx = buildIndex(p.columns());
        Map<String, ProductMeta> map = new HashMap<>(p.rows().size() * 2);
        for (List<String> row : p.rows()) {
            String id = col(row, idx, "productId");
            if (id == null) continue;
            String imageUrl   = col(row, idx, "imageUrl");
            String title      = col(row, idx, "title");
            String desc       = col(row, idx, "generalDescription");
            int    currPrice  = colIntDefault(row, idx, "currentPrice",  0);
            int    origPrice  = colIntDefault(row, idx, "originalValue", 0);
            int    qty        = colIntDefault(row, idx, "quantity",      10);
            map.put(id, new ProductMeta(title, currPrice, origPrice, desc, imageUrl, qty));
        }
        return map;
    }

    private void loadBooks(Map<String, ProductMeta> productMap) {
        ParsedSql p = parseSqlFile("db/Book_rows.sql");
        if (p == null) return;
        Map<String, Integer> idx = buildIndex(p.columns());
        List<Object[]> media = new ArrayList<>(p.rows().size());
        List<Object[]> sub   = new ArrayList<>(p.rows().size());
        for (List<String> row : p.rows()) {
            String barcode = col(row, idx, "productId");
            if (barcode == null) continue;
            String author  = trunc(col(row, idx, "authors"), 255);
            String cover   = trunc(col(row, idx, "coverType"), 255);
            String pub     = trunc(col(row, idx, "publisher"), 255);
            String pubDate = trunc(col(row, idx, "publicationDate"), 50);
            Integer pages  = colInt(row, idx, "numberOfPages");
            String lang    = trunc(col(row, idx, "language"), 255);
            String genre   = trunc(col(row, idx, "genre"), 255);
            ProductMeta meta = productMap.get(barcode);
            String title    = meta != null && meta.title() != null ? trunc(meta.title(), 255) : bookTitle(author, genre);
            int    orig     = meta != null && meta.originalPrice() > 0 ? meta.originalPrice() : price(barcode, 60_000, 500_000);
            int    curr     = meta != null && meta.currentPrice()  > 0 ? meta.currentPrice()  : discounted(barcode, orig);
            String desc     = meta != null && meta.description() != null ? meta.description() : bookDesc(author, pub, genre, pages, pubDate);
            String imageUrl = meta != null && meta.imageUrl() != null ? meta.imageUrl() : "https://picsum.photos/seed/" + barcode + "/400/600";
            int    qty      = meta != null && meta.quantity() > 0 ? meta.quantity() : stock(barcode);
            media.add(new Object[]{barcode, title, "Book", orig, curr, desc, imageUrl, qty, rushDelivery(barcode)});
            sub.add(new Object[]{author, cover, pubDate, pub, genre, lang, pages, bookDims(pages), bookWeight(pages), barcode});
        }
        batchMedia(media);
        batchExec("INSERT INTO book (id,author,cover_type,publication_date,publisher,genre,language,number_of_pages,dimensions,weight) " +
            "SELECT m.id,?,?,?,?,?,?,?,?,? FROM media m WHERE m.barcode=? ON CONFLICT (id) DO NOTHING", sub);
        log.info("SqlCatalogLoader: {} book rows processed", p.rows().size());
    }

    private void loadCDs(Map<String, ProductMeta> productMap) {
        ParsedSql p = parseSqlFile("db/CD_rows.sql");
        if (p == null) return;
        Map<String, Integer> idx = buildIndex(p.columns());
        List<Object[]> media = new ArrayList<>(p.rows().size());
        List<Object[]> sub   = new ArrayList<>(p.rows().size());
        for (List<String> row : p.rows()) {
            String barcode = col(row, idx, "productId");
            if (barcode == null) continue;
            String artist  = trunc(col(row, idx, "artists"), 255);
            String label   = trunc(col(row, idx, "recordLabel"), 255);
            String tracks  = col(row, idx, "tracksList");
            String genre   = trunc(col(row, idx, "genre"), 255);
            String relDate = normCdDate(col(row, idx, "releaseDate"));
            ProductMeta meta = productMap.get(barcode);
            String title    = meta != null && meta.title() != null ? trunc(meta.title(), 255) : cdTitle(artist, tracks, genre);
            int    orig     = meta != null && meta.originalPrice() > 0 ? meta.originalPrice() : price(barcode, 80_000, 300_000);
            int    curr     = meta != null && meta.currentPrice()  > 0 ? meta.currentPrice() : discounted(barcode, orig);
            String desc     = meta != null && meta.description() != null ? meta.description() : cdDesc(artist, genre, tracks, relDate);
            String imageUrl = meta != null && meta.imageUrl() != null ? meta.imageUrl() : "https://picsum.photos/seed/" + barcode + "/400/400";
            int    qty      = meta != null && meta.quantity() > 0 ? meta.quantity() : stock(barcode);
            media.add(new Object[]{barcode, title, "CD", orig, curr, desc, imageUrl, qty, true});
            sub.add(new Object[]{artist, genre, label, tracks, relDate, "14×12×0.5 cm", 0.1, barcode});
        }
        batchMedia(media);
        batchExec("INSERT INTO cd (id,artist,genre,record_label,track_list,release_date,dimensions,weight) " +
            "SELECT m.id,?,?,?,?,?,?,? FROM media m WHERE m.barcode=? ON CONFLICT (id) DO NOTHING", sub);
        log.info("SqlCatalogLoader: {} CD rows processed", p.rows().size());
    }

    private void loadDVDs(Map<String, ProductMeta> productMap) {
        ParsedSql p = parseSqlFile("db/DVD_rows.sql");
        if (p == null) return;
        Map<String, Integer> idx = buildIndex(p.columns());
        List<Object[]> media = new ArrayList<>(p.rows().size());
        List<Object[]> sub   = new ArrayList<>(p.rows().size());
        for (List<String> row : p.rows()) {
            String barcode  = col(row, idx, "productId");
            if (barcode == null) continue;
            String disc     = trunc(col(row, idx, "discType"), 255);
            String director = trunc(col(row, idx, "director"), 255);
            Integer runtime = colInt(row, idx, "runtime");
            String studio   = trunc(col(row, idx, "studio"), 255);
            String lang     = trunc(col(row, idx, "language"), 255);
            String subs     = trunc(col(row, idx, "subtitles"), 255);
            String relDate  = trunc(col(row, idx, "releaseDate"), 50);
            String genre    = trunc(col(row, idx, "genre"), 255);
            boolean rush = (hash(barcode) % 3) != 0;
            ProductMeta meta = productMap.get(barcode);
            String title    = meta != null && meta.title() != null ? trunc(meta.title(), 255) : dvdTitle(director, genre, relDate);
            int    orig     = meta != null && meta.originalPrice() > 0 ? meta.originalPrice() : price(barcode, 100_000, 450_000);
            int    curr     = meta != null && meta.currentPrice()  > 0 ? meta.currentPrice() : discounted(barcode, orig);
            String desc     = meta != null && meta.description() != null ? meta.description() : dvdDesc(director, genre, runtime, relDate);
            String imageUrl = meta != null && meta.imageUrl() != null ? meta.imageUrl() : "https://picsum.photos/seed/" + barcode + "/400/580";
            int    qty      = meta != null && meta.quantity() > 0 ? meta.quantity() : stock(barcode);
            media.add(new Object[]{barcode, title, "DVD", orig, curr, desc, imageUrl, qty, rush});
            sub.add(new Object[]{director, disc, lang, runtime, studio, subs, genre, relDate, "19×13×1.5 cm", 0.15, barcode});
        }
        batchMedia(media);
        batchExec("INSERT INTO dvd (id,director,disc_type,language,runtime_minutes,studio,subtitles,genre,release_date,dimensions,weight) " +
            "SELECT m.id,?,?,?,?,?,?,?,?,?,? FROM media m WHERE m.barcode=? ON CONFLICT (id) DO NOTHING", sub);
        log.info("SqlCatalogLoader: {} DVD rows processed", p.rows().size());
    }

    private void loadNewspapers(Map<String, ProductMeta> productMap) {
        ParsedSql p = parseSqlFile("db/Newspaper_rows.sql");
        if (p == null) return;
        Map<String, Integer> idx = buildIndex(p.columns());
        List<Object[]> media = new ArrayList<>(p.rows().size());
        List<Object[]> sub   = new ArrayList<>(p.rows().size());
        for (List<String> row : p.rows()) {
            String barcode  = col(row, idx, "productId");
            if (barcode == null) continue;
            String editor   = trunc(col(row, idx, "editorInChief"), 255);
            String pub      = trunc(col(row, idx, "publisher"), 255);
            String pubDate  = trunc(col(row, idx, "publicationDate"), 50);
            String issueNum = trunc(col(row, idx, "issueNumber"), 255);
            String issn     = trunc(col(row, idx, "issn"), 255);
            String lang     = trunc(col(row, idx, "language"), 255);
            String sections = col(row, idx, "section");
            ProductMeta meta = productMap.get(barcode);
            String title    = meta != null && meta.title() != null ? trunc(meta.title(), 255) : newsTitle(pub, sections);
            int    orig     = meta != null && meta.originalPrice() > 0 ? meta.originalPrice() : price(barcode, 8_000, 50_000);
            int    curr     = meta != null && meta.currentPrice()  > 0 ? meta.currentPrice() : discounted(barcode, orig);
            String desc     = meta != null && meta.description() != null ? meta.description() : newsDesc(pub, editor, sections, pubDate);
            String imageUrl = meta != null && meta.imageUrl() != null ? meta.imageUrl() : "https://picsum.photos/seed/" + barcode + "/400/550";
            int    qty      = meta != null && meta.quantity() > 0 ? meta.quantity() : stock(barcode);
            media.add(new Object[]{barcode, title, "Newspaper", orig, curr, desc, imageUrl, qty, false});
            sub.add(new Object[]{editor, pubDate, pub, issn, issueNum, lang, "Daily", sections, "40×30×0.2 cm", 0.3, barcode});
        }
        batchMedia(media);
        batchExec("INSERT INTO newspaper (id,editor_in_chief,publication_date,publisher,issn,issue_number,language,publication_frequency,sections,dimensions,weight) " +
            "SELECT m.id,?,?,?,?,?,?,?,?,?,? FROM media m WHERE m.barcode=? ON CONFLICT (id) DO NOTHING", sub);
        log.info("SqlCatalogLoader: {} Newspaper rows processed", p.rows().size());
    }

    private void batchMedia(List<Object[]> rows) {
        batchExec("INSERT INTO media (barcode,title,category,original_price,current_price,general_description,image_url,quantity_in_stock,status,support_rush_delivery) " +
            "VALUES (?,?,?,?,?,?,?,?,'ACTIVE',?) ON CONFLICT (barcode) DO UPDATE SET title=EXCLUDED.title, general_description=EXCLUDED.general_description, " +
            "image_url=EXCLUDED.image_url, original_price=EXCLUDED.original_price, current_price=EXCLUDED.current_price", rows);
    }

    private void batchExec(String sql, List<Object[]> rows) {
        for (int i = 0; i < rows.size(); i += BATCH_SIZE) {
            jdbcTemplate.batchUpdate(sql, rows.subList(i, Math.min(i + BATCH_SIZE, rows.size())));
        }
    }

    private ParsedSql parseSqlFile(String resourcePath) { return null; }
    private Map<String, Integer> buildIndex(String[] columns) { return null; }
    private String col(List<String> row, Map<String, Integer> idx, String colName) { return null; }
    private Integer colInt(List<String> row, Map<String, Integer> idx, String colName) {  return null; }
    private int colIntDefault(List<String> row, Map<String, Integer> idx, String colName, int defaultValue) {  return 0; }
    private long hash(String barcode) { return Math.abs((long) barcode.hashCode()) & HASH_MASK; }
    private int price(String barcode, int min, int max) { return (int) (min + hash(barcode) % (max - min)); }
    private int discounted(String barcode, int orig) { int raw = (int) (orig * (0.85 + (hash(barcode) % 16) / 100.0)); return (raw / 1_000) * 1_000; }
    private int stock(String barcode) { return (int) (5 + hash(barcode) % 95); }
    private boolean rushDelivery(String barcode) { return hash(barcode) % 2 == 0; }
    private String trunc(String s, int max) { return (s == null || s.length() <= max) ? s : s.substring(0, max); }
    private String orUnknown(String s) { return s != null ? s : "Unknown"; }
    private String firstOf(String csv, String fallback) { if (csv == null || csv.isBlank()) return fallback; String first = csv.split(",")[0].trim(); return first.isEmpty() ? fallback : first; }
    private String normCdDate(String raw) { if (raw == null) return null; String t = raw.trim(); return t.matches("\\d{4}") ? t + "-01-01" : t; }
    private double bookWeight(Integer pages) { return (pages == null || pages <= 0) ? 0.3 : Math.round((0.05 + pages * 0.001) * 100.0) / 100.0; }
    private String bookDims(Integer pages) { return (pages == null || pages <= 0) ? "21×14×2 cm" : "21×14×" + Math.max(1, pages / 150) + " cm"; }
    private String bookTitle(String author, String genre) { return trunc(orUnknown(author) + " — " + firstOf(genre, "General"), 255); }
    private String bookDesc(String author, String pub, String genre, Integer pages, String pubDate) { String year = (pubDate != null && pubDate.length() >= 4) ? pubDate.substring(0, 4) : ""; return String.format("%s book by %s, published by %s%s. %s", firstOf(genre, "General"), orUnknown(author), orUnknown(pub), year.isEmpty() ? "" : " (" + year + ")", (pages != null && pages > 0) ? pages + " pages." : ""); }
    private String cdTitle(String artist, String tracks, String genre) { String a = orUnknown(artist); if (tracks != null && !tracks.isBlank()) return trunc(a + " — " + firstOf(tracks, ""), 255); return trunc(a + " — " + firstOf(genre, "Music"), 255); }
    private String cdDesc(String artist, String genre, String tracks, String relDate) { String year = (relDate != null && relDate.length() >= 4) ? relDate.substring(0, 4) : ""; return String.format("%s music by %s%s. %s", firstOf(genre, "Music"), orUnknown(artist), year.isEmpty() ? "" : " (" + year + ")", (tracks != null && !tracks.isBlank()) ? "Track: " + trunc(tracks, 200) + "." : ""); }
    private String dvdTitle(String director, String genre, String releaseDate) { String year = (releaseDate != null && releaseDate.length() >= 4) ? releaseDate.substring(0, 4) : ""; String g = firstOf(genre, "Film"); boolean unknownDir = (director == null || director.equalsIgnoreCase("Unknown")); if (!unknownDir) return trunc(director + " — " + g + (year.isEmpty() ? "" : " (" + year + ")"), 255); return trunc(g + (year.isEmpty() ? "" : " (" + year + ")"), 255); }
    private String dvdDesc(String director, String genre, Integer runtime, String releaseDate) { String year = (releaseDate != null && releaseDate.length() >= 4) ? releaseDate.substring(0, 4) : ""; boolean unknownDir = (director == null || director.equalsIgnoreCase("Unknown")); String dirPart = unknownDir ? "" : "Directed by " + director + ". "; return String.format("%s%s film%s.%s", dirPart, firstOf(genre, ""), year.isEmpty() ? "" : " (" + year + ")", (runtime != null && runtime > 0) ? " Runtime: " + runtime + " min." : ""); }
    private String newsTitle(String publisher, String sections) { String p = orUnknown(publisher); String s = firstOf(sections, ""); return trunc(p + (s.isEmpty() ? "" : " — " + s), 255); }
    private String newsDesc(String publisher, String editor, String sections, String pubDate) { return String.format("Published by %s. Chief Editor: %s. Sections: %s. Date: %s.", orUnknown(publisher), orUnknown(editor), sections != null ? trunc(sections, 200) : "General", pubDate != null ? pubDate : ""); }
    private record ParsedSql(String[] columns, List<List<String>> rows) {}
}
