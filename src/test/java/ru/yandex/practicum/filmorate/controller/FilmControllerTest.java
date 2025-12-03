package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({FilmController.class, FilmService.class, UserService.class, FilmDbStorage.class, UserDbStorage.class, MpaDbStorage.class, GenreDbStorage.class, FilmMapper.class})
class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Test
    @DisplayName("Добавление фильма с валидными данными")
    public void addFilmValidData() {
        // Given
        FilmCreateDto filmDto = FilmCreateDto.builder().name("Test Film").description("Test Description").releaseDate(LocalDate.of(2000, 1, 1)).duration(120).mpaId(1).build();

        // When
        FilmDto response = filmController.createFilm(filmDto);

        // Then
        assertNotNull(response);
        assertEquals("Test Film", response.getName());
        assertEquals("Test Description", response.getDescription());
        assertTrue(response.getId() > 0);
    }

    @Test
    @DisplayName("Добавление фильма с невалидной датой релиза")
    public void addFilmInvalidReleaseDate() {
        // Given
        FilmCreateDto filmDto = FilmCreateDto.builder().name("Old Film").description("Very old film").releaseDate(LocalDate.of(1890, 1, 1)).duration(90).mpaId(1).build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> filmController.createFilm(filmDto));
    }

    @Test
    @DisplayName("Обновление существующего фильма")
    public void updateFilmExistingFilm() {
        FilmCreateDto createDto = FilmCreateDto.builder().name("Original").description("Original desc").releaseDate(LocalDate.of(2000, 1, 1)).duration(120).mpaId(1).build();

        FilmDto createdFilm = filmController.createFilm(createDto);
        assertNotNull(createdFilm);

        FilmUpdateDto updateDto = FilmUpdateDto.builder().id(createdFilm.getId()).name("Updated").description("Updated desc").releaseDate(LocalDate.of(2001, 1, 1)).duration(150).mpaId(2)  // новый MPA
                .build();

        // When
        FilmDto updatedFilm = filmController.updateFilm(updateDto);

        // Then
        assertNotNull(updatedFilm);
        assertEquals("Updated", updatedFilm.getName());
        assertEquals("Updated desc", updatedFilm.getDescription());
        assertEquals(150, updatedFilm.getDuration());
    }

    @Test
    @DisplayName("Обновление несуществующего фильма")
    public void updateFilmNonExistingFilm() {
        // Given
        FilmUpdateDto updateDto = FilmUpdateDto.builder().id(999)  // int
                .name("Non Existing").description("Description").releaseDate(LocalDate.of(2000, 1, 1)).duration(120).mpaId(1).build();

        // When & Then
        assertThrows(Exception.class, () -> filmController.updateFilm(updateDto));
    }

    @Test
    @DisplayName("Получение всех фильмов из пустого списка")
    public void getAllFilmsEmptyList() {
        // When
        List<FilmDto> films = filmController.getAllFilms();

        // Then
        assertNotNull(films);
        assertTrue(films.isEmpty());
    }

    @Test
    @DisplayName("Получение всех фильмов с данными")
    public void getAllFilmsWithData() {
        // Given
        FilmCreateDto film1 = FilmCreateDto.builder().name("Film 1").description("Desc 1").releaseDate(LocalDate.of(2000, 1, 1)).duration(120).mpaId(1).build();

        FilmCreateDto film2 = FilmCreateDto.builder().name("Film 2").description("Desc 2").releaseDate(LocalDate.of(2001, 1, 1)).duration(150).mpaId(1).build();

        filmController.createFilm(film1);
        filmController.createFilm(film2);

        // When
        List<FilmDto> films = filmController.getAllFilms();

        // Then
        assertEquals(2, films.size());
    }

    @Test
    @DisplayName("Добавление нескольких фильмов и проверка уникальности ID")
    public void addMultipleFilmsCheckIds() {
        // Given
        FilmCreateDto film1 = FilmCreateDto.builder().name("Film 1").description("Desc 1").releaseDate(LocalDate.of(2000, 1, 1)).duration(120).mpaId(1).build();

        FilmCreateDto film2 = FilmCreateDto.builder().name("Film 2").description("Desc 2").releaseDate(LocalDate.of(2001, 1, 1)).duration(150).mpaId(1).build();

        FilmCreateDto film3 = FilmCreateDto.builder().name("Film 3").description("Desc 3").releaseDate(LocalDate.of(2002, 1, 1)).duration(180).mpaId(1).build();

        // When
        FilmDto result1 = filmController.createFilm(film1);
        FilmDto result2 = filmController.createFilm(film2);
        FilmDto result3 = filmController.createFilm(film3);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);

        assertNotEquals(result1.getId(), result2.getId());
        assertNotEquals(result2.getId(), result3.getId());
        assertNotEquals(result1.getId(), result3.getId());
    }

    @Test
    @DisplayName("Получение фильма по ID")
    public void getFilmById() {
        // Given
        FilmCreateDto createDto = FilmCreateDto.builder().name("Test Film").description("Test Description").releaseDate(LocalDate.of(2000, 1, 1)).duration(120).mpaId(1).build();

        FilmDto createdFilm = filmController.createFilm(createDto);

        // When
        FilmDto foundFilm = filmController.getFilm(createdFilm.getId());

        // Then
        assertNotNull(foundFilm);
        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Test Film", foundFilm.getName());
    }

}