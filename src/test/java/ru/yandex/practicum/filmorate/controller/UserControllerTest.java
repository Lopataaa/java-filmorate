package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void test_Create_ValidUserData_ShouldCreateUser() throws Exception {
        // Given
        User user = createUser("test@example.com", "testuser", "Test User", LocalDate.of(1990, 1, 1));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("Создание пользователя с пустым email должно вызывать исключение")
    void test_Create_EmptyEmail_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser("", "testuser", "Test User", LocalDate.of(1990, 1, 1));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание пользователя с email без @ должно вызывать исключение")
    void test_Create_EmailWithoutAt_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser("invalid-email", "testuser", "Test User", LocalDate.of(1990, 1, 1));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание пользователя с пустым логином должно вызывать исключение")
    void test_Create_EmptyLogin_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser("test@example.com", "", "Test User", LocalDate.of(1990, 1, 1));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание пользователя с логином содержащим пробелы должно вызывать исключение")
    void test_Create_LoginWithSpaces_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser("test@example.com", "test user", "Test User", LocalDate.of(1990, 1, 1));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание пользователя с датой рождения в будущем должно вызывать исключение")
    void test_Create_FutureBirthday_ShouldThrowValidationException() throws Exception {
        // Given
        User user = createUser("test@example.com", "testuser", "Test User", LocalDate.now().plusDays(1));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание пользователя без имени должно использовать логин как имя")
    void test_Create_UserWithoutName_ShouldUseLoginAsName() throws Exception {
        // Given
        User user = createUser("test@example.com", "testuser", null, LocalDate.of(1990, 1, 1));

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("testuser"));
    }
}