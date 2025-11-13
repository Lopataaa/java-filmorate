package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        log.info("GET /users - получение списка всех пользователей");
        List<User> users = userService.findAll();
        log.debug("GET /users - найдено {} пользователей", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Integer id) {
        log.info("GET /users/{} - получение пользователя по ID", id);
        User user = userService.getById(id);
        log.debug("GET /users/{} - найден пользователь: '{}'", id, user.getLogin());
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        log.info("POST /users - попытка создания нового пользователя: {}", user.getLogin());
        log.debug("POST /users - детали создаваемого пользователя: {}", user);

        User createdUser = userService.create(user);

        log.info("POST /users - пользователь успешно создан с ID: {}", createdUser.getId());
        log.debug("POST /users - созданный пользователь: {}", createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping
    public ResponseEntity<User> update(@RequestBody User user) {
        log.info("PUT /users - попытка обновления пользователя с ID: {}", user.getId());
        log.debug("PUT /users - обновляемые данные: {}", user);

        User updatedUser = userService.update(user);

        log.info("PUT /users - пользователь с ID {} успешно обновлен", updatedUser.getId());
        log.debug("PUT /users - обновленный пользователь: {}", updatedUser);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("PUT /users/{}/friends/{} - добавление в друзья", id, friendId);
        userService.addFriend(id, friendId);
        log.debug("PUT /users/{}/friends/{} - дружба успешно установлена", id, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("DELETE /users/{}/friends/{} - удаление из друзей", id, friendId);
        userService.removeFriend(id, friendId);
        log.debug("DELETE /users/{}/friends/{} - дружба успешно удалена", id, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable Integer id) {
        log.info("GET /users/{}/friends - получение списка друзей", id);
        List<User> friends = userService.getFriends(id);
        log.debug("GET /users/{}/friends - найдено {} друзей", id, friends.size());
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(
            @PathVariable Integer id,
            @PathVariable Integer otherId) {
        log.info("GET /users/{}/friends/common/{} - получение общих друзей", id, otherId);
        List<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.debug("GET /users/{}/friends/common/{} - найдено {} общих друзей", id, otherId, commonFriends.size());
        return ResponseEntity.ok(commonFriends);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clear() {
        log.info("DELETE /users/clear - очистка всех пользователей");
        userService.clear();
        return ResponseEntity.ok().build();
    }
}