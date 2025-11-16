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

    void delete(Integer id);

    void clear();

    void addLike(Integer filmId, Integer userId);

    void removeLike(Integer filmId, Integer userId);

    Set<Integer> getLikes(Integer filmId);

    void saveFilmGenres(Integer filmId, Set<Genre> genres);

    void updateFilmGenres(Integer filmId, Set<Genre> genres);

    Set<Genre> getFilmGenres(Integer filmId);

    List<Film> getPopularFilms(Integer count);
}