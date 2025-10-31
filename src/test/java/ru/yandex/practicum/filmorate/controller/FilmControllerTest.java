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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    @MockBean
    private UserService userService;

    private static final String FILM_NAME = "Test Film";
    private static final String FILM_DESCRIPTION = "Test Description";
    private static final LocalDate FILM_RELEASE_DATE = LocalDate.of(2000, 1, 1);
    private static final Integer FILM_DURATION = 120;

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
        Film createdFilm = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);
        createdFilm.setId(1);

        when(filmService.create(any(Film.class))).thenReturn(createdFilm);

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(FILM_NAME))
                .andExpect(jsonPath("$.description").value(FILM_DESCRIPTION))
                .andExpect(jsonPath("$.releaseDate").value(FILM_RELEASE_DATE.toString()))
                .andExpect(jsonPath("$.duration").value(FILM_DURATION));

        verify(filmService, times(1)).create(any(Film.class));
    }

    @Test
    @DisplayName("Создание фильма с пустым названием должно вызывать исключение")
    void test_Create_EmptyFilmName_ShouldThrowValidationException() throws Exception {
        // Given
        Film film = createFilm("", FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);

        when(filmService.create(any(Film.class)))
                .thenThrow(new ValidationException("Название фильма не может быть пустым"));

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Название фильма не может быть пустым"));

        verify(filmService, times(1)).create(any(Film.class));
    }

    @Test
    @DisplayName("Создание фильма с описанием длиннее 200 символов должно вызывать исключение")
    void test_Create_FilmDescriptionExceeds200Chars_ShouldThrowValidationException() throws Exception {
        // Given
        String longDescription = "A".repeat(201);
        Film film = createFilm(FILM_NAME, longDescription, FILM_RELEASE_DATE, FILM_DURATION);

        when(filmService.create(any(Film.class)))
                .thenThrow(new ValidationException("Описание не может превышать 200 символов"));

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Описание не может превышать 200 символов"));

        verify(filmService, times(1)).create(any(Film.class));
    }

    @Test
    @DisplayName("Создание фильма с датой релиза до 1895 года должно вызывать исключение")
    void test_Create_FilmReleaseDateBefore1895_ShouldThrowValidationException() throws Exception {
        // Given
        LocalDate earlyDate = LocalDate.of(1890, 1, 1);
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, earlyDate, FILM_DURATION);

        when(filmService.create(any(Film.class)))
                .thenThrow(new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года"));

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата релиза не может быть раньше 28 декабря 1895 года"));

        verify(filmService, times(1)).create(any(Film.class));
    }

    @Test
    @DisplayName("Создание фильма с отрицательной продолжительностью должно вызывать исключение")
    void test_Create_FilmWithNegativeDuration_ShouldThrowValidationException() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, -100);

        when(filmService.create(any(Film.class)))
                .thenThrow(new ValidationException("Продолжительность фильма должна быть положительным числом"));

        // When & Then
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Продолжительность фильма должна быть положительным числом"));

        verify(filmService, times(1)).create(any(Film.class));
    }

    @Test
    @DisplayName("Обновление несуществующего фильма должно вызывать исключение")
    void test_Update_NonExistentFilm_ShouldThrowNotFoundException() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);
        film.setId(9999);

        when(filmService.update(any(Film.class)))
                .thenThrow(new RuntimeException("Фильм с id=9999 не найден"));

        // When & Then
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"));

        verify(filmService, times(1)).update(any(Film.class));
    }

    @Test
    @DisplayName("Получение всех фильмов должно возвращать пустой список при отсутствии фильмов")
    void test_FindAll_ShouldReturnEmptyList() throws Exception {
        // Given
        when(filmService.findAll()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(filmService, times(1)).findAll();
    }

    @Test
    @DisplayName("Получение всех фильмов должно возвращать список фильмов")
    void test_FindAll_ShouldReturnFilmsList() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);
        film.setId(1);
        List<Film> films = List.of(film);

        when(filmService.findAll()).thenReturn(films);

        // When & Then
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(FILM_NAME));

        verify(filmService, times(1)).findAll();
    }

    @Test
    @DisplayName("Обновление фильма с валидными данными должно быть успешным")
    void test_Update_ValidFilm_ShouldUpdateFilm() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);
        film.setId(1);

        Film updatedFilm = createFilm("Updated Film Name", "Updated Description", FILM_RELEASE_DATE, FILM_DURATION);
        updatedFilm.setId(1);

        when(filmService.update(any(Film.class))).thenReturn(updatedFilm);

        // When & Then
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Film Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        verify(filmService, times(1)).update(any(Film.class));
    }

    @Test
    @DisplayName("Получение фильма по ID должно возвращать фильм")
    void test_GetById_ShouldReturnFilm() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);
        film.setId(1);

        when(filmService.getById(1)).thenReturn(film);

        // When & Then
        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(FILM_NAME))
                .andExpect(jsonPath("$.description").value(FILM_DESCRIPTION));

        verify(filmService, times(1)).getById(1);
    }

    @Test
    @DisplayName("Добавление лайка должно быть успешным")
    void test_AddLike_ShouldBeSuccessful() throws Exception {
        // Given
        doNothing().when(filmService).addLike(1, 1);

        // When & Then
        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());

        verify(filmService, times(1)).addLike(1, 1);
    }

    @Test
    @DisplayName("Удаление лайка должно быть успешным")
    void test_RemoveLike_ShouldBeSuccessful() throws Exception {
        // Given
        doNothing().when(filmService).removeLike(1, 1);

        // When & Then
        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk());

        verify(filmService, times(1)).removeLike(1, 1);
    }

    @Test
    @DisplayName("Получение популярных фильмов должно возвращать список")
    void test_GetPopularFilms_ShouldReturnList() throws Exception {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);
        film.setId(1);
        List<Film> popularFilms = List.of(film);

        when(filmService.getPopularFilms(10)).thenReturn(popularFilms);

        // When & Then
        mockMvc.perform(get("/films/popular?count=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(filmService, times(1)).getPopularFilms(10);
    }

    @Test
    @DisplayName("Очистка фильмов должна быть успешной")
    void test_Clear_ShouldBeSuccessful() throws Exception {
        // Given
        doNothing().when(filmService).clear();

        // When & Then
        mockMvc.perform(delete("/films/clear"))
                .andExpect(status().isOk());

        verify(filmService, times(1)).clear();
    }
}