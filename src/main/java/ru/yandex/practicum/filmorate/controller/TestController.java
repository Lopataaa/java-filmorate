package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/user")
    public User createTestUser() {
        int randomId = ThreadLocalRandom.current().nextInt(1000, 10000);

        User user = new User();
        user.setEmail("test" + randomId + "@example.com");
        user.setLogin("testuser" + randomId);
        user.setName("Test User " + randomId);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        return userStorage.create(user);
    }

    @PostMapping("/film")
    public Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        // Предполагая, что MPA с id=1 существует в базе
        film.setMpa(new Mpa(1, "G", "Нет возрастных ограничений"));
        return filmStorage.create(film);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userStorage.findAll();
    }

    @GetMapping("/films")
    public List<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    @PostMapping("/clear-db")
    public String clearDatabase() {
        try {
            jdbcTemplate.update("DELETE FROM film_genres");
            jdbcTemplate.update("DELETE FROM film_likes");
            jdbcTemplate.update("DELETE FROM friendships");
            jdbcTemplate.update("DELETE FROM films");
            jdbcTemplate.update("DELETE FROM users");

            jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
            jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");

            return "База данных очищена!";
        } catch (Exception e) {
            return "Ошибка при очистке: " + e.getMessage();
        }
    }
}