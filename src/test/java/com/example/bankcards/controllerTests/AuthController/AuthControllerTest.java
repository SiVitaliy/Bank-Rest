package com.example.bankcards.controllerTests.AuthController;

import com.example.bankcards.controller.AuthController;
import com.example.bankcards.dto.request.AuthUserRequest;
import com.example.bankcards.dto.response.JwtResponseDto;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.util.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController controller;

    @Test
    void registration_validRequest_registersUserAndReturnsToken() throws UsernameAlreadyExistsException {
        AuthUserRequest request = new AuthUserRequest(
                "ivan",
                "password123"
        );

        when(jwtUtil.generateToken("ivan"))
                .thenReturn("registration-token");

        ResponseEntity<JwtResponseDto> response =
                controller.registration(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token())
                .isEqualTo("registration-token");

        verify(authService).register(request);
        verify(jwtUtil).generateToken("ivan");
    }

    @Test
    void login_validCredentials_authenticatesAndReturnsToken() {
        AuthUserRequest request = new AuthUserRequest(
                "ivan",
                "password123"
        );
        Authentication authentication =
                org.mockito.Mockito.mock(Authentication.class);

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        "ivan",
                        "password123"
                )
        )).thenReturn(authentication);

        when(authentication.getName()).thenReturn("ivan");
        when(jwtUtil.generateToken("ivan"))
                .thenReturn("login-token");

        ResponseEntity<JwtResponseDto> response =
                controller.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token())
                .isEqualTo("login-token");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(
                        UsernamePasswordAuthenticationToken.class
                );

        verify(authenticationManager).authenticate(captor.capture());

        assertThat(captor.getValue().getPrincipal()).isEqualTo("ivan");
        assertThat(captor.getValue().getCredentials())
                .isEqualTo("password123");

        verify(jwtUtil).generateToken("ivan");
    }

    @Test
    void login_invalidCredentials_throwsBadCredentialsException() {
        AuthUserRequest request = new AuthUserRequest(
                "ivan",
                "wrong-password"
        );

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        "ivan",
                        "wrong-password"
                )
        )).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> controller.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username or password");
    }
}