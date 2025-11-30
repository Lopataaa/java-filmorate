package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    public void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        testFilm.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        testFilm.setMpa(mpa);
    }

    @Test
    @DisplayName("Создание фильма в базе данных")
    public void testCreateFilm() {
        // When
        Film createdFilm = filmStorage.create(testFilm);

        // Then
        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isPositive();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getDuration()).isEqualTo(120);
    }

    @Test
    @DisplayName("Получение фильма по ID")
    public void testGetFilmById() {
        // Given
        Film createdFilm = filmStorage.create(testFilm);

        // When
        Optional<Film> foundFilm = filmStorage.getById(createdFilm.getId());

        // Then
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getName()).isEqualTo("Test Film");
    }

    @Test
    @DisplayName("Получение всех фильмов")
    public void testGetAllFilms() {
        // Given
        filmStorage.create(testFilm);

        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another Description");
        anotherFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        anotherFilm.setDuration(90);
        filmStorage.create(anotherFilm);

        // When
        List<Film> films = filmStorage.getAll();

        // Then
        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName).containsExactlyInAnyOrder("Test Film", "Another Film");
    }

    @Test
    @DisplayName("Обновление фильма")
    public void testUpdateFilm() {
        // Given
        Film createdFilm = filmStorage.create(testFilm);
        createdFilm.setName("Updated Film");
        createdFilm.setDuration(150);

        // When
        Film updatedFilm = filmStorage.update(createdFilm);

        // Then
        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDuration()).isEqualTo(150);

        Optional<Film> foundFilm = filmStorage.getById(createdFilm.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getName()).isEqualTo("Updated Film");
    }

    @Test
    @DisplayName("Проверка существования фильма")
    public void testFilmExists() {
        // Given
        Film createdFilm = filmStorage.create(testFilm);

        // When
        boolean exists = filmStorage.exists(createdFilm.getId());
        boolean notExists = filmStorage.exists(999);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Удаление фильма")
    public void testDeleteFilm() {
        // Given
        Film createdFilm = filmStorage.create(testFilm);

        // When
        boolean existsBefore = filmStorage.exists(createdFilm.getId());
        filmStorage.delete(createdFilm.getId());
        boolean existsAfter = filmStorage.exists(createdFilm.getId());

        // Then
        assertThat(existsBefore).isTrue();
        assertThat(existsAfter).isFalse();
    }

    @Test
    @DisplayName("Получение всех рейтингов MPA")
    public void testGetAllMpaRatings() {
        // When
        List<Mpa> mpas = filmStorage.getAllMpaRatings();

        // Then
        assertThat(mpas).isNotEmpty();
        assertThat(mpas).extracting(Mpa::getName).contains("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    @DisplayName("Получение всех жанров")
    public void testGetAllGenres() {
        // When
        List<Genre> genres = filmStorage.getAllGenres();

        // Then
        assertThat(genres).isNotEmpty();
        assertThat(genres).extracting(Genre::getName).contains("Комедия", "Драма", "Боевик");
    }
}