package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.response.UserDto;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {


    UserDto toDto(User user);
}
