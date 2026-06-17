package com.example.bankcards.service;


import com.example.bankcards.dto.filter.CardFilter;
import com.example.bankcards.dto.request.ChangeBalanceRequest;
import com.example.bankcards.dto.response.CardDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.entity.Card;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.specification.CardSpecificationBuilder;
import com.example.bankcards.util.mapper.CardMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final BigDecimal MAX_AMOUNT = new BigDecimal("10000000");

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final EncryptionService encryptionService;
    private final CardSpecificationBuilder cardSpecificationBuilder;



    public PageResponse<CardDto> findAllCards(CardFilter filter, Pageable pageable) {

        Specification<Card> spec= cardSpecificationBuilder.buildSpecForAdmin(filter);
        Page<Card> pageOfCards= cardRepository.findAll(spec,pageable);
        log.info("Admin поиск карт. filter={}, total={}",
                filter, pageOfCards.getTotalElements());
        return PageResponse.from(pageOfCards,cardMapper::toDto);
    }

    public PageResponse<CardDto> findAllCardsFotUser(CardFilter filter, Pageable pageable, User user) {
        Specification<Card> spec= cardSpecificationBuilder.buildSpecForUser(filter,user.getId());
        Page<Card> pageOfCards  = cardRepository.findAll(spec,pageable);
        log.info("Поиск карт пользователя id={}, total={}",
                user.getId(), pageOfCards.getTotalElements());
        return PageResponse.from(pageOfCards,cardMapper::toDto);
    }

    public CardDto findById(Long id){
        log.debug("Поиск карты id={}", id);
        return cardMapper.toDto(cardRepository.findById(id)
                .orElseThrow(()-> new CardNotFoundException(id)));
    }



    public CardDto save(Long userId, CreateCardRequest request, User admin) {
        String fullNumber = request.cardNumber().replaceAll("\\D", "");
        if (fullNumber.length()!=16){
            throw new InvalidCardException();
        }
        String encryptedNumber = encryptionService.encrypt(fullNumber);
        if (cardRepository.existsByCardNumber(encryptedNumber)){
            throw new CardAlreadyExistsException();
        }
        log.info(fullNumber+ " -> "+ encryptedNumber);
        String lastFour = fullNumber.substring(fullNumber.length() - 4);
        User owner = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
        Card newCard = new Card();
        newCard.setCardNumber(encryptedNumber);
        newCard.setExpirationDate(request.expirationDate().atEndOfMonth());
        newCard.setStatus(Card.CardStatus.ACTIVE);
        newCard.setOwner(owner);
        newCard.setCreatedByAdmin(admin);
        newCard.setLastFour(lastFour);
        newCard.setBalance(BigDecimal.ZERO); //String
        log.info("Создание карты userId={}, adminId={}", userId, admin.getId());
        return cardMapper.toDto(cardRepository.save(newCard));
    }

    public CardDto setNewExpirationDate(Long cardId, YearMonth newExpirationDate, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        card.setExpirationDate(newExpirationDate.atEndOfMonth());
        cardRepository.save(card);
        log.info("Обновление срока карты id={}, newDate={}", cardId, newExpirationDate);
        return cardMapper.toDto(card);
    }

    public CardDto addBalance(Long id, ChangeBalanceRequest request,User user) {
        Card card = cardRepository.findByIdAndOwnerId(id,user.getId()).orElseThrow(
                ()->new CardNotFoundException(id)
        );
        BigDecimal amount = request.amount();
        if (amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Введите сумму больше нуля");
        }
        if (amount.compareTo(MAX_AMOUNT)>0){
            throw new IllegalArgumentException("Слишком большая сумма пополнения");
        }
        card.setBalance(card.getBalance().add(amount));
        log.info("Операция BALANCE ADD cardId={}, userId={}, amount={}",
                id, user.getId(), amount);
        return cardMapper.toDto(cardRepository.save(card));
    }
    public CardDto subtractBalance(Long id, ChangeBalanceRequest request,User user) {
        Card card = cardRepository.findByIdAndOwnerId(id,user.getId()).orElseThrow(
                ()->new CardNotFoundException(id)
        );
        BigDecimal amount = request.amount();

        if (amount.compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("Введите сумму больше нуля");
        }
        if (amount.compareTo(card.getBalance())>0){
            throw new NotEnoughMoneyException();
        }

        card.setBalance(card.getBalance().subtract(request.amount()));
        log.info("Операция BALANCE SUB cardId={}, userId={}, amount={}",
                id, user.getId(), amount);
        return cardMapper.toDto(cardRepository.save(card));
    }

    public CardDto findByIdForUser(Long id, User user) {
        Card card = cardRepository.findByIdAndOwnerId(id,user.getId()).orElseThrow(
                ()->new CardNotFoundException(id)
        );
        return cardMapper.toDto(card);
    }
}

