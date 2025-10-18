package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    private FilmController filmController;

    private static final String FILM_NAME = "Test Film";
    private static final String FILM_DESCRIPTION = "Test Description";
    private static final LocalDate FILM_RELEASE_DATE = LocalDate.of(2000, 1, 1);
    private static final Integer FILM_DURATION = 120;
    private static final Integer NON_EXISTENT_ID = 9999;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

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
    void test_Create_ValidFilmData_ShouldCreateFilm() {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);

        // When
        Film result = filmController.create(film);

        // Then
        assertNotNull(result.getId(), "Фильм должен получить ID");
        assertEquals(FILM_NAME, result.getName(), "Название должно совпадать");
        assertEquals(FILM_DESCRIPTION, result.getDescription(), "Описание должно совпадать");
    }

    @Test
    @DisplayName("Создание фильма с пустым названием должно вызывать исключение")
    void test_Create_EmptyFilmName_ShouldThrowValidationException() {
        // Given
        Film film = createFilm("", FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);

        // When & Then
        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Должно быть выброшено ValidationException при пустом названии");
    }

    @Test
    @DisplayName("Создание фильма с описанием длиннее 200 символов должно вызывать исключение")
    void test_Create_FilmDescriptionExceeds200Chars_ShouldThrowValidationException() {
        // Given
        String longDescription = "A".repeat(201);
        Film film = createFilm(FILM_NAME, longDescription, FILM_RELEASE_DATE, FILM_DURATION);

        // When & Then
        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Должно быть выброшено ValidationException при описании длиннее 200 символов");
    }

    @Test
    @DisplayName("Создание фильма с датой релиза до 1895 года должно вызывать исключение")
    void test_Create_FilmReleaseDateBefore1895_ShouldThrowValidationException() {
        // Given
        LocalDate earlyDate = LocalDate.of(1890, 1, 9);
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, earlyDate, FILM_DURATION);

        // When & Then
        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Должно быть выброшено ValidationException при дате релиза до 1895 года");
    }

    @Test
    @DisplayName("Создание фильма с отрицательной продолжительностью должно вызывать исключение")
    void test_Create_FilmWithNegativeDuration_ShouldThrowValidationException() {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, -103);

        // When & Then
        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Должно быть выброшено ValidationException при отрицательной продолжительности");
    }

    @Test
    @DisplayName("Обновление несуществующего фильма должно вызывать исключение")
    void test_Update_NonExistentFilm_ShouldThrowValidationException() {
        // Given
        Film film = createFilm(FILM_NAME, FILM_DESCRIPTION, FILM_RELEASE_DATE, FILM_DURATION);
        film.setId(NON_EXISTENT_ID);

        // When & Then
        assertThrows(ValidationException.class, () -> filmController.update(film),
                "Должно быть выброшено ValidationException при обновлении несуществующего фильма");
    }
}