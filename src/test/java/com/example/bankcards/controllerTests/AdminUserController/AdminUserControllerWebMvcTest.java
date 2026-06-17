
package com.example.bankcards.controllerTests.AdminUserController;

import com.example.bankcards.controller.admin.AdminUserController;
import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain filterChain = invocation.getArgument(2);

            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void findAllUsers_validRequest_returnsOk() throws Exception {
        PageResponse<UserDto> response = mock(PageResponse.class);

        when(userService.findAll(any(Pageable.class), eq(null)))
                .thenReturn(response);

        mockMvc.perform(get("/admin/users")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(userService).findAll(
                pageableCaptor.capture(),
                eq(null)
        );

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("creationTime"))
                .isNotNull()
                .satisfies(order -> assertThat(order.isAscending()).isTrue());
    }

    @Test
    void findAllUsers_withSearchAndPageable_passesParametersToService() throws Exception {
        String search = "ivan";
        PageResponse<UserDto> response = mock(PageResponse.class);

        when(userService.findAll(any(Pageable.class), eq(search)))
                .thenReturn(response);

        mockMvc.perform(get("/admin/users")
                        .param("search", search)
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "creationTime,desc")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(userService).findAll(
                pageableCaptor.capture(),
                eq(search)
        );

        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("creationTime"))
                .isNotNull()
                .satisfies(order -> assertThat(order.isDescending()).isTrue());
    }

    @Test
    void findUser_existingUser_returnsOk() throws Exception {
        long userId = 3L;
        UserDto response = mock(UserDto.class);

        when(userService.findById(userId))
                .thenReturn(response);

        mockMvc.perform(get("/admin/users/{id}", userId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        verify(userService).findById(userId);
    }

    @Test
    void lock_existingUser_returnsOk() throws Exception {
        long userId = 3L;
        User admin = mock(User.class);
        UserDto response = mock(UserDto.class);

        when(userService.lock(userId, admin))
                .thenReturn(response);

        mockMvc.perform(patch("/admin/users/{userId}/lock", userId)
                        .with(authentication(adminAuthentication(admin)))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userService).lock(userId, admin);
    }

    @Test
    void unlock_existingUser_returnsOk() throws Exception {
        long userId = 3L;
        User admin = mock(User.class);
        UserDto response = mock(UserDto.class);

        when(userService.unlock(userId, admin))
                .thenReturn(response);

        mockMvc.perform(patch("/admin/users/{userId}/unlock", userId)
                        .with(authentication(adminAuthentication(admin)))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userService).unlock(userId, admin);
    }

    @Test
    void findAllUsers_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void lock_withoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/admin/users/{userId}/lock", 3L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    private UsernamePasswordAuthenticationToken adminAuthentication(User admin) {
        return new UsernamePasswordAuthenticationToken(
                admin,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}

