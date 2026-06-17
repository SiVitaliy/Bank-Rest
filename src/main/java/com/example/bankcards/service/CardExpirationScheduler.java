package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardExpirationScheduler {

    private final CardExpirationService cardExpirationService;

    @Scheduled(fixedRate = 1000000)
    public void expireCards() {
        int updated = cardExpirationService.expireCards();

        if (updated > 0) {
            log.info("Переведено карт в статус EXPIRED: {}", updated);
        }
    }
}