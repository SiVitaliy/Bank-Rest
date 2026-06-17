package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CardExpirationService {

    private final CardRepository cardRepository;

    @Transactional
    public int expireCards() {
        return cardRepository.markExpiredCards(
                LocalDate.now(),
                Card.CardStatus.EXPIRED
        );
    }
}