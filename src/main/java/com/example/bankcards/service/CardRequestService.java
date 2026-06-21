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

import java.time.LocalDate;
import java.util.Objects;
/**
 * Сервис для работы с заявками на операции с банковскими картами.
 * Отвечает за создание пользовательских заявок, поиск заявок с фильтрацией
 * и обработку заявок администратором. При одобрении заявки выполняет
 * соответствующее действие над картой: блокировку, активацию или удаление.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CardRequestService {
    private final CardRequestRepository cardRequestRepository;
    private final CardRepository cardRepository;
    private final CardRequestMapper cardRequestMapper;
    private final CardRequestSpecificationBuilder cardRequestSpecificationBuilder;
    /**
     * Возвращает страницу заявок на операции с картами.
     * Заявки фильтруются по переданным параметрам и возвращаются
     * с учётом пагинации и сортировки.
     * @param filter параметры фильтрации заявок
     * @param pageable параметры пагинации и сортировки
     * @return страница найденных заявок
     */
    public PageResponse<CardRequestDto> findAllRequests(CardRequestFilter filter,Pageable pageable) {

        Specification<CardRequest> spec = cardRequestSpecificationBuilder.buildSpec(filter);
        Page<CardRequest> pageOfRequests = cardRequestRepository.findAll(spec, pageable);
        log.info("Поиск заявок на карты. Фильтр={}, найдено={}",
                filter, pageOfRequests.getTotalElements());
        return PageResponse.from(pageOfRequests, cardRequestMapper::toDto);
    }
    /**
     * Создаёт новую заявку на операцию с картой от имени пользователя.
     * Для операций с уже существующей картой проверяет, что карта указана
     * и принадлежит текущему пользователю. Для заявки на выпуск карты
     * существующая карта не требуется.
     * @param request данные создаваемой заявки
     * @param user пользователь, создающий заявку
     * @return созданная заявка
     * @throws IllegalArgumentException если для операции с существующей картой не передан id карты
     * @throws CardNotFoundException если карта не найдена или не принадлежит пользователю
     */

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
    /**
     * Находит карту пользователя для создания заявки.
     * Проверяет, что идентификатор карты передан, а сама карта существует
     * и принадлежит указанному пользователю.
     * @param cardId идентификатор карты
     * @param userId идентификатор пользователя-владельца карты
     * @return найденная карта
     * @throws IllegalArgumentException если идентификатор карты не передан
     * @throws CardNotFoundException если карта не найдена или не принадлежит пользователю
     */
    private Card resolveCard(Long cardId, Long userId) {
        if (cardId == null) {
            throw new IllegalArgumentException(
                    "Для операции с существующей картой необходимо указать id"
            );
        }

        return cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }
    /**
     * Обрабатывает заявку на операцию с картой.
     * Администратор может одобрить или отклонить заявку. При одобрении
     * выполняется действие, соответствующее типу заявки. Повторная обработка
     * уже обработанной заявки запрещена.
     * @param requestId идентификатор заявки
     * @param request данные обработки заявки
     * @param admin администратор, обрабатывающий заявку
     * @return обработанная заявка
     * @throws RequestNotFoundException если заявка не найдена
     * @throws IllegalStateException если заявка уже обработана
     * @throws IllegalArgumentException если передано недопустимое действие
     */
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
    /**
     * Выполняет действие по одобренной заявке.
     * В зависимости от типа заявки блокирует карту, активирует карту
     * или удаляет карту. Просроченную карту активировать нельзя.
     * @param cardRequest одобренная заявка на операцию с картой
     * @throws IllegalStateException если выполняется попытка активировать просроченную карту
     */
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
                if (card.getStatus() == Card.CardStatus.EXPIRED &&
                        card.getExpirationDate().isBefore(LocalDate.now())) {
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
