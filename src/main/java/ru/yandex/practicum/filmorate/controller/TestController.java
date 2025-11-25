package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @PostMapping("/user")
    public User createTestUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
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
}