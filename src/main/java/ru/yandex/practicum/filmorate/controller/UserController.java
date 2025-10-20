package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextUserId = 1;

    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        log.info("GET /users - получение списка всех пользователей. Количество пользователей: {}", users.size());
        List<User> userList = new ArrayList<>(users.values());
        return ResponseEntity.ok(userList);
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        log.info("POST /users - попытка создания нового пользователя: {}", user);

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        user.setId(nextUserId++);
        users.put(user.getId(), user);

        log.info("POST /users - пользователь успешно создан с ID: {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping
    public ResponseEntity<User> update(@RequestBody User user) {
        log.info("PUT /users - попытка обновления пользователя: {}", user);

        if (user.getId() == null || !users.containsKey(user.getId())) {
            String errorMessage = "Пользователь с id=" + user.getId() + " не найден";
            log.error("PUT /users - ошибка: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя не указано, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);

        log.info("PUT /users - пользователь с ID {} успешно обновлен", user.getId());
        return ResponseEntity.ok(user);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            String errorMessage = "Электронная почта не может быть пустой и должна содержать символ @";
            log.warn("Валидация пользователя failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            String errorMessage = "Логин не может быть пустым и содержать пробелы";
            log.warn("Валидация пользователя failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (user.getBirthday() == null) {
            String errorMessage = "Дата рождения должна быть указана";
            log.warn("Валидация пользователя failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            String errorMessage = "Дата рождения не может быть в будущем";
            log.warn("Валидация пользователя failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

        log.debug("Валидация пользователя пройдена успешно");
    }
}