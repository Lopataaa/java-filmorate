package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    List<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(Integer id);

    boolean existsById(Integer id);

    void clear();

    Set<Genre> getFilmGenres(Integer id);

    void removeLike(Integer filmId, Integer userId);

    void addLike(Integer filmId, Integer userId);

    void updateFilmGenres(Integer id, Set<Genre> genres);

    void saveFilmGenres(Integer id, Set<Genre> genres);

    Set<Integer> getLikes(Integer id);
}