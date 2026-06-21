package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
/**
 * Сервис обработки банковских карт с истёкшим сроком действия.
 * Отвечает за поиск карт, у которых дата окончания срока действия меньше
 * текущей даты, и перевод таких карт в статус {@code EXPIRED}.
 */
@Service
@RequiredArgsConstructor
public class CardExpirationService {

    private final CardRepository cardRepository;
    /**
     * Переводит просроченные карты в статус {@link Card.CardStatus#EXPIRED}.
     * Метод выполняется в транзакции, так как производит массовое обновление
     * статуса карт в базе данных.
     * @return количество карт, переведённых в статус {@code EXPIRED}
     */
    @Transactional
    public int expireCards() {
        return cardRepository.markExpiredCards(
                LocalDate.now(),
                Card.CardStatus.EXPIRED
        );
    }
}