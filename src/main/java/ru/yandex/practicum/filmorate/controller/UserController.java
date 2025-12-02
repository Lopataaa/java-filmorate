package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.UserUpdateDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        User user = userMapper.toEntity(userCreateDto);
        User createdUser = userService.createUser(user);
        return userMapper.toDto(createdUser);
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UserUpdateDto userUpdateDto) {
        User existingUser = userService.getUserById(userUpdateDto.getId());
        User updatedUser = userMapper.toEntity(userUpdateDto, existingUser);
        User savedUser = userService.updateUser(updatedUser);
        return userMapper.toDto(savedUser);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable int id) {
        User user = userService.getUserById(id);
        return userMapper.toDto(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> getFriends(@PathVariable int id) {
        List<User> friends = userService.getFriends(id);
        Collection<UserDto> dtoCollection = userMapper.toDtoCollection(friends);
        return new ArrayList<>(dtoCollection);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        Collection<UserDto> dtoCollection = userMapper.toDtoCollection(commonFriends);
        return new ArrayList<>(dtoCollection);
    }
}