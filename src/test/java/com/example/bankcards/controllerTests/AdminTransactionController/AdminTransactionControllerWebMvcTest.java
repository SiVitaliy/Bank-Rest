package com.example.bankcards.controllerTests.AdminTransactionController;

import com.example.bankcards.controller.admin.AdminTransactionController;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.TransactionDto;
import com.example.bankcards.service.JpaUserDetailsService;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.util.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTransactionController.class)
class AdminTransactionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    @MockitoBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void findAllUserTransaction_validRequest_returnsOk() throws Exception {
        long userId = 3L;
        PageResponse<TransactionDto> response = mock(PageResponse.class);

        when(transactionService.findAllByUserId(any(Pageable.class), eq(userId)))
                .thenReturn(response);

        mockMvc.perform(get("/admin/users/{userId}/transactions", userId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(transactionService).findAllByUserId(
                pageableCaptor.capture(),
                eq(userId)
        );

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("creationTime"))
                .isNotNull()
                .satisfies(order -> assertThat(order.isAscending()).isTrue());
    }

    @Test
    void findAllUserTransaction_withPageableParameters_passesPageableToService() throws Exception {
        long userId = 3L;
        PageResponse<TransactionDto> response = mock(PageResponse.class);

        when(transactionService.findAllByUserId(any(Pageable.class), eq(userId)))
                .thenReturn(response);

        mockMvc.perform(get("/admin/users/{userId}/transactions", userId)
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "creationTime,desc")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(transactionService).findAllByUserId(
                pageableCaptor.capture(),
                eq(userId)
        );

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("creationTime"))
                .isNotNull()
                .satisfies(order -> assertThat(order.isDescending()).isTrue());
    }

    @Test
    void findTransaction_existingTransaction_returnsOk() throws Exception {
        long transactionId = 15L;
        TransactionDto response = mock(TransactionDto.class);

        when(transactionService.getTransaction(transactionId))
                .thenReturn(response);

        mockMvc.perform(get("/admin/transactions/{id}", transactionId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        verify(transactionService).getTransaction(transactionId);
    }

    @Test
    void findAllUserTransaction_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/users/{userId}/transactions", 3L))
                .andExpect(status().isUnauthorized());
    }
}