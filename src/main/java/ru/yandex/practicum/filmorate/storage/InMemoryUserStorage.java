package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private int nextId = 1;

    @Override
    public List<User> findAll() {
        log.info("Получение списка всех пользователей. Количество: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан с ID: {}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new RuntimeException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.info("Пользователь с ID {} успешно обновлен", user.getId());
        return user;
    }

    @Override
    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public boolean existsById(Integer id) {
        return users.containsKey(id);
    }

    @Override
    public void delete(Integer id) {
        if (users.containsKey(id)) {
            users.remove(id);
            log.info("Пользователь с ID {} удален", id);
        } else {
            log.warn("Попытка удаления несуществующего пользователя с ID: {}", id);
        }
    }

    @Override
    public void clear() {
        log.info("Очистка хранилища пользователей");
        users.clear();
        nextId = 1;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null && friend != null) {
            user.addFriend(friendId);
            log.debug("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        User user = users.get(userId);
        if (user != null) {
            user.removeFriend(friendId);
            log.debug("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        }
    }

    @Override
    public void confirmFriendship(Integer userId, Integer friendId) {
        log.debug("Подтверждение дружбы между пользователями {} и {}", userId, friendId);
    }

    @Override
    public List<Integer> getFriendIds(Integer userId) {
        User user = users.get(userId);
        return user != null ? new ArrayList<>(user.getFriends()) : new ArrayList<>();
    }

    @Override
    public List<Integer> getCommonFriendIds(Integer userId, Integer otherUserId) {
        List<Integer> userFriends = getFriendIds(userId);
        List<Integer> otherUserFriends = getFriendIds(otherUserId);

        return userFriends.stream().filter(otherUserFriends::contains).collect(Collectors.toList());
    }

    @Override
    public List<User> getFriends(Integer userId) {
        List<Integer> friendIds = getFriendIds(userId);
        return friendIds.stream().map(users::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        List<Integer> commonFriendIds = getCommonFriendIds(userId, otherUserId);
        return commonFriendIds.stream().map(users::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<Friendship> getFriendshipStatuses(Integer userId) {
        log.debug("Получение статусов дружбы для пользователя: {}", userId);
        return new ArrayList<>();
    }
}