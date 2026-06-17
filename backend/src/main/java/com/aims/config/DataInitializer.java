package com.aims.config;

import com.aims.entity.User;
import com.aims.repository.MediaRepository;
import com.aims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final MediaRepository  mediaRepository;
    private final UserRepository   userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final SqlCatalogLoader sqlCatalogLoader;
    private final JdbcTemplate     jdbcTemplate;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.manager-account.password:manager123}")
    private String managerPassword;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        migrateLegacySingleRoleColumn();
        migratePhysicalMediaDimensions();
        migrateCdTrackList();
        seedUsers();
        log.info("DataInitializer: syncing catalog from SQL files…");
        sqlCatalogLoader.loadAll();
        log.info("DataInitializer: catalog sync complete ({} items).", mediaRepository.count());
    }

    private void migrateLegacySingleRoleColumn() {
        Integer legacyColumnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_name = 'users' AND column_name = 'role'",
                Integer.class);
        if (legacyColumnCount == null || legacyColumnCount == 0) return;

        List<Map<String, Object>> legacyRows = jdbcTemplate.queryForList(
                "SELECT id, role FROM users WHERE role IS NOT NULL");

        int migrated = 0;
        for (Map<String, Object> row : legacyRows) {
            Long   userId = ((Number) row.get("id")).longValue();
            String role   = (String) row.get("role");

            Integer existingCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_roles WHERE user_id = ? AND role = ?",
                    Integer.class, userId, role);
            if (existingCount != null && existingCount == 0) {
                jdbcTemplate.update("INSERT INTO user_roles (user_id, role) VALUES (?, ?)", userId, role);
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("DataInitializer: migrated {} legacy single-role assignment(s) into user_roles.", migrated);
        }
    }

    private void migratePhysicalMediaDimensions() {
        for (String table : List.of("book", "cd", "dvd", "newspaper")) {
            Integer dimColExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name=? AND column_name='dimensions'",
                Integer.class, table);
            if (dimColExists == null || dimColExists == 0) continue;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT t.id, t.dimensions FROM " + table + " t " +
                "WHERE t.dimensions IS NOT NULL AND t.height_cm = 0 AND t.width_cm = 0 AND t.length_cm = 0");

            for (Map<String, Object> row : rows) {
                Long   id   = ((Number) row.get("id")).longValue();
                double[] d  = parseDimString((String) row.get("dimensions"));
                jdbcTemplate.update(
                    "UPDATE " + table + " SET height_cm=?,width_cm=?,length_cm=? WHERE id=?",
                    d[0], d[1], d[2], id);
            }
            if (!rows.isEmpty()) {
                log.info("DataInitializer: migrated {} {} dimension record(s).", rows.size(), table);
            }
        }
    }

    private void migrateCdTrackList() {
        Integer colExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_name='cd' AND column_name='track_list'",
            Integer.class);
        if (colExists == null || colExists == 0) return;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, track_list FROM cd WHERE track_list IS NOT NULL AND track_list <> ''");

        int migrated = 0;
        for (Map<String, Object> row : rows) {
            Long   id        = ((Number) row.get("id")).longValue();
            String trackList = (String) row.get("track_list");

            Integer existing = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cd_track WHERE cd_id=?", Integer.class, id);
            if (existing != null && existing > 0) continue;

            String[] lines = trackList.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isBlank()) continue;
                int sep = line.indexOf('|');
                String title  = sep >= 0 ? line.substring(0, sep).trim() : line;
                String length = sep >= 0 ? line.substring(sep + 1).trim() : "";
                jdbcTemplate.update(
                    "INSERT INTO cd_track (cd_id,title,length,track_order) VALUES (?,?,?,?)",
                    id, title, length, i);
            }
            migrated++;
        }
        if (migrated > 0) {
            log.info("DataInitializer: migrated {} CD(s) from legacy track_list.", migrated);
        }
    }

    private double[] parseDimString(String raw) {
        if (raw == null || raw.isBlank()) return new double[]{0, 0, 0};
        String[] parts = raw.toLowerCase().replace("cm", "").trim().split("[x×]");
        double[] result = new double[3];
        for (int i = 0; i < Math.min(3, parts.length); i++) {
            try { result[i] = Double.parseDouble(parts[i].trim()); }
            catch (NumberFormatException e) { result[i] = 0; }
        }
        return result;
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setEmail("admin@aims.vn");
        admin.setRoles(Set.of(User.ROLE_ADMIN));
        admin.setFullName("AIMS Administrator");
        admin.setPhone("0901234567");
        admin.setStatus("ACTIVE");
        userRepository.save(admin);

        User manager = new User();
        manager.setUsername("manager");
        manager.setPasswordHash(passwordEncoder.encode(managerPassword));
        manager.setEmail("manager@aims.vn");
        manager.setRoles(Set.of(User.ROLE_PRODUCT_MANAGER));
        manager.setFullName("Product Manager");
        manager.setPhone("0907654321");
        manager.setStatus("ACTIVE");
        userRepository.save(manager);

        log.info("DataInitializer: created admin and manager accounts.");
    }
}
