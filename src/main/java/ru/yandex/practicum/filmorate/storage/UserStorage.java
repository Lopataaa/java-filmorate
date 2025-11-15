package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> findAll();

    User create(User user);

    User update(User user);

    Optional<User> findById(Integer id);

    boolean existsById(Integer id);

    void clear();

    List<Friendship> getFriendshipStatuses(Integer userId);

    List<Integer> getCommonFriendIds(Integer userId, Integer otherUserId);

    List<Integer> getFriendIds(Integer userId);

    void removeFriend(Integer userId, Integer friendId);

    void confirmFriendship(Integer userId, Integer friendId);

    void addFriend(Integer userId, Integer friendId);
}