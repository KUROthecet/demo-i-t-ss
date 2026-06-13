package com.aims.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class ExchangeRateService {

    private static final String RATE_API_URL =
            "https://open.er-api.com/v6/latest/USD";

    @Value("${paypal.vnd-to-usd-rate:25000}")
    private int fallbackRate;

    private final RestClient restClient = RestClient.create();

    private volatile double cachedVndPerUsd;

    @PostConstruct
    public void init() {
        this.cachedVndPerUsd = fallbackRate;
        refreshRate();
    }

    @Scheduled(fixedDelay = 3_600_000)
    @SuppressWarnings("unchecked")
    public void refreshRate() {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(RATE_API_URL)
                    .retrieve()
                    .body(Map.class);

            if (body == null) return;

            Map<String, Object> rates = (Map<String, Object>) body.get("rates");
            if (rates == null || rates.get("VND") == null) return;

            double rate = ((Number) rates.get("VND")).doubleValue();
            this.cachedVndPerUsd = rate;
            log.info("[ExchangeRate] 1 USD = {} VND (refreshed)", rate);

        } catch (Exception e) {
            log.warn("[ExchangeRate] Refresh failed, using {} VND/USD: {}", cachedVndPerUsd, e.getMessage());
        }
    }

    public double getVndPerUsd() {
        return cachedVndPerUsd;
    }
}
