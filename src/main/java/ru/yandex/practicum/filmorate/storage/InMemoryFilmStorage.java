package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new ConcurrentHashMap<>();
    private int nextId = 1;

    @Override
    public List<Film> findAll() {
        log.info("Получение списка всех фильмов. Количество: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Фильм успешно создан с ID: {}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new RuntimeException("Фильм с id=" + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        log.info("Фильм с ID {} успешно обновлен", film.getId());
        return film;
    }

    @Override
    public Optional<Film> findById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public boolean existsById(Integer id) {
        return films.containsKey(id);
    }

    @Override
    public void clear() {
        log.info("Очистка хранилища фильмов");
        films.clear();
        nextId = 1;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.addLike(userId);
            log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
        }
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.removeLike(userId);
            log.debug("Пользователь {} удалил лайк с фильма {}", userId, filmId);
        }
    }

    @Override
    public Set<Integer> getLikes(Integer filmId) {
        Film film = films.get(filmId);
        return film != null ? film.getLikes() : new HashSet<>();
    }

    @Override
    public void saveFilmGenres(Integer filmId, Set<Genre> genres) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getGenres().addAll(genres);
            log.debug("Добавлены жанры к фильму {}: {}", filmId, genres);
        }
    }

    @Override
    public void updateFilmGenres(Integer filmId, Set<Genre> genres) {
        Film film = films.get(filmId);
        if (film != null) {
            film.getGenres().clear();
            film.getGenres().addAll(genres);
            log.debug("Обновлены жанры фильма {}: {}", filmId, genres);
        }
    }

    @Override
    public Set<Genre> getFilmGenres(Integer filmId) {
        Film film = films.get(filmId);
        return film != null ? film.getGenres() : new HashSet<>();
    }
}