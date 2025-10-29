package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
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
    public void clear() {
        log.info("Очистка хранилища пользователей");
        users.clear();
        nextId = 1;
    }
}