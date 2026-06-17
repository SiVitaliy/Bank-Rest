package com.example.bankcards.controllerTests.AdminCardController;

import com.example.bankcards.controller.admin.AdminCardController;
import com.example.bankcards.dto.filter.CardFilter;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.CardDto;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCardController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCardControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;
    @MockitoBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;


    @Test
    void findAllCards_returnsOk() throws Exception {
        PageResponse<CardDto> response = mock(PageResponse.class);

        when(cardService.findAllCards(
                any(CardFilter.class),
                any()
        )).thenReturn(response);

        mockMvc.perform(get("/admin/cards")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "creationTime,asc"))
                .andExpect(status().isOk());

        verify(cardService).findAllCards(
                any(CardFilter.class),
                argThat(pageable ->
                        pageable.getPageNumber() == 0
                                && pageable.getPageSize() == 5
                                && pageable.getSort()
                                .getOrderFor("creationTime") != null
                )
        );
    }

    @Test
    void findCard_existingCard_returnsOk() throws Exception {
        Long cardId = 10L;
        CardDto response = mock(CardDto.class);

        when(cardService.findById(cardId))
                .thenReturn(response);

        mockMvc.perform(get("/admin/cards/{id}", cardId))
                .andExpect(status().isOk());

        verify(cardService).findById(cardId);
    }

    @Test
    void findCard_invalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/admin/cards/{id}", "incorrect"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cardService);
    }

    @Test
    void createCard_validRequest_returnsCreated() throws Exception {
        Long userId = 3L;
        Long cardId = 10L;

        CardDto response = mock(CardDto.class);

        when(response.id()).thenReturn(cardId);
        when(cardService.save(
                eq(userId),
                any(CreateCardRequest.class),
                isNull()
        )).thenReturn(response);

        mockMvc.perform(post("/admin/users/{userId}/cards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "cardNumber": "1111222233334444",
                              "expirationDate": "12/29"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/admin/users/3/cards/10"
                ));

        verify(cardService).save(
                eq(userId),
                any(CreateCardRequest.class),
                isNull()
        );
    }

    @Test
    void createCard_invalidRequest_returnsBadRequest() throws Exception {
        User admin = mock(User.class);

        mockMvc.perform(post("/admin/users/{userId}/cards", 3L)
                        .with(authentication(adminAuthentication(admin)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "cardNumber": "",
                                  "expirationDate": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cardService);
    }

    @Test
    void setNewExpirationDate_validRequest_returnsOk() throws Exception {
        Long cardId = 10L;
        YearMonth expirationDate = YearMonth.of(2030, 12);
        CardDto response = mock(CardDto.class);

        when(cardService.setNewExpirationDate(
                cardId,
                expirationDate,
                null
        )).thenReturn(response);

        mockMvc.perform(patch("/admin/cards/{id}/expiration-date", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "expirationDate": "12/30"
                            }
                            """))
                .andExpect(status().isOk());

        verify(cardService).setNewExpirationDate(
                cardId,
                expirationDate,
                null
        );
    }
    @Test
    void setNewExpirationDate_invalidBody_returnsBadRequest() throws Exception {
        User admin = mock(User.class);

        mockMvc.perform(patch("/admin/cards/{id}/expiration-date", 10L)
                        .with(authentication(adminAuthentication(admin)))
                        .contentType("application/json")
                        .content("""
                                {
                                  "expirationDate": "incorrect"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cardService);
    }

    private UsernamePasswordAuthenticationToken adminAuthentication(User admin) {
        return new UsernamePasswordAuthenticationToken(
                admin,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}