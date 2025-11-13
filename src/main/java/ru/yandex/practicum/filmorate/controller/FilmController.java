package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<List<Film>> findAll() {
        log.info("GET /films - получение списка всех фильмов");
        List<Film> films = filmService.findAll();
        return ResponseEntity.ok(films);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getById(@PathVariable Integer id) {
        log.info("GET /films/{} - получение фильма по ID", id);
        Film film = filmService.getById(id);
        return ResponseEntity.ok(film);
    }

    @PostMapping
    public ResponseEntity<Film> create(@RequestBody Film film) {
        log.info("POST /films - создание нового фильма: {}", film.getName());
        Film createdFilm = filmService.create(film);
        log.info("POST /films - фильм успешно создан с ID: {}", createdFilm.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFilm);
    }

    @PutMapping
    public ResponseEntity<Film> update(@RequestBody Film film) {
        log.info("PUT /films - обновление фильма с ID: {}", film.getId());
        Film updatedFilm = filmService.update(film);
        log.info("PUT /films - фильм с ID {} успешно обновлен", updatedFilm.getId());
        return ResponseEntity.ok(updatedFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("PUT /films/{}/like/{} - добавление лайка", id, userId);
        filmService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("DELETE /films/{}/like/{} - удаление лайка", id, userId);
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopularFilms(
            @RequestParam(defaultValue = "10") Integer count) {
        log.info("GET /films/popular - получение {} популярных фильмов", count);
        List<Film> popularFilms = filmService.getPopularFilms(count);
        return ResponseEntity.ok(popularFilms);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clear() {
        log.info("DELETE /films/clear - очистка всех фильмов");
        filmService.clear();
        return ResponseEntity.ok().build();
    }
}