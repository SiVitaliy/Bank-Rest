package com.example.bankcards.controllerTests.AdminCardController;

import com.example.bankcards.controller.admin.AdminCardController;
import com.example.bankcards.dto.filter.CardFilter;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateCardExpirationDateRequest;
import com.example.bankcards.dto.response.CardDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private AdminCardController controller;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void findAllCards_returnsPageResponse() {
        CardFilter filter = mock(CardFilter.class);
        Pageable pageable = PageRequest.of(0, 5);
        PageResponse<CardDto> expectedResponse = mock(PageResponse.class);

        when(cardService.findAllCards(filter, pageable))
                .thenReturn(expectedResponse);

        ResponseEntity<PageResponse<CardDto>> response =
                controller.findAllCards(filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedResponse);

        verify(cardService).findAllCards(filter, pageable);
        verifyNoMoreInteractions(cardService);
    }

    @Test
    void findCard_existingCard_returnsCard() {
        Long cardId = 10L;
        CardDto expectedResponse = mock(CardDto.class);

        when(cardService.findById(cardId))
                .thenReturn(expectedResponse);

        ResponseEntity<CardDto> response = controller.findCard(cardId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedResponse);

        verify(cardService).findById(cardId);
        verifyNoMoreInteractions(cardService);
    }

    @Test
    void createCard_validRequest_returnsCreatedResponse() {
        Long userId = 3L;
        Long cardId = 10L;

        CreateCardRequest request = mock(CreateCardRequest.class);
        User admin = mock(User.class);
        CardDto expectedResponse = mock(CardDto.class);

        when(expectedResponse.id()).thenReturn(cardId);
        when(cardService.save(userId, request, admin))
                .thenReturn(expectedResponse);

        MockHttpServletRequest servletRequest =
                new MockHttpServletRequest("POST", "/admin/users/3/cards");

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(servletRequest)
        );

        ResponseEntity<CardDto> response =
                controller.createCard(userId, request, admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(expectedResponse);
        assertThat(response.getHeaders().getLocation())
                .hasToString("http://localhost/admin/users/3/cards/10");

        verify(cardService).save(userId, request, admin);
        verifyNoMoreInteractions(cardService);
    }

    @Test
    void setNewExpirationDate_validRequest_returnsUpdatedCard() {
        Long cardId = 10L;
        YearMonth expirationDate = YearMonth.of(2030, 12);

        UpdateCardExpirationDateRequest request =
                mock(UpdateCardExpirationDateRequest.class);
        User admin = mock(User.class);
        CardDto expectedResponse = mock(CardDto.class);

        when(request.expirationDate()).thenReturn(expirationDate);
        when(cardService.setNewExpirationDate(cardId, expirationDate, admin))
                .thenReturn(expectedResponse);

        ResponseEntity<CardDto> response =
                controller.setNewExpirationDate(cardId, request, admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedResponse);

        verify(cardService)
                .setNewExpirationDate(cardId, expirationDate, admin);
        verifyNoMoreInteractions(cardService);
    }
}