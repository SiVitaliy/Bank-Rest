
package com.example.bankcards.serviceTests.UserService;

import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService sut;

    @Test
    void findAll_withoutSearch_returnsAllUsers() {
        Pageable pageable = PageRequest.of(0, 5);

        User user = user(
                10L,
                "ivan",
                User.Role.USER,
                true
        );
        UserDto userDto = mock(UserDto.class);

        var page = new PageImpl<>(
                List.of(user),
                pageable,
                1
        );

        when(userRepository.findAll(pageable))
                .thenReturn(page);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        PageResponse<UserDto> result =
                sut.findAll(pageable, null);

        assertThat(result).isNotNull();

        verify(userRepository).findAll(pageable);
        verify(userRepository, never())
                .findAllWithSearch(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(Pageable.class)
                );
        verify(userMapper).toDto(user);
    }

    @Test
    void findAll_withBlankSearch_returnsAllUsers() {
        Pageable pageable = PageRequest.of(0, 5);

        User user = user(
                10L,
                "ivan",
                User.Role.USER,
                true
        );
        UserDto userDto = mock(UserDto.class);

        var page = new PageImpl<>(
                List.of(user),
                pageable,
                1
        );

        when(userRepository.findAll(pageable))
                .thenReturn(page);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        PageResponse<UserDto> result =
                sut.findAll(pageable, "   ");

        assertThat(result).isNotNull();

        verify(userRepository).findAll(pageable);
        verify(userRepository, never())
                .findAllWithSearch(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(Pageable.class)
                );
        verify(userMapper).toDto(user);
    }

    @Test
    void findAll_withSearch_returnsFilteredUsers() {
        Pageable pageable = PageRequest.of(1, 10);

        User user = user(
                10L,
                "ivan",
                User.Role.USER,
                true
        );
        UserDto userDto = mock(UserDto.class);

        var page = new PageImpl<>(
                List.of(user),
                pageable,
                1
        );

        when(userRepository.findAllWithSearch("ivan", pageable))
                .thenReturn(page);
        when(userMapper.toDto(user))
                .thenReturn(userDto);

        PageResponse<UserDto> result =
                sut.findAll(pageable, "ivan");

        assertThat(result).isNotNull();

        verify(userRepository)
                .findAllWithSearch("ivan", pageable);
        verify(userRepository, never())
                .findAll(pageable);
        verify(userMapper).toDto(user);
    }

    @Test
    void findAll_withEncodedSearch_decodesSearchBeforeRepositoryCall() {
        Pageable pageable = PageRequest.of(0, 5);

        User user = user(
                10L,
                "Иван Иванов",
                User.Role.USER,
                true
        );
        UserDto userDto = mock(UserDto.class);

        var page = new PageImpl<>(
                List.of(user),
                pageable,
                1
        );

        when(userRepository.findAllWithSearch(
                "Иван Иванов",
                pageable
        )).thenReturn(page);

        when(userMapper.toDto(user))
                .thenReturn(userDto);

        PageResponse<UserDto> result =
                sut.findAll(
                        pageable,
                        "%D0%98%D0%B2%D0%B0%D0%BD%20%D0%98%D0%B2%D0%B0%D0%BD%D0%BE%D0%B2"
                );

        assertThat(result).isNotNull();

        verify(userRepository)
                .findAllWithSearch(
                        "Иван Иванов",
                        pageable
                );
        verify(userMapper).toDto(user);
    }

    @Test
    void findById_existingUser_returnsMappedUser() {
        User user = user(
                10L,
                "ivan",
                User.Role.USER,
                true
        );
        UserDto expectedResult = mock(UserDto.class);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));
        when(userMapper.toDto(user))
                .thenReturn(expectedResult);

        UserDto result = sut.findById(10L);

        assertThat(result).isSameAs(expectedResult);

        verify(userRepository).findById(10L);
        verify(userMapper).toDto(user);
    }

    @Test
    void findById_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.findById(10L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(10L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void lock_enabledUser_disablesAndSavesUser() {
        User admin = user(
                100L,
                "admin",
                User.Role.ADMIN,
                true
        );
        User user = user(
                10L,
                "ivan",
                User.Role.USER,
                true
        );
        UserDto expectedResult = mock(UserDto.class);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user))
                .thenReturn(user);
        when(userMapper.toDto(user))
                .thenReturn(expectedResult);

        UserDto result = sut.lock(10L, admin);

        assertThat(result).isSameAs(expectedResult);
        assertThat(user.isEnabled()).isFalse();

        verify(userRepository).findById(10L);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void lock_userNotFound_throwsUserNotFoundException() {
        User admin = new User();

        when(userRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sut.lock(10L, admin)
        ).isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(10L);
        verify(userRepository, never())
                .save(org.mockito.ArgumentMatchers.any(User.class));
        verifyNoInteractions(userMapper);
    }

    @Test
    void lock_enabledAdmin_throwsIllegalArgumentException() {
        User admin = new User();
        User userToLock = user(
                10L,
                "another-admin",
                User.Role.ADMIN,
                true
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(userToLock));

        assertThatThrownBy(() ->
                sut.lock(10L, admin)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Невозможно заблокировать администратора.");

        assertThat(userToLock.isEnabled()).isTrue();

        verify(userRepository, never())
                .save(org.mockito.ArgumentMatchers.any(User.class));
        verifyNoInteractions(userMapper);
    }

    @Test
    void lock_alreadyLockedUser_throwsIllegalStateException() {
        User admin = new User();
        User lockedUser = user(
                10L,
                "ivan",
                User.Role.USER,
                false
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(lockedUser));

        assertThatThrownBy(() ->
                sut.lock(10L, admin)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Пользователь с id 10 уже заблокирован."
                );

        verify(userRepository, never())
                .save(org.mockito.ArgumentMatchers.any(User.class));
        verifyNoInteractions(userMapper);
    }

    @Test
    void unlock_lockedUser_enablesAndSavesUser() {
        User admin = user(
                100L,
                "admin",
                User.Role.ADMIN,
                true
        );
        User lockedUser = user(
                10L,
                "ivan",
                User.Role.USER,
                false
        );
        UserDto expectedResult = mock(UserDto.class);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(lockedUser));
        when(userRepository.save(lockedUser))
                .thenReturn(lockedUser);
        when(userMapper.toDto(lockedUser))
                .thenReturn(expectedResult);

        UserDto result = sut.unlock(10L, admin);

        assertThat(result).isSameAs(expectedResult);
        assertThat(lockedUser.isEnabled()).isTrue();

        verify(userRepository).findById(10L);
        verify(userRepository).save(lockedUser);
        verify(userMapper).toDto(lockedUser);
    }

    @Test
    void unlock_userNotFound_throwsUserNotFoundException() {
        User admin = new User();

        when(userRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sut.unlock(10L, admin)
        ).isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(10L);
        verify(userRepository, never())
                .save(org.mockito.ArgumentMatchers.any(User.class));
        verifyNoInteractions(userMapper);
    }

    @Test
    void unlock_lockedAdmin_throwsIllegalArgumentException() {
        User admin = new User();
        User lockedAdmin = user(
                10L,
                "another-admin",
                User.Role.ADMIN,
                false
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(lockedAdmin));

        assertThatThrownBy(() ->
                sut.unlock(10L, admin)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Невозможно разблокировать администратора."
                );

        assertThat(lockedAdmin.isEnabled()).isFalse();

        verify(userRepository, never())
                .save(org.mockito.ArgumentMatchers.any(User.class));
        verifyNoInteractions(userMapper);
    }

    @Test
    void unlock_enabledUser_throwsIllegalStateException() {
        User admin = new User();
        User enabledUser = user(
                10L,
                "ivan",
                User.Role.USER,
                true
        );

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(enabledUser));

        assertThatThrownBy(() ->
                sut.unlock(10L, admin)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Пользователь с id 10  не заблокирован."
                );

        verify(userRepository, never())
                .save(org.mockito.ArgumentMatchers.any(User.class));
        verifyNoInteractions(userMapper);
    }

    private User user(
            Long id,
            String username,
            User.Role role,
            boolean enabled
    ) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setEnabled(enabled);
        return user;
    }
}

