package com.example.bankcards.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/**
 * Планировщик автоматического обновления статуса просроченных карт.
 * Периодически запускает проверку карт и переводит карты с истёкшим сроком
 * действия в статус {@code EXPIRED}.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class CardExpirationScheduler {

    private final CardExpirationService cardExpirationService;
    /**
     * Запускает процесс перевода просроченных карт в статус {@code EXPIRED}.
     * Метод выполняется с фиксированным интервалом.
     */
    @Scheduled(fixedRate = 1000000)
    public void expireCards() {
        int updated = cardExpirationService.expireCards();

        if (updated > 0) {
            log.info("Переведено карт в статус EXPIRED: {}", updated);
        }
    }
}