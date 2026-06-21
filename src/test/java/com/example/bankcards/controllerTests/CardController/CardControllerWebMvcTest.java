package com.example.bankcards.controllerTests.CardController;

import com.example.bankcards.controller.CardController;
import com.example.bankcards.dto.filter.CardFilter;
import com.example.bankcards.dto.request.ChangeBalanceRequest;
import com.example.bankcards.dto.response.CardDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.CardService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
class CardControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(
                    invocation.getArgument(0),
                    invocation.getArgument(1)
            );
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void findAllCardsForUser_validRequest_passesDefaultPageableToService() throws Exception {
        User user = authenticatedUser();
        PageResponse<CardDto> response = mock(PageResponse.class);

        when(cardService.findAllCardsForUser(
                any(CardFilter.class),
                any(Pageable.class),
                eq(user)
        )).thenReturn(response);

        mockMvc.perform(get("/api/cards")
                        .with(authentication(userAuthentication(user))))
                .andExpect(status().isOk());

        ArgumentCaptor<CardFilter> filterCaptor =
                ArgumentCaptor.forClass(CardFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(cardService).findAllCardsForUser(
                filterCaptor.capture(),
                pageableCaptor.capture(),
                eq(user)
        );

        Pageable pageable = pageableCaptor.getValue();

        assertThat(filterCaptor.getValue()).isNotNull();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("creationTime"))
                .isNotNull()
                .satisfies(order -> assertThat(order.isAscending()).isTrue());
    }

    @Test
    void findAllCardsForUser_withPageableParameters_passesPageableToService() throws Exception {
        User user = authenticatedUser();
        PageResponse<CardDto> response = mock(PageResponse.class);

        when(cardService.findAllCardsForUser(
                any(CardFilter.class),
                any(Pageable.class),
                eq(user)
        )).thenReturn(response);

        mockMvc.perform(get("/api/cards")
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "creationTime,desc")
                        .with(authentication(userAuthentication(user))))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(cardService).findAllCardsForUser(
                any(CardFilter.class),
                pageableCaptor.capture(),
                eq(user)
        );

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("creationTime"))
                .isNotNull()
                .satisfies(order -> assertThat(order.isDescending()).isTrue());
    }

    @Test
    void findById_existingCard_returnsOk() throws Exception {
        long cardId = 3L;
        User user = authenticatedUser();
        CardDto response = mock(CardDto.class);

        when(cardService.findByIdForUser(cardId, user))
                .thenReturn(response);

        mockMvc.perform(get("/api/cards/{id}", cardId)
                        .with(authentication(userAuthentication(user))))
                .andExpect(status().isOk());

        verify(cardService).findByIdForUser(cardId, user);
    }

    @Test
    void addBalance_addOperation_callsAddBalance() throws Exception {
        long cardId = 3L;
        User user = authenticatedUser();
        CardDto response = mock(CardDto.class);

        when(cardService.addBalance(
                eq(cardId),
                any(ChangeBalanceRequest.class),
                eq(user)
        )).thenReturn(response);

        mockMvc.perform(patch("/api/cards/{id}/balance", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100.00,
                                  "operation": "ADD"
                                }
                                """)
                        .with(authentication(userAuthentication(user)))
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<ChangeBalanceRequest> requestCaptor =
                ArgumentCaptor.forClass(ChangeBalanceRequest.class);

        verify(cardService).addBalance(
                eq(cardId),
                requestCaptor.capture(),
                eq(user)
        );

        assertThat(requestCaptor.getValue().operation()).isEqualTo("ADD");

        verify(cardService, never()).subtractBalance(
                any(),
                any(ChangeBalanceRequest.class),
                any(User.class)
        );
    }

    @Test
    void addBalance_subtractOperation_callsSubtractBalance() throws Exception {
        long cardId = 3L;
        User user = authenticatedUser();
        CardDto response = mock(CardDto.class);

        when(cardService.subtractBalance(
                eq(cardId),
                any(ChangeBalanceRequest.class),
                eq(user)
        )).thenReturn(response);

        mockMvc.perform(patch("/api/cards/{id}/balance", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 50.00,
                                  "operation": "SUBTRACT"
                                }
                                """)
                        .with(authentication(userAuthentication(user)))
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<ChangeBalanceRequest> requestCaptor =
                ArgumentCaptor.forClass(ChangeBalanceRequest.class);

        verify(cardService).subtractBalance(
                eq(cardId),
                requestCaptor.capture(),
                eq(user)
        );

        assertThat(requestCaptor.getValue().operation()).isEqualTo("SUBTRACT");

        verify(cardService, never()).addBalance(
                any(),
                any(ChangeBalanceRequest.class),
                any(User.class)
        );
    }

    @Test
    void addBalance_invalidRequest_returnsBadRequest() throws Exception {
        User user = authenticatedUser();

        mockMvc.perform(patch("/api/cards/{id}/balance", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": null,
                                  "operation": ""
                                }
                                """)
                        .with(authentication(userAuthentication(user)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).addBalance(
                any(),
                any(ChangeBalanceRequest.class),
                any(User.class)
        );
        verify(cardService, never()).subtractBalance(
                any(),
                any(ChangeBalanceRequest.class),
                any(User.class)
        );
    }

    private User authenticatedUser() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(10L);
        return user;
    }

    private UsernamePasswordAuthenticationToken userAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}

