package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
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
        log.debug("GET /films - найдено {} фильмов", films.size());
        return ResponseEntity.ok(films);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getById(@PathVariable Integer id) {
        log.info("GET /films/{} - получение фильма по ID", id);
        Film film = filmService.getById(id);
        log.debug("GET /films/{} - найден фильм: '{}'", id, film.getName());
        return ResponseEntity.ok(film);
    }

    @PostMapping
    public ResponseEntity<Film> create(@RequestBody Film film) {
        log.info("POST /films - попытка создания нового фильма: {}", film.getName());
        log.debug("POST /films - детали создаваемого фильма: {}", film);

        Film createdFilm = filmService.create(film);

        log.info("POST /films - фильм успешно создан с ID: {}", createdFilm.getId());
        log.debug("POST /films - созданный фильм: {}", createdFilm);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFilm);
    }

    @PutMapping
    public ResponseEntity<Film> update(@RequestBody Film film) {
        log.info("PUT /films - попытка обновления фильма с ID: {}", film.getId());
        log.debug("PUT /films - обновляемые данные: {}", film);

        Film updatedFilm = filmService.update(film);

        log.info("PUT /films - фильм с ID {} успешно обновлен", updatedFilm.getId());
        log.debug("PUT /films - обновленный фильм: {}", updatedFilm);
        return ResponseEntity.ok(updatedFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("PUT /films/{}/like/{} - добавление лайка", id, userId);
        filmService.addLike(id, userId);
        log.debug("PUT /films/{}/like/{} - лайк успешно добавлен", id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("DELETE /films/{}/like/{} - удаление лайка", id, userId);
        filmService.removeLike(id, userId);
        log.debug("DELETE /films/{}/like/{} - лайк успешно удален", id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopularFilms(
            @RequestParam(defaultValue = "10") Integer count) {
        log.info("GET /films/popular - получение {} популярных фильмов", count);
        List<Film> popularFilms = filmService.getPopularFilms(count);
        log.debug("GET /films/popular - найдено {} популярных фильмов", popularFilms.size());
        return ResponseEntity.ok(popularFilms);
    }

    @GetMapping("/mpa")
    public ResponseEntity<List<Mpa>> getAllMpa() {
        log.info("GET /films/mpa - получение всех рейтингов MPA");
        List<Mpa> mpaList = filmService.getAllMpa();
        return ResponseEntity.ok(mpaList);
    }

    @GetMapping("/mpa/{id}")
    public ResponseEntity<Mpa> getMpaById(@PathVariable Integer id) {
        log.info("GET /films/mpa/{} - получение рейтинга MPA по ID", id);
        Mpa mpa = filmService.getMpaById(id);
        return ResponseEntity.ok(mpa);
    }

    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> getAllGenres() {
        log.info("GET /films/genres - получение всех жанров");
        List<Genre> genres = filmService.getAllGenres();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/genres/{id}")
    public ResponseEntity<Genre> getGenreById(@PathVariable Integer id) {
        log.info("GET /films/genres/{} - получение жанра по ID", id);
        Genre genre = filmService.getGenreById(id);
        return ResponseEntity.ok(genre);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clear() {
        log.info("DELETE /films/clear - очистка всех фильмов");
        filmService.clear();
        return ResponseEntity.ok().build();
    }
}