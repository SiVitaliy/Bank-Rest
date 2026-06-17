package com.example.bankcards.service;

import com.example.bankcards.dto.filter.CardRequestFilter;
import com.example.bankcards.dto.response.CardRequestDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.request.CardActionRequest;
import com.example.bankcards.dto.request.ProcessCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.RequestNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardRequestRepository;
import com.example.bankcards.specification.CardRequestSpecificationBuilder;
import com.example.bankcards.util.mapper.CardRequestMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CardRequestService {
    private final CardRequestRepository cardRequestRepository;
    private final CardRepository cardRepository;
    private final CardRequestMapper cardRequestMapper;
    private final CardRequestSpecificationBuilder cardRequestSpecificationBuilder;

    public PageResponse<CardRequestDto> findAllRequests(CardRequestFilter filter,Pageable pageable) {

        Specification<CardRequest> spec = cardRequestSpecificationBuilder.buildSpec(filter);
        Page<CardRequest> pageOfRequests = cardRequestRepository.findAll(spec, pageable);
        log.info("Поиск заявок на карты. Фильтр={}, найдено={}",
                filter, pageOfRequests.getTotalElements());
        return PageResponse.from(pageOfRequests, cardRequestMapper::toDto);
    }

    @Transactional
    public CardRequestDto save(CardActionRequest request, User user) {
        CardRequest.RequestType requestType =
                CardRequest.RequestType.valueOf(request.requestType());

        CardRequest cardRequest = new CardRequest();
        cardRequest.setRequester(user);
        cardRequest.setRequestType(requestType);

        if (requestType != CardRequest.RequestType.ISSUE) {
            Card card = resolveCard(request.cardId(), user.getId());
            cardRequest.setCard(card);
        }
        log.info("Создание заявки. userId={}, type={}, cardId={}",
                user.getId(), request.requestType(), request.cardId());
        return cardRequestMapper.toDto(cardRequestRepository.save(cardRequest));

    }
    private Card resolveCard(Long cardId, Long userId) {
        if (cardId == null) {
            throw new IllegalArgumentException(
                    "Для операции с существующей картой необходимо указать id"
            );
        }

        return cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    public CardRequestDto process(Long requestId, ProcessCardRequest request, User admin) {
        CardRequest cardRequest = cardRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (cardRequest.getStatus() != CardRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Запрос уже обработан");
        }


        if ("APPROVE".equals(request.action())) {

            cardRequest.setStatus(CardRequest.RequestStatus.APPROVED);
            executeRequestAction(cardRequest);
        } else if ("REJECT".equals(request.action())) {
            cardRequest.setStatus(CardRequest.RequestStatus.REJECTED);
        } else {
            throw new IllegalArgumentException("Недопустимое действие: " + request.action());
        }

        cardRequest.setProcessedBy(admin);
        log.info("Обработка заявки requestId={}, action={}, adminId={}",
                requestId, request.action(), admin.getId());
        return cardRequestMapper.toDto(cardRequestRepository.save(cardRequest));
    }

    private void executeRequestAction(CardRequest cardRequest) {
        log.debug("Выполнение действия заявки type={}", cardRequest.getRequestType());
        switch (cardRequest.getRequestType()) {
            case BLOCK -> {
                Card card = cardRequest.getCard();
                card.setStatus(Card.CardStatus.BLOCKED);
                cardRepository.save(card);
            }
            case ACTIVATE -> {
                Card card = cardRequest.getCard();
                if (card.getStatus() == Card.CardStatus.EXPIRED) {
                    throw new IllegalStateException("Просроченную карту нельзя активировать");
                }
                card.setStatus(Card.CardStatus.ACTIVE);
                cardRepository.save(card);
            }
            case DELETE -> {
                Card card = cardRequest.getCard();
                cardRequest.setCard(null);
                cardRepository.delete(card);
            }
        }
    }
}
