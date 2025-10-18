package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    private UserController userController;

    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_LOGIN = "testuser";
    private static final String USER_NAME = "Test User";
    private static final LocalDate USER_BIRTHDAY = LocalDate.of(1990, 1, 1);
    private static final Integer NON_EXISTENT_ID = 9999;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    private User createUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными должно быть успешным")
    void test_Create_ValidUserData_ShouldCreateUser() {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);

        // When
        User result = userController.create(user);

        // Then
        assertNotNull(result.getId(), "Пользователь должен получить ID");
        assertEquals(USER_EMAIL, result.getEmail(), "Email должен совпадать");
        assertEquals(USER_LOGIN, result.getLogin(), "Логин должен совпадать");
        assertEquals(USER_NAME, result.getName(), "Имя должно совпадать");
    }

    @Test
    @DisplayName("Создание пользователя с пустым email должно вызывать исключение")
    void test_Create_UserWithEmptyEmail_ShouldThrowValidationException() {
        // Given
        User user = createUser("", USER_LOGIN, USER_NAME, USER_BIRTHDAY);

        // When & Then
        assertThrows(ValidationException.class, () -> userController.create(user),
                "Должно быть выброшено ValidationException при пустом email");
    }

    @Test
    @DisplayName("Создание пользователя с email без символа @ должно вызывать исключение")
    void test_Create_UserWithInvalidEmail_ShouldThrowValidationException() {
        // Given
        User user = createUser("invalid-email", USER_LOGIN, USER_NAME, USER_BIRTHDAY);

        // When & Then
        assertThrows(ValidationException.class, () -> userController.create(user),
                "Должно быть выброшено ValidationException при email без символа @");
    }

    @Test
    @DisplayName("Создание пользователя с логином содержащим пробелы должно вызывать исключение")
    void test_Create_UserWithSpacesInLogin_ShouldThrowValidationException() {
        // Given
        User user = createUser(USER_EMAIL, "login with spaces", USER_NAME, USER_BIRTHDAY);

        // When & Then
        assertThrows(ValidationException.class, () -> userController.create(user),
                "Должно быть выброшено ValidationException при логине с пробелами");
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем должно использовать логин как имя")
    void test_Create_UserWithEmptyName_ShouldUseLoginAsName() {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, "", USER_BIRTHDAY);

        // When
        User result = userController.create(user);

        // Then
        assertEquals(USER_LOGIN, result.getName(),
                "При пустом имени должно использоваться значение логина");
    }

    @Test
    @DisplayName("Создание пользователя с датой рождения в будущем должно вызывать исключение")
    void test_Create_UserWithFutureBirthday_ShouldThrowValidationException() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, futureDate);

        // When & Then
        assertThrows(ValidationException.class, () -> userController.create(user),
                "Должно быть выброшено ValidationException при дате рождения в будущем");
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя должно вызывать исключение")
    void test_Update_NonExistentUser_ShouldThrowValidationException() {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);
        user.setId(NON_EXISTENT_ID);

        // When & Then
        assertThrows(ValidationException.class, () -> userController.update(user),
                "Должно быть выброшено ValidationException при обновлении несуществующего пользователя");
    }
}