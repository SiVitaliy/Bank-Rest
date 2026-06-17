package com.example.bankcards.service;

import com.example.bankcards.dto.request.TransactionRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.TransactionDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.NotEnoughMoneyException;
import com.example.bankcards.exception.TransactionNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.util.mapper.TransactionMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public PageResponse<TransactionDto> findAllByUser(Pageable pageable, User user) {
        log.debug("Получение транзакций пользователя userId={}, page={}, size={}",
                user.getId(), pageable.getPageNumber(), pageable.getPageSize());

        Page<Transaction> pageOfTransactions = transactionRepository.findAllByOwner(user, pageable);

        log.info("Получены транзакции пользователя userId={}, найдено={}",
                user.getId(), pageOfTransactions.getTotalElements());

        return PageResponse.from(pageOfTransactions, transactionMapper::toDto);
    }

    public PageResponse<TransactionDto> findAllByUserId(Pageable pageable, Long userId) {
        log.debug("Получение транзакций пользователя администратором userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Transaction> pageOfTransactions =
                transactionRepository.findAllByOwnerId(userId, pageable);

        log.info("Получены транзакции пользователя userId={}, найдено={}",
                userId, pageOfTransactions.getTotalElements());
        return PageResponse.from(pageOfTransactions, transactionMapper::toDto);
    }

    public TransactionDto getTransaction(Long id) {
        log.debug("Получение транзакции администратором transactionId={}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {log.warn("Транзакция не найдена transactionId={}", id);
                    return new TransactionNotFoundException(id);});

        return transactionMapper.toDto(transaction);
    }

    public TransactionDto getTransactionForUser(Long id, User user) {
        log.debug("Получение транзакции transactionId={} пользователем userId={}",
                id, user.getId());

        Transaction transaction = transactionRepository.findByIdForUser(id, user)
                .orElseThrow(() -> {log.warn("Транзакция не найдена или недоступна transactionId={}, userId={}",
                            id, user.getId());
                    return new TransactionNotFoundException(id);
                });

        return transactionMapper.toDto(transaction);
    }

    public TransactionDto performTransaction(TransactionRequest request, User user) {
        log.info("Начало выполнения транзакции userId={}, fromCardId={}, toCardId={}, amount={}",
                user.getId(), request.fromCardId(), request.toCardId(), request.amount());

        if (Objects.equals(request.fromCardId(), request.toCardId())) {
            log.warn("Отклонена транзакция между одной картой cardId={}, userId={}",
                    request.fromCardId(), user.getId());
            throw new IllegalArgumentException("Выберите разные карты");
        }

        Card fromCard = cardRepository.findById(request.fromCardId())
                .orElseThrow(() -> {
                    log.warn("Карта отправителя не найдена cardId={}", request.fromCardId());
                    return new CardNotFoundException(request.fromCardId());
                });

        Card toCard = cardRepository.findById(request.toCardId())
                .orElseThrow(() -> {
                    log.warn("Карта получателя не найдена cardId={}", request.toCardId());
                    return new CardNotFoundException(request.toCardId());
                });

        if (fromCard.getOwner().getId().longValue() != user.getId()
                || toCard.getOwner().getId().longValue() != user.getId()) {
            log.warn("Отклонена транзакция: пользователь не является владельцем карт userId={}, fromCardId={}, toCardId={}",
                    user.getId(), request.fromCardId(), request.toCardId());
            throw new IllegalArgumentException("Вы не владелец карты");
        }
        if (request.amount().compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Введите сумму больше нуля");
        }
        if (fromCard.getStatus() != Card.CardStatus.ACTIVE
                || toCard.getStatus() != Card.CardStatus.ACTIVE) {
            log.warn("Отклонена транзакция из-за статуса карты fromCardId={}, fromStatus={}, toCardId={}, toStatus={}",
                    fromCard.getId(), fromCard.getStatus(), toCard.getId(), toCard.getStatus());
            throw new IllegalStateException("Карты должны быть активными ");
        }

        if (fromCard.getBalance().compareTo(request.amount()) < 0) {
            log.warn("Недостаточно средств для транзакции cardId={}, requestedAmount={}, currentBalance={}",
                    fromCard.getId(), request.amount(), fromCard.getBalance());
            throw new NotEnoughMoneyException();
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
        toCard.setBalance(toCard.getBalance().add(request.amount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.amount());
        transaction.setOwner(fromCard.getOwner());
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Транзакция успешно выполнена transactionId={}, userId={}, fromCardId={}, toCardId={}, amount={}",
                savedTransaction.getId(), user.getId(), fromCard.getId(), toCard.getId(), request.amount());

        return transactionMapper.toDto(savedTransaction);
    }
}