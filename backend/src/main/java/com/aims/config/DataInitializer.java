package com.aims.config;

import com.aims.entity.User;
import com.aims.repository.MediaRepository;
import com.aims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final MediaRepository  mediaRepository;
    private final UserRepository   userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final SqlCatalogLoader sqlCatalogLoader;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        seedUsers();

        if (mediaRepository.count() > 0) {
            log.info("DataInitializer: catalog already seeded ({} items). Skipping.", mediaRepository.count());
            return;
        }

        log.info("DataInitializer: seeding catalog from SQL files…");
        sqlCatalogLoader.loadAll();
        log.info("DataInitializer: seeded {} media items.", mediaRepository.count());
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@aims.vn");
        admin.setRole("ADMIN");
        admin.setFullName("AIMS Administrator");
        admin.setPhone("0901234567");
        admin.setStatus("ACTIVE");
        userRepository.save(admin);

        User manager = new User();
        manager.setUsername("manager");
        manager.setPasswordHash(passwordEncoder.encode("manager123"));
        manager.setEmail("manager@aims.vn");
        manager.setRole("PRODUCT_MANAGER");
        manager.setFullName("Product Manager");
        manager.setPhone("0907654321");
        manager.setStatus("ACTIVE");
        userRepository.save(manager);

        log.info("DataInitializer: created admin and manager accounts.");
    }
}
