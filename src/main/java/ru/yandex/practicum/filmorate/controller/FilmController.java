package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final FilmMapper filmMapper;

    @Autowired
    public FilmController(FilmService filmService, FilmMapper filmMapper) {
        this.filmService = filmService;
        this.filmMapper = filmMapper;
    }

    @GetMapping
    public List<FilmDto> getAllFilms() {
        return filmService.getAllFilms().stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FilmDto getFilm(@PathVariable int id) {
        Film film = filmService.getFilmById(id);
        return filmMapper.toDto(film);
    }

    @PostMapping
    public FilmDto createFilm(@Valid @RequestBody FilmCreateDto filmCreateDto) {
        Film createdFilm = filmService.createFilm(filmCreateDto);
        return filmMapper.toDto(createdFilm);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmUpdateDto filmUpdateDto) {
        Film updatedFilm = filmService.updateFilm(filmUpdateDto);
        return filmMapper.toDto(updatedFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        List<Film> popularFilms = filmService.getPopularFilms(count);
        return popularFilms.stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }
}
