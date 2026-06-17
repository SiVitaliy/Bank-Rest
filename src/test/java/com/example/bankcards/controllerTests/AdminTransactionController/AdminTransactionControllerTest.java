package com.example.bankcards.controllerTests.AdminTransactionController;

import com.example.bankcards.controller.admin.AdminTransactionController;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.TransactionDto;
import com.example.bankcards.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private AdminTransactionController controller;

    @Test
    void findAllUserTransaction_validRequest_returnsResponseFromService() {
        long userId = 3L;
        Pageable pageable = PageRequest.of(
                0,
                5,
                Sort.by(Sort.Direction.ASC, "creationTime")
        );
        PageResponse<TransactionDto> expectedResponse = mock(PageResponse.class);

        when(transactionService.findAllByUserId(pageable, userId))
                .thenReturn(expectedResponse);

        ResponseEntity<PageResponse<TransactionDto>> actualResponse =
                controller.findAllUserTransaction(userId, pageable);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isSameAs(expectedResponse);

        verify(transactionService).findAllByUserId(pageable, userId);
    }

    @Test
    void findTransaction_existingTransaction_returnsResponseFromService() {
        long transactionId = 15L;
        TransactionDto expectedResponse = mock(TransactionDto.class);

        when(transactionService.getTransaction(transactionId))
                .thenReturn(expectedResponse);

        ResponseEntity<TransactionDto> actualResponse =
                controller.findTransaction(transactionId);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isSameAs(expectedResponse);

        verify(transactionService).getTransaction(transactionId);
    }
}