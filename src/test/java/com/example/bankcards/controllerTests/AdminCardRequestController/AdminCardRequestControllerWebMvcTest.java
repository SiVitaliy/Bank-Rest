package com.example.bankcards.controllerTests.AdminCardRequestController;


import com.example.bankcards.controller.admin.AdminCardRequestController;
import com.example.bankcards.dto.filter.CardRequestFilter;
import com.example.bankcards.dto.response.CardRequestDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.CardRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCardRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCardRequestControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardRequestService cardRequestService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void findAllRequests_returnsOk() throws Exception {
        PageResponse<CardRequestDto> response = mock(PageResponse.class);

        when(cardRequestService.findAllRequests(
                any(CardRequestFilter.class),
                any()
        )).thenReturn(response);

        mockMvc.perform(get("/admin/card-requests")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "creationTime,desc"))
                .andExpect(status().isOk());

        verify(cardRequestService).findAllRequests(
                any(CardRequestFilter.class),
                argThat(pageable ->
                        pageable.getPageNumber() == 0
                                && pageable.getPageSize() == 5
                                && pageable.getSort()
                                .getOrderFor("creationTime") != null
                                && pageable.getSort()
                                .getOrderFor("creationTime")
                                .isDescending()
                )
        );
    }

    @Test
    void findAllRequests_withFilters_passesFilterToService() throws Exception {
        PageResponse<CardRequestDto> response = mock(PageResponse.class);

        when(cardRequestService.findAllRequests(
                any(CardRequestFilter.class),
                any()
        )).thenReturn(response);

        mockMvc.perform(get("/admin/card-requests")
                        .param("requestType", "BLOCK")
                        .param("status", "PENDING"))
                .andExpect(status().isOk());

        verify(cardRequestService).findAllRequests(
                argThat(filter ->
                        "BLOCK".equals(filter.requestType())
                                && "PENDING".equals(filter.status())
                ),
                any()
        );
    }


    @Test
    void processRequest_invalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/card-requests/{id}", "incorrect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPROVED",
                                  "comment": "Заявка одобрена"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cardRequestService);
    }

    @Test
    void processRequest_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/card-requests/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cardRequestService);
    }

    @Test
    void processRequest_invalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/admin/card-requests/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "UNKNOWN"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cardRequestService);
    }
}