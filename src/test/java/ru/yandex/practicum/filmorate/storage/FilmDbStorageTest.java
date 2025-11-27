package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan(basePackages = {"ru.yandex.practicum.filmorate.storage.db"})
@Sql(scripts = {"/schema.sql", "/test-data.sql"})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    void testFindFilmById() {
        // When
        Optional<Film> filmOptional = filmStorage.findById(1);

        // Then
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getId()).isEqualTo(1);
                    assertThat(film.getName()).isEqualTo("Film 1");
                    assertThat(film.getMpa().getId()).isEqualTo(1);
                });
    }

    @Test
    void testFindAllFilms() {
        // When
        List<Film> films = filmStorage.findAll();

        // Then
        assertThat(films).hasSize(3);
        assertThat(films).extracting(Film::getName)
                .containsExactly("Film 1", "Film 2", "Film 3");
    }

    @Test
    void testCreateFilm() {
        // Given
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2023, 1, 1));
        newFilm.setDuration(120);

        Mpa mpa = new Mpa(1, "G", "Нет возрастных ограничений");
        newFilm.setMpa(mpa);

        // When
        Film createdFilm = filmStorage.create(newFilm);

        // Then
        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();
    }

    @Test
    void testUpdateFilm() {
        // Given
        Film filmToUpdate = filmStorage.findById(1).get();
        filmToUpdate.setName("Updated Film");
        filmToUpdate.setDescription("Updated Description");

        // When
        Film updatedFilm = filmStorage.update(filmToUpdate);

        // Then
        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");

        // Verify in database
        Optional<Film> foundFilm = filmStorage.findById(1);
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getName()).isEqualTo("Updated Film");
    }

    @Test
    void testExistsById() {
        // Then
        assertThat(filmStorage.existsById(1)).isTrue();
        assertThat(filmStorage.existsById(999)).isFalse();
    }

    @Test
    void testDeleteFilm() {
        // When
        filmStorage.delete(1);

        // Then
        assertThat(filmStorage.existsById(1)).isFalse();
    }
}