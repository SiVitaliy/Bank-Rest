package com.example.bankcards.controllerTests.AdminCardRequestController;

import com.example.bankcards.controller.admin.AdminCardRequestController;
import com.example.bankcards.dto.filter.CardRequestFilter;
import com.example.bankcards.dto.request.ProcessCardRequest;
import com.example.bankcards.dto.response.CardRequestDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardRequestControllerTest {

    @Mock
    private CardRequestService cardRequestService;

    @InjectMocks
    private AdminCardRequestController controller;

    @Test
    void findAllRequests_returnsPageResponse() {
        CardRequestFilter filter = mock(CardRequestFilter.class);
        Pageable pageable = PageRequest.of(0, 5);
        PageResponse<CardRequestDto> expectedResponse = mock(PageResponse.class);

        when(cardRequestService.findAllRequests(filter, pageable))
                .thenReturn(expectedResponse);

        ResponseEntity<PageResponse<CardRequestDto>> response =
                controller.findAllRequests(filter, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedResponse);

        verify(cardRequestService).findAllRequests(filter, pageable);
        verifyNoMoreInteractions(cardRequestService);
    }

    @Test
    void processRequest_validRequest_returnsProcessedRequest() {
        Long requestId = 5L;
        ProcessCardRequest request = mock(ProcessCardRequest.class);
        User admin = mock(User.class);
        CardRequestDto expectedResponse = mock(CardRequestDto.class);

        when(cardRequestService.process(requestId, request, admin))
                .thenReturn(expectedResponse);

        ResponseEntity<CardRequestDto> response =
                controller.processRequest(requestId, request, admin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expectedResponse);

        verify(cardRequestService).process(requestId, request, admin);
        verifyNoMoreInteractions(cardRequestService);
    }
}