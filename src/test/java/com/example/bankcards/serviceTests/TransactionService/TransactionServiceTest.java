
package com.example.bankcards.serviceTests.TransactionService;

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
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.util.mapper.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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
class TransactionServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService sut;

    @Test
    void findAllByUser_transactionsExist_returnsMappedPage() {
        User user = user(10L);
        Pageable pageable = PageRequest.of(0, 5);

        Transaction transaction = new Transaction();
        TransactionDto transactionDto = mock(TransactionDto.class);

        var page = new PageImpl<>(
                List.of(transaction),
                pageable,
                1
        );

        when(transactionRepository.findAllByOwner(user, pageable))
                .thenReturn(page);
        when(transactionMapper.toDto(transaction))
                .thenReturn(transactionDto);

        PageResponse<TransactionDto> result =
                sut.findAllByUser(pageable, user);

        assertThat(result).isNotNull();

        verify(transactionRepository)
                .findAllByOwner(user, pageable);
        verify(transactionMapper).toDto(transaction);
    }

    @Test
    void findAllByUserId_transactionsExist_returnsMappedPage() {
        Pageable pageable = PageRequest.of(1, 10);

        Transaction transaction = new Transaction();
        TransactionDto transactionDto = mock(TransactionDto.class);

        var page = new PageImpl<>(
                List.of(transaction),
                pageable,
                1
        );

        when(transactionRepository.findAllByOwnerId(10L, pageable))
                .thenReturn(page);
        when(transactionMapper.toDto(transaction))
                .thenReturn(transactionDto);

        PageResponse<TransactionDto> result =
                sut.findAllByUserId(pageable, 10L);

        assertThat(result).isNotNull();

        verify(transactionRepository)
                .findAllByOwnerId(10L, pageable);
        verify(transactionMapper).toDto(transaction);
    }

    @Test
    void getTransaction_transactionExists_returnsMappedTransaction() {
        Transaction transaction = new Transaction();
        TransactionDto expectedResult = mock(TransactionDto.class);

        when(transactionRepository.findById(15L))
                .thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(transaction))
                .thenReturn(expectedResult);

        TransactionDto result = sut.getTransaction(15L);

        assertThat(result).isSameAs(expectedResult);

        verify(transactionRepository).findById(15L);
        verify(transactionMapper).toDto(transaction);
    }

    @Test
    void getTransaction_transactionNotFound_throwsException() {
        when(transactionRepository.findById(15L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.getTransaction(15L))
                .isInstanceOf(TransactionNotFoundException.class);

        verify(transactionRepository).findById(15L);
        verifyNoInteractions(transactionMapper);
    }

    @Test
    void getTransactionForUser_transactionExists_returnsMappedTransaction() {
        User user = user(10L);
        Transaction transaction = new Transaction();
        TransactionDto expectedResult = mock(TransactionDto.class);

        when(transactionRepository.findByIdForUser(15L, user))
                .thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(transaction))
                .thenReturn(expectedResult);

        TransactionDto result =
                sut.getTransactionForUser(15L, user);

        assertThat(result).isSameAs(expectedResult);

        verify(transactionRepository)
                .findByIdForUser(15L, user);
        verify(transactionMapper).toDto(transaction);
    }

    @Test
    void getTransactionForUser_transactionNotFound_throwsException() {
        User user = user(10L);

        when(transactionRepository.findByIdForUser(15L, user))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sut.getTransactionForUser(15L, user)
        ).isInstanceOf(TransactionNotFoundException.class);

        verify(transactionRepository)
                .findByIdForUser(15L, user);
        verifyNoInteractions(transactionMapper);
    }

    @Test
    void performTransaction_validRequest_transfersMoneyAndSavesTransaction() {
        User user = user(10L);
        TransactionRequest request =
                transactionRequest(1L, 2L, "40.00");

        Card fromCard = card(
                1L,
                user,
                Card.CardStatus.ACTIVE,
                "100.00"
        );
        Card toCard = card(
                2L,
                user,
                Card.CardStatus.ACTIVE,
                "20.00"
        );

        TransactionDto expectedResult = mock(TransactionDto.class);

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L))
                .thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    transaction.setId(15L);
                    return transaction;
                });
        when(transactionMapper.toDto(any(Transaction.class)))
                .thenReturn(expectedResult);

        TransactionDto result =
                sut.performTransaction(request, user);

        assertThat(result).isSameAs(expectedResult);
        assertThat(fromCard.getBalance())
                .isEqualByComparingTo("60.00");
        assertThat(toCard.getBalance())
                .isEqualByComparingTo("60.00");

        verify(cardRepository).save(fromCard);
        verify(cardRepository).save(toCard);

        ArgumentCaptor<Transaction> transactionCaptor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository)
                .save(transactionCaptor.capture());

        Transaction savedTransaction =
                transactionCaptor.getValue();

        assertThat(savedTransaction.getAmount())
                .isEqualByComparingTo("40.00");
        assertThat(savedTransaction.getOwner())
                .isSameAs(user);
        assertThat(savedTransaction.getFromCard())
                .isSameAs(fromCard);
        assertThat(savedTransaction.getToCard())
                .isSameAs(toCard);

        verify(transactionMapper).toDto(savedTransaction);
    }

    @Test
    void performTransaction_sameCards_throwsException() {
        User user = user(10L);
        TransactionRequest request =
                transactionRequest(1L, 1L, "40.00");

        assertThatThrownBy(() ->
                sut.performTransaction(request, user)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Выберите разные карты");

        verifyNoInteractions(
                cardRepository,
                transactionRepository,
                transactionMapper
        );
    }

    @Test
    void performTransaction_fromCardNotFound_throwsException() {
        User user = user(10L);
        TransactionRequest request =
                transactionRequest(1L, 2L, "40.00");

        when(cardRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sut.performTransaction(request, user)
        ).isInstanceOf(CardNotFoundException.class);

        verify(cardRepository).findById(1L);
        verify(cardRepository, never()).findById(2L);
        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(
                transactionRepository,
                transactionMapper
        );
    }

    @Test
    void performTransaction_toCardNotFound_throwsException() {
        User user = user(10L);
        TransactionRequest request =
                transactionRequest(1L, 2L, "40.00");

        Card fromCard = card(
                1L,
                user,
                Card.CardStatus.ACTIVE,
                "100.00"
        );

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sut.performTransaction(request, user)
        ).isInstanceOf(CardNotFoundException.class);

        verify(cardRepository).findById(1L);
        verify(cardRepository).findById(2L);
        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(
                transactionRepository,
                transactionMapper
        );
    }

    @Test
    void performTransaction_fromCardBelongsToAnotherUser_throwsException() {
        User user = user(10L);
        User anotherUser = user(20L);

        TransactionRequest request =
                transactionRequest(1L, 2L, "40.00");

        Card fromCard = card(
                1L,
                anotherUser,
                Card.CardStatus.ACTIVE,
                "100.00"
        );
        Card toCard = card(
                2L,
                user,
                Card.CardStatus.ACTIVE,
                "20.00"
        );

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L))
                .thenReturn(Optional.of(toCard));

        assertThatThrownBy(() ->
                sut.performTransaction(request, user)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Вы не владелец карты");

        assertThat(fromCard.getBalance())
                .isEqualByComparingTo("100.00");
        assertThat(toCard.getBalance())
                .isEqualByComparingTo("20.00");

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(
                transactionRepository,
                transactionMapper
        );
    }

    @Test
    void performTransaction_toCardBelongsToAnotherUser_throwsException() {
        User user = user(10L);
        User anotherUser = user(20L);

        TransactionRequest request =
                transactionRequest(1L, 2L, "40.00");

        Card fromCard = card(
                1L,
                user,
                Card.CardStatus.ACTIVE,
                "100.00"
        );
        Card toCard = card(
                2L,
                anotherUser,
                Card.CardStatus.ACTIVE,
                "20.00"
        );

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L))
                .thenReturn(Optional.of(toCard));

        assertThatThrownBy(() ->
                sut.performTransaction(request, user)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Вы не владелец карты");

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(
                transactionRepository,
                transactionMapper
        );
    }

    @Test
    void performTransaction_fromCardNotActive_throwsException() {
        User user = user(10L);
        TransactionRequest request =
                transactionRequest(1L, 2L, "40.00");

        Card fromCard = card(
                1L,
                user,
                Card.CardStatus.BLOCKED,
                "100.00"
        );
        Card toCard = card(
                2L,
                user,
                Card.CardStatus.ACTIVE,
                "20.00"
        );

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L))
                .thenReturn(Optional.of(toCard));

        assertThatThrownBy(() ->
                sut.performTransaction(request, user)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Карты должны быть активными ");

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(
                transactionRepository,
                transactionMapper
        );
    }

    @Test
    void performTransaction_toCardNotActive_throwsException() {
        User user = user(10L);
        TransactionRequest request =
                transactionRequest(1L, 2L, "40.00");

        Card fromCard = card(
                1L,
                user,
                Card.CardStatus.ACTIVE,
                "100.00"
        );
        Card toCard = card(
                2L,
                user,
                Card.CardStatus.BLOCKED,
                "20.00"
        );

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L))
                .thenReturn(Optional.of(toCard));

        assertThatThrownBy(() ->
                sut.performTransaction(request, user)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Карты должны быть активными ");

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(
                transactionRepository,
                transactionMapper
        );
    }

    @Test
    void performTransaction_notEnoughMoney_throwsException() {
        User user = user(10L);
        TransactionRequest request =
                transactionRequest(1L, 2L, "100.01");

        Card fromCard = card(
                1L,
                user,
                Card.CardStatus.ACTIVE,
                "100.00"
        );
        Card toCard = card(
                2L,
                user,
                Card.CardStatus.ACTIVE,
                "20.00"
        );

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L))
                .thenReturn(Optional.of(toCard));

        assertThatThrownBy(() ->
                sut.performTransaction(request, user)
        ).isInstanceOf(NotEnoughMoneyException.class);

        assertThat(fromCard.getBalance())
                .isEqualByComparingTo("100.00");
        assertThat(toCard.getBalance())
                .isEqualByComparingTo("20.00");

        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(
                transactionRepository,
                transactionMapper
        );
    }

    @Test
    void performTransaction_fullBalance_setsSenderBalanceToZero() {
        User user = user(10L);
        TransactionRequest request =
                transactionRequest(1L, 2L, "100.00");

        Card fromCard = card(
                1L,
                user,
                Card.CardStatus.ACTIVE,
                "100.00"
        );
        Card toCard = card(
                2L,
                user,
                Card.CardStatus.ACTIVE,
                "20.00"
        );

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L))
                .thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction transaction = invocation.getArgument(0);
                    transaction.setId(15L);
                    return transaction;
                });
        when(transactionMapper.toDto(any(Transaction.class)))
                .thenReturn(mock(TransactionDto.class));

        sut.performTransaction(request, user);

        assertThat(fromCard.getBalance())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(toCard.getBalance())
                .isEqualByComparingTo("120.00");

        verify(cardRepository).save(fromCard);
        verify(cardRepository).save(toCard);
        verify(transactionRepository).save(any(Transaction.class));
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Card card(
            Long id,
            User owner,
            Card.CardStatus status,
            String balance
    ) {
        Card card = new Card();
        card.setId(id);
        card.setOwner(owner);
        card.setStatus(status);
        card.setBalance(new BigDecimal(balance));
        return card;
    }

    private TransactionRequest transactionRequest(
            Long fromCardId,
            Long toCardId,
            String amount
    ) {
        TransactionRequest request =
                mock(TransactionRequest.class);

        when(request.fromCardId()).thenReturn(fromCardId);
        when(request.toCardId()).thenReturn(toCardId);
        when(request.amount()).thenReturn(new BigDecimal(amount));

        return request;
    }
}

