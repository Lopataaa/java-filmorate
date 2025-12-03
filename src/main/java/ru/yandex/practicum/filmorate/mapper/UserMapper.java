package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.UserUpdateDto;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
    }

    public Collection<UserDto> toDtoCollection(Collection<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public User toEntity(UserCreateDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setLogin(dto.getLogin());

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            user.setName(dto.getLogin());
        } else {
            user.setName(dto.getName());
        }

        user.setBirthday(dto.getBirthday());

        validateBirthday(user.getBirthday());

        return user;
    }

    public User toEntity(UserUpdateDto dto, User existingUser) {
        if (dto == null || existingUser == null) {
            return existingUser;
        }

        existingUser.setEmail(dto.getEmail());
        existingUser.setLogin(dto.getLogin());

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            existingUser.setName(dto.getLogin());
        } else {
            existingUser.setName(dto.getName());
        }

        existingUser.setBirthday(dto.getBirthday());

        validateBirthday(existingUser.getBirthday());

        return existingUser;
    }

    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setLogin(dto.getLogin());

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            user.setName(dto.getLogin());
        } else {
            user.setName(dto.getName());
        }

        user.setBirthday(dto.getBirthday());

        validateBirthday(user.getBirthday());

        return user;
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }
    }
}