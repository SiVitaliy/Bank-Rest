package com.example.bankcards.controllerTests.TransactionCintroller;

import com.example.bankcards.controller.TransactionController;
import com.example.bankcards.dto.request.TransactionRequest;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.TransactionDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.TransactionService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

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
    void findAllTransactionsForUser_validRequest_passesDefaultPageableToService() throws Exception {
        User user = authenticatedUser();
        PageResponse<TransactionDto> response = mock(PageResponse.class);

        when(transactionService.findAllByUser(any(Pageable.class), eq(user)))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions")
                        .with(authentication(userAuthentication(user))))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(transactionService).findAllByUser(
                pageableCaptor.capture(),
                eq(user)
        );

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("creationTime"))
                .isNotNull()
                .satisfies(order -> assertThat(order.isAscending()).isTrue());
    }

    @Test
    void findAllTransactionsForUser_withPageableParameters_passesPageableToService()
            throws Exception {
        User user = authenticatedUser();
        PageResponse<TransactionDto> response = mock(PageResponse.class);

        when(transactionService.findAllByUser(any(Pageable.class), eq(user)))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions")
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "creationTime,desc")
                        .with(authentication(userAuthentication(user))))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(transactionService).findAllByUser(
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
    void findTransactionForUser_existingTransaction_returnsOk() throws Exception {
        long transactionId = 15L;
        User user = authenticatedUser();
        TransactionDto response = mock(TransactionDto.class);

        when(transactionService.getTransactionForUser(transactionId, user))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions/{id}", transactionId)
                        .with(authentication(userAuthentication(user))))
                .andExpect(status().isOk());

        verify(transactionService).getTransactionForUser(transactionId, user);
    }

    @Test
    void performTransaction_validRequest_returnsCreated() throws Exception {
        long transactionId = 15L;
        User user = authenticatedUser();
        TransactionDto response = mock(TransactionDto.class);

        when(response.id()).thenReturn(transactionId);
        when(transactionService.performTransaction(
                any(TransactionRequest.class),
                eq(user)
        )).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromCardId": 3,
                                  "toCardId": 4,
                                  "amount": 100.00
                                }
                                """)
                        .with(authentication(userAuthentication(user)))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/transactions/15"
                ));

        ArgumentCaptor<TransactionRequest> requestCaptor =
                ArgumentCaptor.forClass(TransactionRequest.class);

        verify(transactionService).performTransaction(
                requestCaptor.capture(),
                eq(user)
        );

        TransactionRequest request = requestCaptor.getValue();

        assertThat(request).isNotNull();
    }

    @Test
    void performTransaction_invalidJson_returnsBadRequest() throws Exception {
        User user = authenticatedUser();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{")
                        .with(authentication(userAuthentication(user)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void findAllTransactionsForUser_withoutAuthentication_returnsUnauthorized()
            throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(transactionService);
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
