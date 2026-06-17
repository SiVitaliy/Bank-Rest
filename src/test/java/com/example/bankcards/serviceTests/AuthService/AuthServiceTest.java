package com.example.bankcards.serviceTests.AuthService;

import com.example.bankcards.dto.request.AuthUserRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService sut;

    @Test
    void register_usernameDoesNotExist_savesUser() {
        AuthUserRequest request = new AuthUserRequest(
                "ivan",
                "password123"
        );

        when(userRepository.existsByUsername("ivan"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        assertDoesNotThrow(() -> sut.register(request));

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).existsByUsername("ivan");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo("ivan");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    void register_usernameAlreadyExists_throwsException() {
        AuthUserRequest request = new AuthUserRequest(
                "ivan",
                "password123"
        );

        when(userRepository.existsByUsername("ivan"))
                .thenReturn(true);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> sut.register(request)
        );

        verify(userRepository).existsByUsername("ivan");
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }
}
