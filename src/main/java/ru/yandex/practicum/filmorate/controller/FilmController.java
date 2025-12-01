package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController extends BaseController<Film> {
    private final FilmService filmService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Object> addFilm(@Valid @RequestBody Film film) {
        return addEntity(film);
    }

    @PutMapping
    public ResponseEntity<Object> updateFilm(@Valid @RequestBody Film film) {
        return updateEntity(film);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getFilm(@PathVariable int id) {
        try {
            Film film = filmService.getFilmById(id);
            return ResponseEntity.ok(film);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Object> addLike(@PathVariable int id, @PathVariable int userId) {
        try {
            filmService.addLike(id, userId);
            return ResponseEntity.ok(Map.of("message", "Лайк успешно добавлен", "filmId", id, "userId", userId));
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Object> removeLike(@PathVariable int id, @PathVariable int userId) {
        try {
            filmService.removeLike(id, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(name = "count", defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }

    @Override
    protected ResponseEntity<Object> addEntity(Film film) {
        try {
            validateEntity(film);
            Film createdFilm = filmService.createFilm(film);
            return ResponseEntity.ok(createdFilm);
        } catch (ValidationException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    protected ResponseEntity<Object> updateEntity(Film film) {

        validateEntity(film);

        Film updatedFilm = filmService.updateFilm(film);
        return ResponseEntity.ok(updatedFilm);
    }

    @Override
    protected void validateEntity(Film film) throws ValidationException {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}