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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
/**
 * Сервис для работы с банковскими картами.
 * Отвечает за поиск карт, создание новых карт, изменение срока действия,
 * пополнение и списание баланса, а также получение карт с учётом прав
 * текущего пользователя.
 */
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


    /**
     * Возвращает страницу карт для администратора.
     * Администратор может искать карты всех пользователей с применением
     * фильтрации, пагинации и сортировки.
     * @param filter параметры фильтрации карт
     * @param pageable параметры пагинации и сортировки
     * @return страница найденных карт
     */

    public PageResponse<CardDto> findAllCards(CardFilter filter, Pageable pageable) {

        Specification<Card> spec= cardSpecificationBuilder.buildSpecForAdmin(filter);
        Page<Card> pageOfCards= cardRepository.findAll(spec,pageable);
        log.info("Admin поиск карт. filter={}, total={}",
                filter, pageOfCards.getTotalElements());
        return PageResponse.from(pageOfCards,cardMapper::toDto);
    }
    /**
     * Возвращает страницу карт текущего пользователя.
     * Пользователь получает только собственные карты. Фильтрация строится
     * с учётом идентификатора текущего пользователя.
     * @param filter параметры фильтрации карт
     * @param pageable параметры пагинации и сортировки
     * @param user текущий аутентифицированный пользователь
     * @return страница карт пользователя
     */
    public PageResponse<CardDto> findAllCardsForUser(CardFilter filter, Pageable pageable, User user) {
        Specification<Card> spec= cardSpecificationBuilder.buildSpecForUser(filter,user.getId());
        Page<Card> pageOfCards  = cardRepository.findAll(spec,pageable);
        log.info("Поиск карт пользователя id={}, total={}",
                user.getId(), pageOfCards.getTotalElements());
        return PageResponse.from(pageOfCards,cardMapper::toDto);
    }
    /**
     * Возвращает карту по идентификатору.
     * Метод используется для административного получения карты без проверки
     * принадлежности текущему пользователю.
     * @param id идентификатор карты
     * @return данные найденной карты
     * @throws CardNotFoundException если карта не найдена
     */
    public CardDto findById(Long id){
        log.debug("Поиск карты id={}", id);
        return cardMapper.toDto(cardRepository.findById(id)
                .orElseThrow(()-> new CardNotFoundException(id)));
    }


    /**
     * Создаёт новую банковскую карту для указанного пользователя.
     * Проверяет корректность номера карты, шифрует полный номер карты,
     * проверяет уникальность зашифрованного номера, сохраняет последние
     * четыре цифры для отображения и назначает карту указанному владельцу.
     * @param userId идентификатор владельца карты
     * @param request данные создаваемой карты
     * @param admin администратор, создающий карту
     * @return созданная карта
     * @throws InvalidCardException если номер карты некорректен
     * @throws CardAlreadyExistsException если карта с таким номером уже существует
     * @throws UserNotFoundException если пользователь-владелец не найден
     */
    public CardDto save(Long userId, CreateCardRequest request, User admin) {
        String fullNumber = request.cardNumber().replaceAll("\\D", "");
        if (fullNumber.length()!=16){
            throw new InvalidCardException();
        }
        String encryptedNumber = encryptionService.encrypt(fullNumber);
        if (cardRepository.existsByCardNumber(encryptedNumber)){
            throw new CardAlreadyExistsException();
        }
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
    /**
     * Обновляет срок действия карты.
     *
     * Если новая дата окончания срока действия находится в будущем,
     * карта переводится в статус {@link Card.CardStatus#ACTIVE}.
     *
     * @param cardId идентификатор карты
     * @param newExpirationDate новый месяц и год окончания срока действия
     * @param user пользователь, выполняющий операцию
     * @return карта с обновлённым сроком действия
     * @throws CardNotFoundException если карта не найдена
     */
    public CardDto setNewExpirationDate(Long cardId, YearMonth newExpirationDate, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        card.setExpirationDate(newExpirationDate.atEndOfMonth());
        if (newExpirationDate.atEndOfMonth().isAfter(LocalDate.now())){
            card.setStatus(Card.CardStatus.ACTIVE);
        }
        cardRepository.save(card);
        log.info("Обновление срока карты id={}, newDate={}", cardId, newExpirationDate);
        return cardMapper.toDto(card);
    }
    /**
     * Пополняет баланс карты текущего пользователя.
     *
     * Проверяет, что карта принадлежит пользователю, сумма положительная
     * и не превышает максимально допустимое значение.
     *
     * @param id идентификатор карты
     * @param request данные операции пополнения
     * @param user текущий аутентифицированный пользователь
     * @return карта с обновлённым балансом
     * @throws CardNotFoundException если карта не найдена или не принадлежит пользователю
     * @throws IllegalArgumentException если сумма некорректна
     */
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

    /**
     * Списывает средства с баланса карты текущего пользователя.
     * Проверяет, что карта принадлежит пользователю, сумма положительная
     * и на карте достаточно средств для списания.
     * @param id идентификатор карты
     * @param request данные операции списания
     * @param user текущий аутентифицированный пользователь
     * @return карта с обновлённым балансом
     * @throws CardNotFoundException если карта не найдена или не принадлежит пользователю
     * @throws IllegalArgumentException если сумма некорректна
     * @throws NotEnoughMoneyException если на карте недостаточно средств
     */
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
    /**
     * Возвращает карту текущего пользователя по идентификатору.
     * Проверяет, что карта существует и принадлежит текущему пользователю.
     * @param id идентификатор карты
     * @param user текущий аутентифицированный пользователь
     * @return данные найденной карты
     * @throws CardNotFoundException если карта не найдена или не принадлежит пользователю
     */
    public CardDto findByIdForUser(Long id, User user) {
        Card card = cardRepository.findByIdAndOwnerId(id,user.getId()).orElseThrow(
                ()->new CardNotFoundException(id)
        );
        return cardMapper.toDto(card);
    }
}

