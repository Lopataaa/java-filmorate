package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
@Import(GlobalExceptionHandler.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String FILM_NAME = "Test Film";
    private static final String FILM_DESCRIPTION = "Test Description";
    private static final LocalDate FILM_RELEASE_DATE = LocalDate.of(2000, 1, 1);
    private static final Integer FILM_DURATION = 120;
    private static final Integer NON_EXISTENT_ID = 9999;

    private Film createFilm(String name, String description, LocalDate releaseDate, Integer duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        return film;
    }

    @Test
    @DisplayName("Создание фильма с валидными данными должно быть успешным")
    void test_Create_ValidFilmData_ShouldCreateFilm() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(FILM_NAME))
                .andExpect(jsonPath("$.description").value(FILM_DESCRIPTION))
                .andExpect(jsonPath("$.releaseDate").value(FILM_RELEASE_DATE.toString()))
                .andExpect(jsonPath("$.duration").value(FILM_DURATION));
    }

    @Test
    @DisplayName("Создание фильма с пустым названием должно вызывать исключение")
    void test_Create_EmptyFilmName_ShouldThrowValidationException() throws Exception {
        // Given
        Film film = createFilm("", FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Название фильма не может быть пустым"));
    }

    @Test
    @DisplayName("Создание фильма с null названием должно вызывать исключение")
    void test_Create_NullFilmName_ShouldThrowValidationException() throws Exception {
        // Given
        Film film = createFilm(null, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Название фильма не может быть пустым"));
    }

    @Test
    @DisplayName("Создание фильма с описанием длиннее 200 символов должно вызывать исключение")
    void test_Create_FilmDescriptionExceeds200Chars_ShouldThrowValidationException() throws Exception {
        // Given
        String longDescription = "A".repeat(201);
        Film film = createFilm(FILM_NAME, longDescription, FILM_RELEASE_DATE, FILM_DURATION);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Описание не может превышать 200 символов"));
    }

    @Test
    @DisplayName("Создание фильма с описанием 200 символов должно быть успешным")
    void test_Create_FilmDescription200Chars_ShouldBeSuccessful() throws Exception {
        // Given
        String exact200CharsDescription = "A".repeat(200);
        Film film = createFilm(FILM_NAME, exact200CharsDescription, FILM_RELEASE_DATE, FILM_DURATION);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(exact200CharsDescription));
    }

    @Test
    @DisplayName("Создание фильма с null датой релиза должно вызывать исключение")
    void test_Create_NullReleaseDate_ShouldThrowValidationException() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, null, FILM_DURATION);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Дата релиза должна быть указана"));
    }

    @Test
    @DisplayName("Создание фильма с отрицательной продолжительностью должно вызывать исключение")
    void test_Create_FilmWithNegativeDuration_ShouldThrowValidationException() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, -103);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    @DisplayName("Создание фильма с нулевой продолжительностью должно вызывать исключение")
    void test_Create_FilmWithZeroDuration_ShouldThrowValidationException() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, 0);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    @DisplayName("Обновление фильма с валидными данными должно быть успешным")
    void test_Update_ValidFilm_ShouldUpdateFilm() throws Exception {
        // Сначала создаем фильм
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Film createdFilm = objectMapper.readValue(response, Film.class);

        // Обновляем данные
        createdFilm.setName("Updated Film Name");
        createdFilm.setDescription("Updated Description");

        // When & Then
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Film Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    @DisplayName("Обновление несуществующего фильма должно вызывать исключение")
    void test_Update_NonExistentFilm_ShouldThrowValidationException() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);
        film.setId(NON_EXISTENT_ID);

        // When & Then
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Фильм с id=9999 не найден"));
    }

    @Test
    @DisplayName("Получение всех фильмов должно возвращать список")
    void test_GetAll_ShouldReturnFilmList() throws Exception {
        // When & Then
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Создание и получение нескольких фильмов должно работать корректно")
    void test_CreateAndGetMultipleFilms_ShouldWorkCorrectly() throws Exception {
        // Given
        Film film1 = createFilm("Film 1", "Description 1", LocalDate.of(2000, 1, 1), 120);
        Film film2 = createFilm("Film 2", "Description 2", LocalDate.of(2001, 1, 1), 130);

        // When & Then
        String response1 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Film createdFilm1 = objectMapper.readValue(response1, Film.class);

        String response2 = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Film createdFilm2 = objectMapper.readValue(response2, Film.class);

        // Then
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == " + createdFilm1.getId() + ")].name").value("Film 1"))
                .andExpect(jsonPath("$[?(@.id == " + createdFilm2.getId() + ")].name").value("Film 2"));
    }
}