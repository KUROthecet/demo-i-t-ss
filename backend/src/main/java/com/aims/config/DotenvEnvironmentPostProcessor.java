package com.aims.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "dotenv";
    private static final List<String> CANDIDATE_PATHS = List.of(".env", "../.env");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        locateDotenvFile()
            .map(this::readEntries)
            .filter(entries -> !entries.isEmpty())
            .ifPresent(entries -> environment.getPropertySources()
                .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, entries)));
    }

    private Optional<Path> locateDotenvFile() {
        return CANDIDATE_PATHS.stream()
            .map(Path::of)
            .filter(Files::isRegularFile)
            .findFirst();
    }

    private Map<String, Object> readEntries(Path path) {
        Map<String, Object> entries = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(path)) {
                parseLine(line).ifPresent(entry -> entries.put(entry.key(), entry.value()));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file at " + path, e);
        }
        return entries;
    }

    private Optional<DotenvEntry> parseLine(String rawLine) {
        String trimmed = rawLine.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return Optional.empty();

        int separatorIndex = trimmed.indexOf('=');
        if (separatorIndex < 0) return Optional.empty();

        String key   = trimmed.substring(0, separatorIndex).trim();
        String value = stripQuotes(trimmed.substring(separatorIndex + 1).trim());
        return Optional.of(new DotenvEntry(key, value));
    }

    private String stripQuotes(String value) {
        boolean isQuoted = value.length() > 1
            && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")));
        return isQuoted ? value.substring(1, value.length() - 1) : value;
    }

    private record DotenvEntry(String key, String value) {}
}
