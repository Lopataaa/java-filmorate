package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({FilmController.class, FilmService.class, UserService.class, FilmDbStorage.class, UserDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Test
    @DisplayName("Добавление фильма с валидными данными")
    public void addFilmValidData() {
        // Given
        Film film = createValidFilm("Test Film", "Test Description", LocalDate.of(2000, 1, 1), 120);

        // When
        ResponseEntity<Object> response = filmController.addFilm(film);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        Film createdFilm = (Film) response.getBody();
        assertEquals("Test Film", createdFilm.getName());
    }

    @Test
    @DisplayName("Добавление фильма с невалидной датой релиза")
    public void addFilmInvalidReleaseDate() {
        // Given
        Film film = createValidFilm("Old Film", "Very old film", LocalDate.of(1890, 1, 1), 90);

        // When
        ResponseEntity<Object> response = filmController.addFilm(film);

        // Then
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Обновление существующего фильма")
    public void updateFilmExistingFilm() {
        // Given
        Film film = createValidFilm("Original", "Original desc", LocalDate.of(2000, 1, 1), 120);
        ResponseEntity<Object> createResponse = filmController.addFilm(film);
        Film createdFilm = (Film) createResponse.getBody();

        Film updatedFilm = createValidFilm("Updated", "Updated desc", LocalDate.of(2001, 1, 1), 150);
        assertNotNull(createdFilm);
        updatedFilm.setId(createdFilm.getId());

        // When
        ResponseEntity<Object> response = filmController.updateFilm(updatedFilm);

        // Then
        assertEquals(200, response.getStatusCode().value());
        Film resultFilm = (Film) response.getBody();
        assertNotNull(resultFilm);
        assertEquals("Updated", resultFilm.getName());
    }

    @Test
    @DisplayName("Обновление несуществующего фильма")
    public void updateFilmNonExistingFilm() {
        // Given
        Film film = createValidFilm("Non Existing", "Description", LocalDate.of(2000, 1, 1), 120);
        film.setId(999);

        // When & Then
        assertThrows(RuntimeException.class, () -> filmController.updateFilm(film));
    }

    @Test
    @DisplayName("Получение всех фильмов из пустого списка")
    public void getAllFilmsEmptyList() {
        // When
        List<Film> films = filmController.getAllFilms();

        // Then
        assertNotNull(films);
        assertTrue(films.isEmpty());
    }

    @Test
    @DisplayName("Получение всех фильмов с данными")
    public void getAllFilmsWithData() {
        // Given
        Film film1 = createValidFilm("Film 1", "Desc 1", LocalDate.of(2000, 1, 1), 120);
        Film film2 = createValidFilm("Film 2", "Desc 2", LocalDate.of(2001, 1, 1), 150);

        filmController.addFilm(film1);
        filmController.addFilm(film2);

        // When
        List<Film> films = filmController.getAllFilms();

        // Then
        assertEquals(2, films.size());
    }

    @Test
    @DisplayName("Добавление нескольких фильмов и проверка уникальности ID")
    public void addMultipleFilmsCheckIds() {
        // Given
        Film film1 = createValidFilm("Film 1", "Desc 1", LocalDate.of(2000, 1, 1), 120);
        Film film2 = createValidFilm("Film 2", "Desc 2", LocalDate.of(2001, 1, 1), 150);
        Film film3 = createValidFilm("Film 3", "Desc 3", LocalDate.of(2002, 1, 1), 180);

        // When
        ResponseEntity<Object> response1 = filmController.addFilm(film1);
        ResponseEntity<Object> response2 = filmController.addFilm(film2);
        ResponseEntity<Object> response3 = filmController.addFilm(film3);

        Film result1 = (Film) response1.getBody();
        Film result2 = (Film) response2.getBody();
        Film result3 = (Film) response3.getBody();

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);

        assertNotEquals(result1.getId(), result2.getId());
        assertNotEquals(result2.getId(), result3.getId());
    }

    private Film createValidFilm(String name, String description, LocalDate releaseDate, int duration) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        return film;
    }
}