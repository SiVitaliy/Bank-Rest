
package com.example.bankcards.serviceTests.CardRequestService;

import com.example.bankcards.dto.filter.CardRequestFilter;
import com.example.bankcards.dto.request.CardActionRequest;
import com.example.bankcards.dto.request.ProcessCardRequest;
import com.example.bankcards.dto.response.CardRequestDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.RequestNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardRequestRepository;
import com.example.bankcards.service.CardRequestService;
import com.example.bankcards.specification.CardRequestSpecificationBuilder;
import com.example.bankcards.util.mapper.CardRequestMapper;
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
class CardRequestServiceTest {

    @Mock
    private CardRequestRepository cardRequestRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardRequestMapper cardRequestMapper;

    @Mock
    private CardRequestSpecificationBuilder cardRequestSpecificationBuilder;

    @InjectMocks
    private CardRequestService sut;

    @Test
    void findAllRequests_validFilter_returnsMappedPage() {
        CardRequestFilter filter = mock(CardRequestFilter.class);
        Pageable pageable = PageRequest.of(0, 5);
        Specification<CardRequest> specification = mock(Specification.class);

        CardRequest cardRequest = new CardRequest();
        CardRequestDto cardRequestDto = mock(CardRequestDto.class);

        var page = new PageImpl<>(
                List.of(cardRequest),
                pageable,
                1
        );

        when(cardRequestSpecificationBuilder.buildSpec(filter))
                .thenReturn(specification);
        when(cardRequestRepository.findAll(specification, pageable))
                .thenReturn(page);
        when(cardRequestMapper.toDto(cardRequest))
                .thenReturn(cardRequestDto);

        PageResponse<CardRequestDto> result =
                sut.findAllRequests(filter, pageable);

        assertThat(result).isNotNull();

        verify(cardRequestSpecificationBuilder).buildSpec(filter);
        verify(cardRequestRepository).findAll(specification, pageable);
        verify(cardRequestMapper).toDto(cardRequest);
    }

    @Test
    void save_issueRequest_savesRequestWithoutCard() {
        User user = userWithId(10L);

        CardActionRequest request = mock(CardActionRequest.class);
        when(request.requestType()).thenReturn("ISSUE");

        CardRequestDto expectedResult = mock(CardRequestDto.class);

        when(cardRequestRepository.save(any(CardRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cardRequestMapper.toDto(any(CardRequest.class)))
                .thenReturn(expectedResult);

        CardRequestDto result = sut.save(request, user);

        assertThat(result).isSameAs(expectedResult);

        ArgumentCaptor<CardRequest> captor =
                ArgumentCaptor.forClass(CardRequest.class);

        verify(cardRequestRepository).save(captor.capture());

        CardRequest savedRequest = captor.getValue();

        assertThat(savedRequest.getRequester()).isSameAs(user);
        assertThat(savedRequest.getRequestType())
                .isEqualTo(CardRequest.RequestType.ISSUE);
        assertThat(savedRequest.getCard()).isNull();

        verifyNoInteractions(cardRepository);
    }

    @Test
    void save_blockRequest_savesRequestWithOwnedCard() {
        User user = userWithId(10L);
        Card card = new Card();

        CardActionRequest request = mock(CardActionRequest.class);
        when(request.requestType()).thenReturn("BLOCK");
        when(request.cardId()).thenReturn(3L);

        CardRequestDto expectedResult = mock(CardRequestDto.class);

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.of(card));
        when(cardRequestRepository.save(any(CardRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cardRequestMapper.toDto(any(CardRequest.class)))
                .thenReturn(expectedResult);

        CardRequestDto result = sut.save(request, user);

        assertThat(result).isSameAs(expectedResult);

        ArgumentCaptor<CardRequest> captor =
                ArgumentCaptor.forClass(CardRequest.class);

        verify(cardRequestRepository).save(captor.capture());

        CardRequest savedRequest = captor.getValue();

        assertThat(savedRequest.getRequester()).isSameAs(user);
        assertThat(savedRequest.getRequestType())
                .isEqualTo(CardRequest.RequestType.BLOCK);
        assertThat(savedRequest.getCard()).isSameAs(card);

        verify(cardRepository).findByIdAndOwnerId(3L, 10L);
    }

    @Test
    void save_nonIssueRequestWithoutCardId_throwsException() {
        User user = mock(User.class);

        CardActionRequest request = mock(CardActionRequest.class);
        when(request.requestType()).thenReturn("BLOCK");
        when(request.cardId()).thenReturn(null);

        assertThatThrownBy(() -> sut.save(request, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Для операции с существующей картой необходимо указать id"
                );

        verifyNoInteractions(cardRepository);
        verify(cardRequestRepository, never())
                .save(any(CardRequest.class));
        verifyNoInteractions(cardRequestMapper);
    }

    @Test
    void save_cardDoesNotBelongToUser_throwsCardNotFoundException() {
        User user = userWithId(10L);

        CardActionRequest request = mock(CardActionRequest.class);
        when(request.requestType()).thenReturn("DELETE");
        when(request.cardId()).thenReturn(3L);

        when(cardRepository.findByIdAndOwnerId(3L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.save(request, user))
                .isInstanceOf(CardNotFoundException.class);

        verify(cardRepository).findByIdAndOwnerId(3L, 10L);
        verify(cardRequestRepository, never())
                .save(any(CardRequest.class));
        verifyNoInteractions(cardRequestMapper);
    }

    @Test
    void save_invalidRequestType_throwsException() {
        User user = mock(User.class);
        CardActionRequest request = mock(CardActionRequest.class);

        when(request.requestType()).thenReturn("UNKNOWN");

        assertThatThrownBy(() -> sut.save(request, user))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(
                cardRepository,
                cardRequestRepository,
                cardRequestMapper
        );
    }

    @Test
    void process_requestNotFound_throwsRequestNotFoundException() {
        User admin = mock(User.class);
        ProcessCardRequest request = mock(ProcessCardRequest.class);

        when(cardRequestRepository.findById(15L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.process(15L, request, admin))
                .isInstanceOf(RequestNotFoundException.class);

        verify(cardRequestRepository).findById(15L);
        verify(cardRequestRepository, never())
                .save(any(CardRequest.class));
        verifyNoInteractions(cardRepository, cardRequestMapper);
    }

    @Test
    void process_alreadyProcessedRequest_throwsException() {
        User admin = mock(User.class);
        ProcessCardRequest request = mock(ProcessCardRequest.class);

        CardRequest cardRequest = new CardRequest();
        cardRequest.setStatus(CardRequest.RequestStatus.APPROVED);

        when(cardRequestRepository.findById(15L))
                .thenReturn(Optional.of(cardRequest));

        assertThatThrownBy(() -> sut.process(15L, request, admin))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Запрос уже обработан");

        verify(cardRequestRepository, never())
                .save(any(CardRequest.class));
        verifyNoInteractions(cardRepository, cardRequestMapper);
    }

    @Test
    void process_rejectRequest_setsRejectedStatusAndAdmin() {
        User admin = userWithId(100L);
        ProcessCardRequest request = processRequest("REJECT");
        CardRequestDto expectedResult = mock(CardRequestDto.class);

        CardRequest cardRequest = pendingRequest(
                CardRequest.RequestType.BLOCK,
                new Card()
        );

        when(cardRequestRepository.findById(15L))
                .thenReturn(Optional.of(cardRequest));
        when(cardRequestRepository.save(cardRequest))
                .thenReturn(cardRequest);
        when(cardRequestMapper.toDto(cardRequest))
                .thenReturn(expectedResult);

        CardRequestDto result = sut.process(15L, request, admin);

        assertThat(result).isSameAs(expectedResult);
        assertThat(cardRequest.getStatus())
                .isEqualTo(CardRequest.RequestStatus.REJECTED);
        assertThat(cardRequest.getProcessedBy()).isSameAs(admin);

        verify(cardRequestRepository).save(cardRequest);
        verifyNoInteractions(cardRepository);
    }

    @Test
    void process_approveBlockRequest_blocksCard() {
        User admin = userWithId(100L);
        ProcessCardRequest request = processRequest("APPROVE");
        CardRequestDto expectedResult = mock(CardRequestDto.class);

        Card card = new Card();
        card.setStatus(Card.CardStatus.ACTIVE);

        CardRequest cardRequest = pendingRequest(
                CardRequest.RequestType.BLOCK,
                card
        );

        when(cardRequestRepository.findById(15L))
                .thenReturn(Optional.of(cardRequest));
        when(cardRequestRepository.save(cardRequest))
                .thenReturn(cardRequest);
        when(cardRequestMapper.toDto(cardRequest))
                .thenReturn(expectedResult);

        CardRequestDto result = sut.process(15L, request, admin);

        assertThat(result).isSameAs(expectedResult);
        assertThat(cardRequest.getStatus())
                .isEqualTo(CardRequest.RequestStatus.APPROVED);
        assertThat(cardRequest.getProcessedBy()).isSameAs(admin);
        assertThat(card.getStatus())
                .isEqualTo(Card.CardStatus.BLOCKED);

        verify(cardRepository).save(card);
        verify(cardRequestRepository).save(cardRequest);
    }

    @Test
    void process_approveActivateRequest_activatesCard() {
        User admin = userWithId(100L);
        ProcessCardRequest request = processRequest("APPROVE");
        CardRequestDto expectedResult = mock(CardRequestDto.class);

        Card card = new Card();
        card.setStatus(Card.CardStatus.BLOCKED);

        CardRequest cardRequest = pendingRequest(
                CardRequest.RequestType.ACTIVATE,
                card
        );

        when(cardRequestRepository.findById(15L))
                .thenReturn(Optional.of(cardRequest));
        when(cardRequestRepository.save(cardRequest))
                .thenReturn(cardRequest);
        when(cardRequestMapper.toDto(cardRequest))
                .thenReturn(expectedResult);

        CardRequestDto result = sut.process(15L, request, admin);

        assertThat(result).isSameAs(expectedResult);
        assertThat(card.getStatus())
                .isEqualTo(Card.CardStatus.ACTIVE);
        assertThat(cardRequest.getStatus())
                .isEqualTo(CardRequest.RequestStatus.APPROVED);
        assertThat(cardRequest.getProcessedBy()).isSameAs(admin);

        verify(cardRepository).save(card);
        verify(cardRequestRepository).save(cardRequest);
    }



    @Test
    void process_approveDeleteRequest_deletesCardAndClearsReference() {
        User admin = userWithId(100L);
        ProcessCardRequest request = processRequest("APPROVE");
        CardRequestDto expectedResult = mock(CardRequestDto.class);

        Card card = new Card();

        CardRequest cardRequest = pendingRequest(
                CardRequest.RequestType.DELETE,
                card
        );

        when(cardRequestRepository.findById(15L))
                .thenReturn(Optional.of(cardRequest));
        when(cardRequestRepository.save(cardRequest))
                .thenReturn(cardRequest);
        when(cardRequestMapper.toDto(cardRequest))
                .thenReturn(expectedResult);

        CardRequestDto result = sut.process(15L, request, admin);

        assertThat(result).isSameAs(expectedResult);
        assertThat(cardRequest.getCard()).isNull();
        assertThat(cardRequest.getStatus())
                .isEqualTo(CardRequest.RequestStatus.APPROVED);
        assertThat(cardRequest.getProcessedBy()).isSameAs(admin);

        verify(cardRepository).delete(card);
        verify(cardRepository, never()).save(any(Card.class));
        verify(cardRequestRepository).save(cardRequest);
    }

    @Test
    void process_approveIssueRequest_doesNotChangeCards() {
        User admin = userWithId(100L);
        ProcessCardRequest request = processRequest("APPROVE");
        CardRequestDto expectedResult = mock(CardRequestDto.class);

        CardRequest cardRequest = pendingRequest(
                CardRequest.RequestType.ISSUE,
                null
        );

        when(cardRequestRepository.findById(15L))
                .thenReturn(Optional.of(cardRequest));
        when(cardRequestRepository.save(cardRequest))
                .thenReturn(cardRequest);
        when(cardRequestMapper.toDto(cardRequest))
                .thenReturn(expectedResult);

        CardRequestDto result = sut.process(15L, request, admin);

        assertThat(result).isSameAs(expectedResult);
        assertThat(cardRequest.getStatus())
                .isEqualTo(CardRequest.RequestStatus.APPROVED);
        assertThat(cardRequest.getProcessedBy()).isSameAs(admin);

        verifyNoInteractions(cardRepository);
        verify(cardRequestRepository).save(cardRequest);
    }

    @Test
    void process_invalidAction_throwsException() {
        User admin = mock(User.class);
        ProcessCardRequest request = processRequest("UNKNOWN");

        CardRequest cardRequest = pendingRequest(
                CardRequest.RequestType.BLOCK,
                new Card()
        );

        when(cardRequestRepository.findById(15L))
                .thenReturn(Optional.of(cardRequest));

        assertThatThrownBy(() -> sut.process(15L, request, admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Недопустимое действие: UNKNOWN");

        verify(cardRequestRepository, never())
                .save(any(CardRequest.class));
        verifyNoInteractions(cardRepository, cardRequestMapper);
    }

    private User userWithId(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private ProcessCardRequest processRequest(String action) {
        ProcessCardRequest request = mock(ProcessCardRequest.class);
        when(request.action()).thenReturn(action);
        return request;
    }

    private CardRequest pendingRequest(
            CardRequest.RequestType requestType,
            Card card
    ) {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setStatus(CardRequest.RequestStatus.PENDING);
        cardRequest.setRequestType(requestType);
        cardRequest.setCard(card);
        return cardRequest;
    }
}

