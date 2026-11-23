package com.app.meridian.user.utils;

import java.util.List;
import com.app.meridian.user.User;
import com.app.meridian.user.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toResponse(User entity);

    List<UserResponseDTO> mapAllToResponse(List<User> entities);
}
