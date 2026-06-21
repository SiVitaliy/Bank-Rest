package com.example.bankcards.serviceTests.CardService;

import com.example.bankcards.dto.filter.CardFilter;
import com.example.bankcards.dto.request.ChangeBalanceRequest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.CardDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InvalidCardException;
import com.example.bankcards.exception.NotEnoughMoneyException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.EncryptionService;
import com.example.bankcards.specification.CardSpecificationBuilder;
import com.example.bankcards.util.mapper.CardMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private CardSpecificationBuilder cardSpecificationBuilder;

    @InjectMocks
    private CardService sut;

    @Test
    void findAllCards_validFilter_returnsMappedPage() {
        CardFilter filter = mock(CardFilter.class);
        Pageable pageable = PageRequest.of(0, 5);
        Specification<Card> specification = mock(Specification.class);

        Card card = new Card();
        CardDto cardDto = mock(CardDto.class);

        var page = new PageImpl<>(
                List.of(card),
                pageable,
                1
        );

        when(cardSpecificationBuilder.buildSpecForAdmin(filter))
                .thenReturn(specification);
        when(cardRepository.findAll(specification, pageable))
                .thenReturn(page);
        when(cardMapper.toDto(card))
                .thenReturn(cardDto);

        PageResponse<CardDto> result =
                sut.findAllCards(filter, pageable);

        assertThat(result).isNotNull();

        verify(cardSpecificationBuilder).buildSpecForAdmin(filter);
        verify(cardRepository).findAll(specification, pageable);
        verify(cardMapper).toDto(card);
    }

    @Test
    void findAllCardsFotUser_validFilter_returnsMappedPage() {
        CardFilter filter = mock(CardFilter.class);
        Pageable pageable = PageRequest.of(0, 5);
        Specification<Card> specification = mock(Specification.class);

        User user = user(10L);
        Card card = new Card();
        CardDto cardDto = mock(CardDto.class);

        var page = new PageImpl<>(
                List.of(card),
                pageable,
                1
        );

        when(cardSpecificationBuilder.buildSpecForUser(filter, 10L))
                .thenReturn(specification);
        when(cardRepository.findAll(specification, pageable))
                .thenReturn(page);
        when(cardMapper.toDto(card))
                .thenReturn(cardDto);

        PageResponse<CardDto> result =
                sut.findAllCardsForUser(filter, pageable, user);

        assertThat(result).isNotNull();

        verify(cardSpecificationBuilder).buildSpecForUser(filter, 10L);
        verify(cardRepository).findAll(specification, pageable);
        verify(cardMapper).toDto(card);
    }

    @Test
    void findById_existingCard_returnsMappedCard() {
        Card card = new Card();
        CardDto expectedResult = mock(CardDto.class);

        when(cardRepository.findById(3L))
                .thenReturn(Optional.of(card));
        when(cardMapper.toDto(card))
                .thenReturn(expectedResult);

        CardDto result = sut.findById(3L);

        assertThat(result).isSameAs(expectedResult);

        verify(cardRepository).findById(3L);
        verify(cardMapper).toDto(card);
    }

    @Test
    void findById_cardNotFound_throwsCardNotFoundException() {
        when(cardRepository.findById(3L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.findById(3L))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardRepository).findById(3L);
        verifyNoInteractions(cardMapper);
    }

    @Test
    void save_validRequest_savesCard() {
        User owner = user(10L);
        User admin = user(100L);

        CreateCardRequest request = mock(CreateCardRequest.class);

        when(request.cardNumber())
                .thenReturn("1234 5678 9012 3456");
        when(request.expirationDate())
                .thenReturn(YearMonth.of(2030, 5));

        when(encryptionService.encrypt("1234567890123456"))
                .thenReturn("encrypted-card-number");
        when(userRepository.findById(10L))
                .thenReturn(Optional.of(owner));
        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CardDto expectedResult = mock(CardDto.class);

        when(cardMapper.toDto(any(Card.class)))
                .thenReturn(expectedResult);

        CardDto result = sut.save(10L, request, admin);

        assertThat(result).isSameAs(expectedResult);

        ArgumentCaptor<Card> cardCaptor =
                ArgumentCaptor.forClass(Card.class);

        verify(cardRepository).save(cardCaptor.capture());

        Card savedCard = cardCaptor.getValue();

        assertThat(savedCard.getCardNumber())
                .isEqualTo("encrypted-card-number");
        assertThat(savedCard.getLastFour())
                .isEqualTo("3456");
        assertThat(savedCard.getExpirationDate())
                .isEqualTo(LocalDate.of(2030, 5, 31));
        assertThat(savedCard.getStatus())
                .isEqualTo(Card.CardStatus.ACTIVE);
        assertThat(savedCard.getBalance())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedCard.getOwner())
                .isSameAs(owner);
        assertThat(savedCard.getCreatedByAdmin())
                .isSameAs(admin);

        verify(encryptionService)
                .encrypt("1234567890123456");
        verify(userRepository).findById(10L);
        verify(cardMapper).toDto(savedCard);
    }

    @Test
    void save_cardNumberContainsNonDigits_removesFormattingAndSavesCard() {
        User owner = user(10L);
        User admin = user(100L);

        CreateCardRequest request = mock(CreateCardRequest.class);

        when(request.cardNumber())
                .thenReturn("1234-5678-9012-3456");
        when(request.expirationDate())
                .thenReturn(YearMonth.of(2030, 5));

        when(encryptionService.encrypt("1234567890123456"))
                .thenReturn("encrypted-card-number");
        when(userRepository.findById(10L))
                .thenReturn(Optional.of(owner));
        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cardMapper.toDto(any(Card.class)))
                .thenReturn(mock(CardDto.class));

        sut.save(10L, request, admin);

        verify(encryptionService)
                .encrypt("1234567890123456");
    }

    @Test
    void save_invalidCardNumber_throwsInvalidCardException() {
        CreateCardRequest request = mock(CreateCardRequest.class);

        when(request.cardNumber())
                .thenReturn("1234 5678");

        assertThatThrownBy(() ->
                sut.save(10L, request, user(100L))
        ).isInstanceOf(InvalidCardException.class);

        verifyNoInteractions(
                encryptionService,
                userRepository,
                cardRepository,
                cardMapper
        );
    }

    @Test
    void save_ownerNotFound_throwsUserNotFoundException() {
        CreateCardRequest request = mock(CreateCardRequest.class);

        when(request.cardNumber())
                .thenReturn("1234567890123456");
        when(encryptionService.encrypt("1234567890123456"))
                .thenReturn("encrypted-card-number");
        when(userRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sut.save(10L, request, user(100L))
        ).isInstanceOf(UserNotFoundException.class);

        verify(encryptionService)
                .encrypt("1234567890123456");
        verify(userRepository).findById(10L);
        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper);
    }

    @Test
    void setNewExpirationDate_existingCard_updatesExpirationDate() {
        Card card = new Card();
        CardDto expectedResult = mock(CardDto.class);
        YearMonth newExpirationDate = YearMonth.of(2031, 7);

        when(cardRepository.findById(3L))
                .thenReturn(Optional.of(card));
        when(cardRepository.save(card))
                .thenReturn(card);
        when(cardMapper.toDto(card))
                .thenReturn(expectedResult);

        CardDto result = sut.setNewExpirationDate(
                3L,
                newExpirationDate,
                user(100L)
        );

        assertThat(result).isSameAs(expectedResult);
        assertThat(card.getExpirationDate())
                .isEqualTo(LocalDate.of(2031, 7, 31));

        verify(cardRepository).save(card);
        verify(cardMapper).toDto(card);
    }

    @Test
    void setNewExpirationDate_cardNotFound_throwsCardNotFoundException() {
        when(cardRepository.findById(3L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sut.setNewExpirationDate(
                        3L,
                        YearMonth.of(2031, 7),
                        user(100L)
                )
        ).isInstanceOf(CardNotFoundException.class);

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper);
    }

    @Test
    void addBalance_validAmount_increasesBalance() {
        User user = user(10L);
        Card card = cardWithBalance("100.00");
        ChangeBalanceRequest request = changeBalanceRequest("50.00");

        CardDto expectedResult = mock(CardDto.class);

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));
        when(cardRepository.save(card))
                .thenReturn(card);
        when(cardMapper.toDto(card))
                .thenReturn(expectedResult);

        CardDto result = sut.addBalance(3L, request, user);

        assertThat(result).isSameAs(expectedResult);
        assertThat(card.getBalance())
                .isEqualByComparingTo("150.00");

        verify(cardRepository).save(card);
        verify(cardMapper).toDto(card);
    }

    @Test
    void addBalance_cardNotFound_throwsCardNotFoundException() {
        User user = user(10L);
        ChangeBalanceRequest request = mock(ChangeBalanceRequest.class);

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sut.addBalance(3L, request, user)
        ).isInstanceOf(CardNotFoundException.class);

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper);
    }

    @Test
    void addBalance_zeroAmount_throwsIllegalArgumentException() {
        User user = user(10L);
        Card card = cardWithBalance("100.00");
        ChangeBalanceRequest request = changeBalanceRequest("0.00");

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));

        assertThatThrownBy(() ->
                sut.addBalance(3L, request, user)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Введите сумму больше нуля");

        assertThat(card.getBalance())
                .isEqualByComparingTo("100.00");

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper);
    }

    @Test
    void addBalance_negativeAmount_throwsIllegalArgumentException() {
        User user = user(10L);
        Card card = cardWithBalance("100.00");
        ChangeBalanceRequest request = changeBalanceRequest("-1.00");

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));

        assertThatThrownBy(() ->
                sut.addBalance(3L, request, user)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Введите сумму больше нуля");

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper);
    }

    @Test
    void addBalance_amountExceedsMaximum_throwsIllegalArgumentException() {
        User user = user(10L);
        Card card = cardWithBalance("100.00");
        ChangeBalanceRequest request =
                changeBalanceRequest("10000000.01");

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));

        assertThatThrownBy(() ->
                sut.addBalance(3L, request, user)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Слишком большая сумма пополнения");

        assertThat(card.getBalance())
                .isEqualByComparingTo("100.00");

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper);
    }

    @Test
    void addBalance_maximumAmount_savesCard() {
        User user = user(10L);
        Card card = cardWithBalance("0.00");
        ChangeBalanceRequest request =
                changeBalanceRequest("10000000");

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));
        when(cardRepository.save(card))
                .thenReturn(card);
        when(cardMapper.toDto(card))
                .thenReturn(mock(CardDto.class));

        sut.addBalance(3L, request, user);

        assertThat(card.getBalance())
                .isEqualByComparingTo("10000000");

        verify(cardRepository).save(card);
    }

    @Test
    void subtractBalance_validAmount_decreasesBalance() {
        User user = user(10L);
        Card card = cardWithBalance("100.00");
        ChangeBalanceRequest request = changeBalanceRequest("40.00");

        CardDto expectedResult = mock(CardDto.class);

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));
        when(cardRepository.save(card))
                .thenReturn(card);
        when(cardMapper.toDto(card))
                .thenReturn(expectedResult);

        CardDto result = sut.subtractBalance(3L, request, user);

        assertThat(result).isSameAs(expectedResult);
        assertThat(card.getBalance())
                .isEqualByComparingTo("60.00");

        verify(cardRepository).save(card);
        verify(cardMapper).toDto(card);
    }

    @Test
    void subtractBalance_fullBalance_setsBalanceToZero() {
        User user = user(10L);
        Card card = cardWithBalance("100.00");
        ChangeBalanceRequest request = changeBalanceRequest("100.00");

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));
        when(cardRepository.save(card))
                .thenReturn(card);
        when(cardMapper.toDto(card))
                .thenReturn(mock(CardDto.class));

        sut.subtractBalance(3L, request, user);

        assertThat(card.getBalance())
                .isEqualByComparingTo(BigDecimal.ZERO);

        verify(cardRepository).save(card);
    }

    @Test
    void subtractBalance_cardNotFound_throwsCardNotFoundException() {
        User user = user(10L);
        ChangeBalanceRequest request = mock(ChangeBalanceRequest.class);

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sut.subtractBalance(3L, request, user)
        ).isInstanceOf(CardNotFoundException.class);

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper);
    }

    @Test
    void subtractBalance_zeroAmount_throwsIllegalArgumentException() {
        User user = user(10L);
        Card card = cardWithBalance("100.00");
        ChangeBalanceRequest request = changeBalanceRequest("0.00");

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));

        assertThatThrownBy(() ->
                sut.subtractBalance(3L, request, user)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Введите сумму больше нуля");

        assertThat(card.getBalance())
                .isEqualByComparingTo("100.00");

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper);
    }

    @Test
    void subtractBalance_insufficientFunds_throwsNotEnoughMoneyException() {
        User user = user(10L);
        Card card = cardWithBalance("100.00");
        ChangeBalanceRequest request = changeBalanceRequest("100.01");

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));

        assertThatThrownBy(() ->
                sut.subtractBalance(3L, request, user)
        ).isInstanceOf(NotEnoughMoneyException.class);

        assertThat(card.getBalance())
                .isEqualByComparingTo("100.00");

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper);
    }

    @Test
    void findByIdForUser_existingCard_returnsMappedCard() {
        User user = user(10L);
        Card card = new Card();
        CardDto expectedResult = mock(CardDto.class);

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));
        when(cardMapper.toDto(card))
                .thenReturn(expectedResult);

        CardDto result = sut.findByIdForUser(3L, user);

        assertThat(result).isSameAs(expectedResult);

        verify(cardRepository)
                .findByIdAndOwnerId(3L, 10L);
        verify(cardMapper).toDto(card);
    }

    @Test
    void findByIdForUser_cardNotFound_throwsCardNotFoundException() {
        User user = user(10L);

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sut.findByIdForUser(3L, user)
        ).isInstanceOf(CardNotFoundException.class);

        verify(cardRepository)
                .findByIdAndOwnerId(3L, 10L);
        verifyNoInteractions(cardMapper);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Card cardWithBalance(String balance) {
        Card card = new Card();
        card.setBalance(new BigDecimal(balance));
        return card;
    }

    private ChangeBalanceRequest changeBalanceRequest(String amount) {
        ChangeBalanceRequest request =
                mock(ChangeBalanceRequest.class);

        when(request.amount())
                .thenReturn(new BigDecimal(amount));

        return request;
    }
}

