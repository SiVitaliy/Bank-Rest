package com.example.bankcards.controllerTests.CardRequestController;

import com.example.bankcards.controller.CardRequestController;
import com.example.bankcards.dto.request.CardActionRequest;
import com.example.bankcards.dto.response.CardRequestDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.CardRequestService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardRequestController.class)
class CardRequestControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardRequestService cardRequestService;

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
    void manageCard_validRequest_savesCardRequest() throws Exception {
        User user = authenticatedUser();
        CardRequestDto response = mock(CardRequestDto.class);

        when(cardRequestService.save(any(CardActionRequest.class), eq(user)))
                .thenReturn(response);

        mockMvc.perform(post("/api/card-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardId": 3,
                                  "action": "BLOCK"
                                }
                                """)
                        .with(authentication(userAuthentication(user)))
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<CardActionRequest> requestCaptor =
                ArgumentCaptor.forClass(CardActionRequest.class);

        verify(cardRequestService).save(
                requestCaptor.capture(),
                eq(user)
        );

        assertThat(requestCaptor.getValue()).isNotNull();
    }

    @Test
    void manageCard_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/card-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardId": 3,
                                  "action": "BLOCK"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cardRequestService);
    }

    @Test
    void manageCard_invalidJson_returnsBadRequest() throws Exception {
        User user = authenticatedUser();

        mockMvc.perform(post("/api/card-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{")
                        .with(authentication(userAuthentication(user)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cardRequestService);
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

