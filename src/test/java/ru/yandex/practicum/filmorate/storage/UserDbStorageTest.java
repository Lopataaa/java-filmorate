package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({UserDbStorage.class, UserService.class})
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private UserService userService;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    public void setUp() {
        // Основной тестовый пользователь
        testUser = new User();
        testUser.setEmail("test@mail.ru");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));

        // Второй пользователь для тестов списков
        anotherUser = new User();
        anotherUser.setEmail("another@mail.ru");
        anotherUser.setLogin("anotherlogin");
        anotherUser.setName("Another User");
        anotherUser.setBirthday(LocalDate.of(1995, 1, 1));
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными")
    public void testCreateUser() {
        // When
        User createdUser = userStorage.create(testUser);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isPositive();
        assertThat(createdUser.getEmail()).isEqualTo("test@mail.ru");
        assertThat(createdUser.getLogin()).isEqualTo("testlogin");
        assertThat(createdUser.getName()).isEqualTo("Test User");
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    @DisplayName("Получение пользователя по существующему ID")
    public void testGetUserById_WhenUserExists() {
        // Given
        User createdUser = userStorage.create(testUser);
        int userId = createdUser.getId();

        // When
        Optional<User> foundUser = userStorage.getById(userId);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(userId);
        assertThat(foundUser.get().getEmail()).isEqualTo("test@mail.ru");
        assertThat(foundUser.get().getLogin()).isEqualTo("testlogin");
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему ID")
    public void testGetUserById_WhenUserNotExists() {
        // When
        Optional<User> foundUser = userStorage.getById(999);

        // Then
        assertThat(foundUser).isNotPresent();
    }

    @Test
    @DisplayName("Получение всех пользователей")
    public void testGetAllUsers() {
        // Given
        userStorage.create(testUser);
        userStorage.create(anotherUser);

        // When
        List<User> users = userStorage.getAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("test@mail.ru", "another@mail.ru");
        assertThat(users).extracting(User::getLogin)
                .containsExactlyInAnyOrder("testlogin", "anotherlogin");
    }

    @Test
    @DisplayName("Обновление существующего пользователя")
    public void testUpdateUser() {
        // Given
        User createdUser = userStorage.create(testUser);
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@mail.ru");

        // When
        User updatedUser = userStorage.update(createdUser);

        // Then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@mail.ru");
        assertThat(updatedUser.getLogin()).isEqualTo("testlogin"); // Не меняли
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя")
    public void testUpdateNonExistentUser() {
        // Given
        testUser.setId(999);

        // When
        User result = userStorage.update(testUser);
    }

    @Test
    @DisplayName("Проверка существования пользователя")
    public void testUserExists() {
        // Given
        User createdUser = userStorage.create(testUser);

        // When & Then
        assertThat(userStorage.exists(createdUser.getId())).isTrue();
        assertThat(userStorage.exists(999)).isFalse();
    }

    @Test
    @DisplayName("Удаление пользователя")
    public void testDeleteUser() {
        // Given
        User createdUser = userStorage.create(testUser);
        int userId = createdUser.getId();

        // When
        userStorage.delete(userId);

        // Then
        assertThat(userStorage.exists(userId)).isFalse();
        assertThat(userStorage.getById(userId)).isNotPresent();
    }

    @Test
    @DisplayName("Создание пользователя с логином как именем (если имя null)")
    public void testCreateUserWithNullName() {
        // Given
        testUser.setName(null);

        // When
        User createdUser = userService.createUser(testUser);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("testlogin");
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем")
    public void testCreateUserWithEmptyName() {
        // Given
        testUser.setName("");

        // When
        User createdUser = userService.createUser(testUser);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("testlogin");
    }
}