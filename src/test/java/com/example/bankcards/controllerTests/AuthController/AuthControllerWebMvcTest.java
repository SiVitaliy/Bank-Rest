package com.example.bankcards.controllerTests.AuthController;



import com.example.bankcards.controller.AuthController;
import com.example.bankcards.dto.request.AuthUserRequest;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.util.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void registration_validRequest_returnsToken()
            throws Exception, UsernameAlreadyExistsException {
        when(jwtUtil.generateToken("ivan"))
                .thenReturn("registration-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ivan",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("registration-token"));

        verify(authService).register(
                new AuthUserRequest("ivan", "password123")
        );
        verify(jwtUtil).generateToken("ivan");
    }

    @Test
    void registration_invalidRequest_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService, jwtUtil);
    }

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(authentication.getName())
                .thenReturn("ivan");
        when(jwtUtil.generateToken("ivan"))
                .thenReturn("login-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ivan",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-token"));

        verify(authenticationManager).authenticate(any());
        verify(jwtUtil).generateToken("ivan");
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ivan",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtUtil);
    }
}

