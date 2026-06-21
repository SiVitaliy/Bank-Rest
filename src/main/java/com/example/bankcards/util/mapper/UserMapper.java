package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.response.UserDto;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
/**
 * Mapper для преобразования сущности User в DTO.
 *
 * Используется для безопасного возврата информации о пользователе
 * через REST API без раскрытия чувствительных данных (например, пароля).
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Преобразует сущность User в UserDto.
     *
     * @param user сущность пользователя
     * @return DTO пользователя
     */
    UserDto toDto(User user);
}
