package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextFilmId = 1;

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public ResponseEntity<List<Film>> findAll() {
        log.info("GET /films - получение списка всех фильмов. Количество фильмов: {}", films.size());
        List<Film> filmList = new ArrayList<>(films.values());
        return ResponseEntity.ok(filmList);
    }

    @PostMapping
    public ResponseEntity<Film> create(@RequestBody Film film) {
        log.info("POST /films - попытка создания нового фильма: {}", film);

        validateFilm(film);
        film.setId(nextFilmId++);
        films.put(film.getId(), film);

        log.info("POST /films - фильм успешно создан с ID: {}", film.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(film);
    }

    @PutMapping
    public ResponseEntity<Film> update(@RequestBody Film film) {
        log.info("PUT /films - попытка обновления фильма: {}", film);

        if (film.getId() == null || !films.containsKey(film.getId())) {
            String errorMessage = "Фильм с id=" + film.getId() + " не найден";
            log.error("PUT /films - ошибка: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

        validateFilm(film);
        films.put(film.getId(), film);

        log.info("PUT /films - фильм с ID {} успешно обновлен", film.getId());
        return ResponseEntity.ok(film);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            String errorMessage = "Название фильма не может быть пустым";
            log.warn("Валидация фильма failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            String errorMessage = "Описание не может превышать 200 символов";
            log.warn("Валидация фильма failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (film.getReleaseDate() == null) {
            String errorMessage = "Дата релиза должна быть указана";
            log.warn("Валидация фильма failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            String errorMessage = "Дата релиза не может быть раньше 28 декабря 1895 года";
            log.warn("Валидация фильма failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            String errorMessage = "Продолжительность фильма должна быть положительным числом";
            log.warn("Валидация фильма failed: {}", errorMessage);
            throw new ValidationException(errorMessage);
        }

        log.debug("Валидация фильма пройдена успешно");
    }

    public void clear() {
        films.clear();
        nextFilmId = 1;
        log.debug("Film storage cleared");
    }
}