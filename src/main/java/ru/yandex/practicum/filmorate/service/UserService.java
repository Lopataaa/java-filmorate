package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public List<User> findAll() {
        log.debug("Получение списка всех пользователей");
        return userStorage.findAll();
    }

    public User create(User user) {
        log.debug("Создание пользователя: {}", user.getLogin());
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя не указано, используется логин: {}", user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        log.debug("Обновление пользователя с ID: {}", user.getId());
        validateUser(user);
        if (!userStorage.existsById(user.getId())) {
            log.warn("Попытка обновления несуществующего пользователя с ID: {}", user.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Пользователь с id=" + user.getId() + " не найден");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя не указано, используется логин: {}", user.getLogin());
        }
        return userStorage.update(user);
    }

    public User getById(Integer id) {
        log.debug("Поиск пользователя по ID: {}", id);
        return userStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID={} не найден", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Пользователь с id=" + id + " не найден");
                });
    }

    public void addFriend(Integer userId, Integer friendId) {
        log.debug("Добавление в друзья: пользователь {} добавляет пользователя {}", userId, friendId);
        User user = getById(userId);
        User friend = getById(friendId);

        if (userId.equals(friendId)) {
            log.warn("Попытка добавить себя в друзья: пользователь {}", userId);
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        user.addFriend(friendId);
        friend.addFriend(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        log.debug("Удаление из друзей: пользователь {} удаляет пользователя {}", userId, friendId);
        User user = getById(userId);
        User friend = getById(friendId);

        user.removeFriend(friendId);
        friend.removeFriend(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        log.debug("Получение списка друзей пользователя: {}", userId);
        User user = getById(userId);
        List<User> friends = user.getFriends().stream()
                .map(this::getById)
                .collect(Collectors.toList());
        log.debug("Найдено {} друзей у пользователя {}", friends.size(), userId);
        return friends;
    }

    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        log.debug("Поиск общих друзей между пользователями {} и {}", userId, otherUserId);
        User user = getById(userId);
        User otherUser = getById(otherUserId);

        List<User> commonFriends = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(this::getById)
                .collect(Collectors.toList());

        log.debug("Найдено {} общих друзей между пользователями {} и {}",
                commonFriends.size(), userId, otherUserId);
        return commonFriends;
    }

    private void validateUser(User user) {
        log.debug("Валидация пользователя: {}", user.getLogin());

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Невалидный email: {}", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Невалидный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() == null) {
            log.warn("Дата рождения не указана для пользователя: {}", user.getLogin());
            throw new ValidationException("Дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения в будущем: {} для пользователя: {}", user.getBirthday(), user.getLogin());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        log.debug("Валидация пользователя пройдена успешно: {}", user.getLogin());
    }

    public void clear() {
        log.info("Очистка данных пользователей");
        userStorage.clear();
    }
}