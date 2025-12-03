package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dto.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.UserUpdateDto;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private UserController userController;

    @Autowired
    private UserDbStorage userStorage;

    @BeforeEach
    public void setUp() {
        userStorage.getAll().forEach(user -> userStorage.delete(user.getId()));
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными")
    public void createUserValidData() {
        // Given
        UserCreateDto userDto = UserCreateDto.builder()
                .email("user@email.com")
                .login("login")
                .name("User Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .password("password123")
                .build();

        // When
        UserDto response = userController.createUser(userDto);

        // Then
        assertNotNull(response);
        assertEquals("User Name", response.getName());
        assertEquals("user@email.com", response.getEmail());
        assertTrue(response.getId() > 0);
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем")
    public void createUserWithEmptyName() {
        // Given
        UserCreateDto userDto = UserCreateDto.builder()
                .email("user@email.com")
                .login("login")
                .name("")  // пустое имя
                .birthday(LocalDate.of(1990, 1, 1))
                .password("password123")
                .build();

        // When
        UserDto response = userController.createUser(userDto);

        // Then
        assertNotNull(response);
        assertEquals("login", response.getName());
    }

    @Test
    @DisplayName("Создание пользователя с null именем")
    public void createUserWithNullName() {
        // Given
        UserCreateDto userDto = UserCreateDto.builder()
                .email("user@email.com")
                .login("login")
                .name(null)  // null имя
                .birthday(LocalDate.of(1990, 1, 1))
                .password("password123")
                .build();

        // When
        UserDto response = userController.createUser(userDto);

        // Then
        assertNotNull(response);
        assertEquals("login", response.getName());
    }

    @Test
    @DisplayName("Создание пользователя с именем из пробелов")
    public void createUserWithWhitespaceName() {
        // Given
        UserCreateDto userDto = UserCreateDto.builder()
                .email("user@email.com")
                .login("login")
                .name("   ")  // пробелы
                .birthday(LocalDate.of(1990, 1, 1))
                .password("password123")
                .build();

        // When
        UserDto response = userController.createUser(userDto);

        // Then
        assertNotNull(response);
        assertEquals("login", response.getName());
    }

    @Test
    @DisplayName("Обновление существующего пользователя")
    public void updateUserExistingUser() {
        UserCreateDto createDto = UserCreateDto.builder()
                .email("user@email.com")
                .login("login")
                .name("Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .password("password123")
                .build();

        UserDto createdUser = userController.createUser(createDto);
        assertNotNull(createdUser);

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .id(createdUser.getId())
                .email("updated@email.com")
                .login("newlogin")
                .name("New Name")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        // When
        UserDto updatedUser = userController.updateUser(updateDto);

        // Then
        assertNotNull(updatedUser);
        assertEquals("New Name", updatedUser.getName());
        assertEquals("updated@email.com", updatedUser.getEmail());
        assertEquals("newlogin", updatedUser.getLogin());
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя")
    public void updateUserNonExistingUser() {
        // Given
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .id(999)  // несуществующий ID
                .email("user@email.com")
                .login("login")
                .name("Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        // When & Then
        assertThrows(Exception.class, () -> userController.updateUser(updateDto));
    }

    @Test
    @DisplayName("Обновление пользователя с пустым именем")
    public void updateUserWithEmptyName() {
        UserCreateDto createDto = UserCreateDto.builder()
                .email("user@email.com")
                .login("login")
                .name("Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .password("password123")
                .build();

        UserDto createdUser = userController.createUser(createDto);
        assertNotNull(createdUser);

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .id(createdUser.getId())
                .email("updated@email.com")
                .login("newlogin")
                .name("")  // пустое имя
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        // When
        UserDto updatedUser = userController.updateUser(updateDto);

        // Then
        assertNotNull(updatedUser);
        assertEquals("newlogin", updatedUser.getName());
    }

    @Test
    @DisplayName("Получение всех пользователей из пустого списка")
    public void getAllUsersEmptyList() {
        // When
        List<UserDto> users = userController.getAllUsers();

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("Получение всех пользователей с данными")
    public void getAllUsersWithData() {
        // Given
        UserCreateDto user1 = UserCreateDto.builder()
                .email("user1@email.com")
                .login("login1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .password("password123")
                .build();

        UserCreateDto user2 = UserCreateDto.builder()
                .email("user2@email.com")
                .login("login2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 1, 1))
                .password("password123")
                .build();

        userController.createUser(user1);
        userController.createUser(user2);

        // When
        List<UserDto> users = userController.getAllUsers();

        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("User One")));
        assertTrue(users.stream().anyMatch(u -> u.getName().equals("User Two")));
    }

    @Test
    @DisplayName("Создание нескольких пользователей и проверка уникальности ID")
    public void createMultipleUsersCheckIds() {
        // Given
        UserCreateDto user1 = UserCreateDto.builder()
                .email("user1@email.com")
                .login("login1")
                .name("User 1")
                .birthday(LocalDate.of(1990, 1, 1))
                .password("password123")
                .build();

        UserCreateDto user2 = UserCreateDto.builder()
                .email("user2@email.com")
                .login("login2")
                .name("User 2")
                .birthday(LocalDate.of(1995, 1, 1))
                .password("password123")
                .build();

        UserCreateDto user3 = UserCreateDto.builder()
                .email("user3@email.com")
                .login("login3")
                .name("User 3")
                .birthday(LocalDate.of(2000, 1, 1))
                .password("password123")
                .build();

        // When
        UserDto result1 = userController.createUser(user1);
        UserDto result2 = userController.createUser(user2);
        UserDto result3 = userController.createUser(user3);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);

        assertNotEquals(result1.getId(), result2.getId());
        assertNotEquals(result2.getId(), result3.getId());
        assertNotEquals(result1.getId(), result3.getId());
    }

    @Test
    @DisplayName("Создание пользователя с датой рождения в будущем")
    public void createUserWithFutureBirthday() {
        // Given
        UserCreateDto userDto = UserCreateDto.builder()
                .email("user@email.com")
                .login("login")
                .name("Name")
                .birthday(LocalDate.now().plusDays(1))
                .password("password123")
                .build();

        // When & Then
        assertThrows(Exception.class, () -> userController.createUser(userDto));
    }

    @Test
    @DisplayName("Создание пользователя с текущей датой рождения")
    public void createUserWithCurrentDateBirthday() {
        // Given
        UserCreateDto userDto = UserCreateDto.builder()
                .email("user@email.com")
                .login("login")
                .name("Name")
                .birthday(LocalDate.now())
                .password("password123")
                .build();

        // When
        UserDto response = userController.createUser(userDto);

        // Then
        assertNotNull(response);
        assertEquals("Name", response.getName());
    }

    @Test
    @DisplayName("Создание пользователя с очень старой датой рождения")
    public void createUserWithVeryOldBirthday() {
        // Given
        UserCreateDto userDto = UserCreateDto.builder()
                .email("user@email.com")
                .login("login")
                .name("Name")
                .birthday(LocalDate.of(1900, 1, 1))
                .password("password123")
                .build();

        // When
        UserDto response = userController.createUser(userDto);

        // Then
        assertNotNull(response);
        assertEquals("Name", response.getName());
    }

    @Test
    @DisplayName("Получение пользователя по ID")
    public void getUserById() {
        // Given
        UserCreateDto createDto = UserCreateDto.builder()
                .email("user@email.com")
                .login("login")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .password("password123")
                .build();

        UserDto createdUser = userController.createUser(createDto);

        // When
        UserDto foundUser = userController.getUser(createdUser.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals("Test User", foundUser.getName());
    }
}