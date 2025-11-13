package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

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
}