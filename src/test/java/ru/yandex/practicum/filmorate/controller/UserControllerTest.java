package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_LOGIN = "testuser";
    private static final String USER_NAME = "Test User";
    private static final LocalDate USER_BIRTHDAY = LocalDate.of(1990, 1, 1);

    private User createUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    @Test
    @DisplayName("Получение всех пользователей должно возвращать пустой список при отсутствии пользователей")
    void test_FindAll_ShouldReturnEmptyList() throws Exception {
        // Given
        when(userService.findAll()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).findAll();
    }

    @Test
    @DisplayName("Получение всех пользователей должно возвращать список пользователей")
    void test_FindAll_ShouldReturnUsersList() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);
        user.setId(1);
        List<User> users = List.of(user);

        when(userService.findAll()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value(USER_EMAIL))
                .andExpect(jsonPath("$[0].login").value(USER_LOGIN));

        verify(userService, times(1)).findAll();
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными должно быть успешным")
    void test_Create_ValidUserData_ShouldCreateUser() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);
        User createdUser = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);
        createdUser.setId(1);

        when(userService.create(any(User.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.login").value(USER_LOGIN))
                .andExpect(jsonPath("$.name").value(USER_NAME))
                .andExpect(jsonPath("$.birthday").value(USER_BIRTHDAY.toString()));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с пустым email должно вызывать исключение")
    void test_Create_UserWithEmptyEmail_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser("", USER_LOGIN, USER_NAME, USER_BIRTHDAY);

        when(userService.create(any(User.class)))
                .thenThrow(new ValidationException("Электронная почта не может быть пустой и должна содержать символ @"));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Электронная почта не может быть пустой и должна содержать символ @"));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с email без символа @ должно вызывать исключение")
    void test_Create_UserWithInvalidEmail_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser("invalid-email", USER_LOGIN, USER_NAME, USER_BIRTHDAY);

        when(userService.create(any(User.class)))
                .thenThrow(new ValidationException("Электронная почта не может быть пустой и должна содержать символ @"));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Электронная почта не может быть пустой и должна содержать символ @"));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с логином содержащим пробелы должно вызывать исключение")
    void test_Create_UserWithSpacesInLogin_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, "login with spaces", USER_NAME, USER_BIRTHDAY);

        when(userService.create(any(User.class)))
                .thenThrow(new ValidationException("Логин не может быть пустым и содержать пробелы"));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Логин не может быть пустым и содержать пробелы"));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем должно использовать логин как имя")
    void test_Create_UserWithEmptyName_ShouldUseLoginAsName() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, "", USER_BIRTHDAY);
        User createdUser = createUser(USER_EMAIL, USER_LOGIN, USER_LOGIN, USER_BIRTHDAY);
        createdUser.setId(1);

        when(userService.create(any(User.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(USER_LOGIN));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с null именем должно использовать логин как имя")
    void test_Create_UserWithNullName_ShouldUseLoginAsName() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, null, USER_BIRTHDAY);
        User createdUser = createUser(USER_EMAIL, USER_LOGIN, USER_LOGIN, USER_BIRTHDAY);
        createdUser.setId(1);

        when(userService.create(any(User.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(USER_LOGIN));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с датой рождения в будущем должно вызывать исключение")
    void test_Create_UserWithFutureBirthday_ShouldThrowValidationException() throws Exception {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, futureDate);

        when(userService.create(any(User.class)))
                .thenThrow(new ValidationException("Дата рождения не может быть в будущем"));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата рождения не может быть в будущем"));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя должно вызывать исключение")
    void test_Update_NonExistentUser_ShouldThrowNotFoundException() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);
        user.setId(9999);

        when(userService.update(any(User.class)))
                .thenThrow(new RuntimeException("Пользователь с id=9999 не найден"));

        // When & Then
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"));

        verify(userService, times(1)).update(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя с валидными данными должно быть успешным")
    void test_Update_ValidUser_ShouldUpdateUser() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);
        user.setId(1);

        User updatedUser = createUser("updated@example.com", USER_LOGIN, "Updated Name", USER_BIRTHDAY);
        updatedUser.setId(1);

        when(userService.update(any(User.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService, times(1)).update(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с пустым логином должно вызывать исключение")
    void test_Create_UserWithEmptyLogin_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, "", USER_NAME, USER_BIRTHDAY);

        when(userService.create(any(User.class)))
                .thenThrow(new ValidationException("Логин не может быть пустым и содержать пробелы"));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Логин не может быть пустым и содержать пробелы"));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя с null датой рождения должно вызывать исключение")
    void test_Create_UserWithNullBirthday_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, null);

        when(userService.create(any(User.class)))
                .thenThrow(new ValidationException("Дата рождения должна быть указана"));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата рождения должна быть указана"));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    @DisplayName("Получение пользователя по ID должно возвращать пользователя")
    void test_GetById_ShouldReturnUser() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);
        user.setId(1);

        when(userService.getById(1)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.login").value(USER_LOGIN));

        verify(userService, times(1)).getById(1);
    }

    @Test
    @DisplayName("Добавление друга должно быть успешным")
    void test_AddFriend_ShouldBeSuccessful() throws Exception {
        // Given
        doNothing().when(userService).addFriend(1, 2);

        // When & Then
        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        verify(userService, times(1)).addFriend(1, 2);
    }

    @Test
    @DisplayName("Удаление друга должно быть успешным")
    void test_RemoveFriend_ShouldBeSuccessful() throws Exception {
        // Given
        doNothing().when(userService).removeFriend(1, 2);

        // When & Then
        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());

        verify(userService, times(1)).removeFriend(1, 2);
    }

    @Test
    @DisplayName("Получение списка друзей должно возвращать список")
    void test_GetFriends_ShouldReturnFriendsList() throws Exception {
        // Given
        User friend = createUser("friend@example.com", "friend", "Friend User", USER_BIRTHDAY);
        friend.setId(2);
        List<User> friends = List.of(friend);

        when(userService.getFriends(1)).thenReturn(friends);

        // When & Then
        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].login").value("friend"));

        verify(userService, times(1)).getFriends(1);
    }

    @Test
    @DisplayName("Получение общих друзей должно возвращать список")
    void test_GetCommonFriends_ShouldReturnCommonFriendsList() throws Exception {
        // Given
        User commonFriend = createUser("common@example.com", "common", "Common Friend", USER_BIRTHDAY);
        commonFriend.setId(3);
        List<User> commonFriends = List.of(commonFriend);

        when(userService.getCommonFriends(1, 2)).thenReturn(commonFriends);

        // When & Then
        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].login").value("common"));

        verify(userService, times(1)).getCommonFriends(1, 2);
    }

    @Test
    @DisplayName("Очистка пользователей должна быть успешной")
    void test_Clear_ShouldBeSuccessful() throws Exception {
        // Given
        doNothing().when(userService).clear();

        // When & Then
        mockMvc.perform(delete("/users/clear"))
                .andExpect(status().isOk());

        verify(userService, times(1)).clear();
    }
}