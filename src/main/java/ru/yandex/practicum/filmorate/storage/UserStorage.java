package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> findAll();

    Optional<User> findById(Integer id);

    User create(User user);

    User update(User user);

    void delete(Integer id);

    boolean existsById(Integer id);

    void addFriend(Integer userId, Integer friendId);

    void removeFriend(Integer userId, Integer friendId);

    void confirmFriendship(Integer userId, Integer friendId);

    List<Integer> getFriendIds(Integer userId);

    List<Integer> getCommonFriendIds(Integer userId, Integer otherUserId);

    List<User> getFriends(Integer userId);

    List<User> getCommonFriends(Integer userId, Integer otherUserId);

    List<Friendship> getFriendshipStatuses(Integer userId);

    void clear();
}