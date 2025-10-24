package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserController userController;

    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_LOGIN = "testuser";
    private static final String USER_NAME = "Test User";
    private static final LocalDate USER_BIRTHDAY = LocalDate.of(1990, 1, 1);
    private static final Integer NON_EXISTENT_ID = 9999;

    @BeforeEach
    void setUp() {
        userController.clear();
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
    @DisplayName("Получение всех пользователей должно возвращать пустой список при отсутствии пользователей")
    void test_FindAll_ShouldReturnEmptyList() throws Exception {
        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными должно быть успешным")
    void test_Create_ValidUserData_ShouldCreateUser() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.login").value(USER_LOGIN))
                .andExpect(jsonPath("$.name").value(USER_NAME))
                .andExpect(jsonPath("$.birthday").value(USER_BIRTHDAY.toString()));
    }

    @Test
    @DisplayName("Создание пользователя с пустым email должно вызывать исключение")
    void test_Create_UserWithEmptyEmail_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser("", USER_LOGIN, USER_NAME, USER_BIRTHDAY);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Электронная почта не может быть пустой и должна содержать символ @"));
    }

    @Test
    @DisplayName("Создание пользователя с email без символа @ должно вызывать исключение")
    void test_Create_UserWithInvalidEmail_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser("invalid-email", USER_LOGIN, USER_NAME, USER_BIRTHDAY);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Электронная почта не может быть пустой и должна содержать символ @"));
    }

    @Test
    @DisplayName("Создание пользователя с логином содержащим пробелы должно вызывать исключение")
    void test_Create_UserWithSpacesInLogin_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, "login with spaces", USER_NAME, USER_BIRTHDAY);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Логин не может быть пустым и содержать пробелы"));
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем должно использовать логин как имя")
    void test_Create_UserWithEmptyName_ShouldUseLoginAsName() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, "", USER_BIRTHDAY);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(USER_LOGIN));
    }

    @Test
    @DisplayName("Создание пользователя с null именем должно использовать логин как имя")
    void test_Create_UserWithNullName_ShouldUseLoginAsName() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, null, USER_BIRTHDAY);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(USER_LOGIN));
    }

    @Test
    @DisplayName("Создание пользователя с датой рождения в будущем должно вызывать исключение")
    void test_Create_UserWithFutureBirthday_ShouldThrowValidationException() throws Exception {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, futureDate);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Дата рождения не может быть в будущем"));
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя должно вызывать исключение")
    void test_Update_NonExistentUser_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);
        user.setId(NON_EXISTENT_ID);

        // When & Then
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Пользователь с id=9999 не найден"));
    }

    @Test
    @DisplayName("Обновление пользователя с валидными данными должно быть успешным")
    void test_Update_ValidUser_ShouldUpdateUser() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, USER_BIRTHDAY);

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);

        // When
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@example.com");

        // Then
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @DisplayName("Создание пользователя с пустым логином должно вызывать исключение")
    void test_Create_UserWithEmptyLogin_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, "", USER_NAME, USER_BIRTHDAY);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Логин не может быть пустым и содержать пробелы"));
    }

    @Test
    @DisplayName("Создание пользователя с null датой рождения должно вызывать исключение")
    void test_Create_UserWithNullBirthday_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser(USER_EMAIL, USER_LOGIN, USER_NAME, null);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Дата рождения должна быть указана"));
    }
}