package com.example.bankcards.service;

import com.example.bankcards.dto.response.PageResponse;
import com.example.bankcards.dto.response.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
/**
 * Сервис для работы с пользователями.
 *
 * Отвечает за поиск пользователей, получение пользователя по идентификатору,
 * а также блокировку и разблокировку пользовательских учётных записей
 * администратором.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Возвращает страницу пользователей.
     *
     * Если передана строка поиска, выполняет поиск пользователей по username
     * без учёта регистра. Если строка поиска отсутствует, возвращает всех
     * пользователей с учётом пагинации и сортировки.
     *
     * @param pageable параметры пагинации и сортировки
     * @param search строка поиска по имени пользователя
     * @return страница найденных пользователей
     */
    public PageResponse<UserDto> findAll(Pageable pageable, String search){
        if (search!=null && !search.isBlank()){
            String decoded = URLDecoder.decode(search, StandardCharsets.UTF_8);
            Page<User> pageOfUsers=userRepository.findAllWithSearch(decoded,pageable);
            log.info("Поиск пользователей по строке '"+decoded+"' . Найдено элементов: "+pageOfUsers.getTotalElements());
            return PageResponse.from(pageOfUsers,userMapper::toDto);
        }

        Page<User> pageOfUsers=userRepository.findAll(pageable);
        log.debug("Поиск пользователей: search={}, page={}, size={}",
                search, pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(pageOfUsers,userMapper::toDto);
    }
    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return данные найденного пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    public  UserDto findById(Long id){
        User user =  userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException(id));

        log.info("Найден пользователь с id "+ id);


        return userMapper.toDto(user);
    }

    /**
     * Блокирует пользователя.
     *
     * Блокировка администратора запрещена. Если пользователь уже заблокирован,
     * выбрасывается исключение о некорректном состоянии операции.
     *
     * @param userId идентификатор блокируемого пользователя
     * @param admin администратор, выполняющий блокировку
     * @return данные заблокированного пользователя
     * @throws UserNotFoundException если пользователь не найден
     * @throws IllegalArgumentException если выполняется попытка заблокировать администратора
     * @throws IllegalStateException если пользователь уже заблокирован
     */
    public UserDto lock(Long userId, User admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
        if (user.isEnabled()){
            if (user.getRole().equals(User.Role.ADMIN)){
                throw new IllegalArgumentException("Невозможно заблокировать администратора.");
            }
            user.setEnabled(false);
            log.info("Администратор adminId={} заблокировал пользователя userId={}",
                    admin.getId(), user.getId());
            return userMapper.toDto(userRepository.save(user));
        }
        log.warn("Попытка заблокировать уже заблокированного пользователя userId={}", userId);
        throw new IllegalStateException("Пользователь с id "+userId+" уже заблокирован.");
    }
    /**
     * Разблокирует пользователя.
     *
     * Разблокировка администратора запрещена. Если пользователь не заблокирован,
     * выбрасывается исключение о некорректном состоянии операции.
     *
     * @param userId идентификатор разблокируемого пользователя
     * @param admin администратор, выполняющий разблокировку
     * @return данные разблокированного пользователя
     * @throws UserNotFoundException если пользователь не найден
     * @throws IllegalArgumentException если выполняется попытка разблокировать администратора
     * @throws IllegalStateException если пользователь не заблокирован
     */
    public UserDto unlock(Long userId, User admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
        if (!user.isEnabled()){
            if (user.getRole().equals(User.Role.ADMIN)){
                throw new IllegalArgumentException("Невозможно разблокировать администратора.");
            }
            user.setEnabled(true);
            log.info("Администратор {} разблокировал пользователя {}", admin.getUsername(), user.getUsername());
            return userMapper.toDto(userRepository.save(user));
        }

        throw new IllegalStateException("Пользователь с id "+userId+"  не заблокирован.");
    }
}
