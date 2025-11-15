package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Slf4j
@Component
public class InMemoryGenreStorage implements GenreStorage {
    private final Map<Integer, Genre> genres = new HashMap<>();

    public InMemoryGenreStorage() {
        genres.put(1, new Genre(1, "Комедия"));
        genres.put(2, new Genre(2, "Драма"));
        genres.put(3, new Genre(3, "Мультфильм"));
        genres.put(4, new Genre(4, "Триллер"));
        genres.put(5, new Genre(5, "Документальный"));
        genres.put(6, new Genre(6, "Боевик"));
        log.info("Инициализировано жанров: {}", genres.size());
    }

    @Override
    public List<Genre> findAll() {
        log.debug("Получение всех жанров");
        return new ArrayList<>(genres.values());
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        log.debug("Поиск жанра по ID: {}", id);
        return Optional.ofNullable(genres.get(id));
    }

    @Override
    public boolean existsById(Integer id) {
        log.debug("Проверка существования жанра с ID: {}", id);
        return genres.containsKey(id);
    }
}