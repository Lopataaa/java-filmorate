package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));

        Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            user.setBirthday(birthday.toLocalDate());
        }

        return user;
    };

    private final RowMapper<Friendship> friendshipRowMapper = (rs, rowNum) -> {
        Friendship friendship = new Friendship();
        friendship.setUserId(rs.getInt("user_id"));
        friendship.setFriendId(rs.getInt("friend_id"));
        friendship.setStatus(Friendship.FriendshipStatus.valueOf(rs.getString("status")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            friendship.setCreatedAt(createdAt.toLocalDateTime());
        }

        return friendship;
    };

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);
        // Загружаем друзей для каждого пользователя
        users.forEach(this::loadFriends);
        return users;
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        User user = users.get(0);
        loadFriends(user);
        return Optional.of(user);
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'PENDING')";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void confirmFriendship(Integer userId, Integer friendId) {
        String sql = "UPDATE friendships SET status = 'CONFIRMED' WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<Integer> getFriendIds(Integer userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ? AND status = 'CONFIRMED'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("friend_id"), userId);
    }

    @Override
    public List<Integer> getCommonFriendIds(Integer userId, Integer otherUserId) {
        String sql = "SELECT f1.friend_id FROM friendships f1 " +
                "JOIN friendships f2 ON f1.friend_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? " +
                "AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("friend_id"), userId, otherUserId);
    }

    @Override
    public List<User> getFriends(Integer userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? AND f.status = 'CONFIRMED'";
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id " +
                "JOIN friendships f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? " +
                "AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'";
        return jdbcTemplate.query(sql, userRowMapper, userId, otherUserId);
    }

    @Override
    public List<Friendship> getFriendshipStatuses(Integer userId) {
        String sql = "SELECT * FROM friendships WHERE user_id = ?";
        return jdbcTemplate.query(sql, friendshipRowMapper, userId);
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM users";
        jdbcTemplate.update(sql);
    }

    private void loadFriends(User user) {
        List<Integer> friendIds = getFriendIds(user.getId());
        user.getFriends().addAll(friendIds);
    }
}