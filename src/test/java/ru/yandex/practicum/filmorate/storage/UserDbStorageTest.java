package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan(basePackages = {"ru.yandex.practicum.filmorate.storage.db"})
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private User testUser;

//    @BeforeEach
//    void setUp() {
//        testUser = new User();
//        testUser.setEmail("test@example.com");
//        testUser.setLogin("testlogin");
//        testUser.setName("Test User");
//        testUser.setBirthday(LocalDate.of(1990, 1, 1));
//    }

    @Test
    void testCreateUser() {
        // Given
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setLogin("newuser");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(1995, 5, 15));

        // When
        User createdUser = userStorage.create(newUser);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    void testFindUserById() {
        // When
        Optional<User> userOptional = userStorage.findById(1);

        // Then
        assertThat(userOptional).isPresent();
    }

    @Test
    void testFindAllUsers() {
        // When
        List<User> users = userStorage.findAll();

        // Then
        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getLogin)
                .containsExactly("user1", "user2", "user3");
    }

    @Test
    void testUpdateUser() {
        // Given
        User userToUpdate = userStorage.findById(1).get();
        userToUpdate.setName("Updated Name");
        userToUpdate.setEmail("updated@example.com");

        // When
        User updatedUser = userStorage.update(userToUpdate);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");

        // Verify in database
        Optional<User> foundUser = userStorage.findById(1);
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Updated Name");
        assertThat(foundUser.get().getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void testDeleteUser() {
        // Given
        assertThat(userStorage.existsById(1)).isTrue();

        // When
        userStorage.delete(1);

        // Then
        assertThat(userStorage.existsById(1)).isFalse();
    }

    @Test
    void testExistsById() {
        // Then
        assertThat(userStorage.existsById(1)).isTrue();
        assertThat(userStorage.existsById(999)).isFalse();
    }
}
