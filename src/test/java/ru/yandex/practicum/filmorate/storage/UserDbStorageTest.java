package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({UserDbStorage.class})
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setEmail("test@mail.ru");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    @DisplayName("Создание пользователя в базе данных")
    public void testCreateUser() {
        // When
        User createdUser = userStorage.create(testUser);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isPositive();
        assertThat(createdUser.getEmail()).isEqualTo("test@mail.ru");
        assertThat(createdUser.getLogin()).isEqualTo("testlogin");
    }

    @Test
    @DisplayName("Получение пользователя по ID")
    public void testGetUserById() {
        // Given
        User createdUser = userStorage.create(testUser);

        // When
        Optional<User> foundUser = userStorage.getById(createdUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@mail.ru");
    }

    @Test
    @DisplayName("Получение всех пользователей")
    public void testGetAllUsers() {
        // Given
        userStorage.create(testUser);

        User anotherUser = new User();
        anotherUser.setEmail("another@mail.ru");
        anotherUser.setLogin("anotherlogin");
        anotherUser.setName("Another User");
        anotherUser.setBirthday(LocalDate.of(1995, 1, 1));
        userStorage.create(anotherUser);

        // When
        List<User> users = userStorage.getAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail).containsExactlyInAnyOrder("test@mail.ru", "another@mail.ru");
    }

    @Test
    @DisplayName("Обновление пользователя")
    public void testUpdateUser() {
        // Given
        User createdUser = userStorage.create(testUser);
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@mail.ru");

        // When
        User updatedUser = userStorage.update(createdUser);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@mail.ru");

        Optional<User> foundUser = userStorage.getById(createdUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("Проверка существования пользователя")
    public void testUserExists() {
        // Given
        User createdUser = userStorage.create(testUser);

        // When
        boolean exists = userStorage.exists(createdUser.getId());
        boolean notExists = userStorage.exists(999);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Удаление пользователя")
    public void testDeleteUser() {
        // Given
        User createdUser = userStorage.create(testUser);

        // When
        boolean existsBefore = userStorage.exists(createdUser.getId());
        userStorage.delete(createdUser.getId());
        boolean existsAfter = userStorage.exists(createdUser.getId());

        // Then
        assertThat(existsBefore).isTrue();
        assertThat(existsAfter).isFalse();
    }
}