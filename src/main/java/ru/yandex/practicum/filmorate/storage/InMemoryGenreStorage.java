package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryGenreStorage implements GenreStorage {
    private final Map<Integer, Genre> genreMap = new HashMap<>();

    public InMemoryGenreStorage() {
        genreMap.put(1, new Genre(1, "Комедия"));
        genreMap.put(2, new Genre(2, "Драма"));
        genreMap.put(3, new Genre(3, "Мультфильм"));
        genreMap.put(4, new Genre(4, "Триллер"));
        genreMap.put(5, new Genre(5, "Документальный"));
        genreMap.put(6, new Genre(6, "Боевик"));
    }

    @Override
    public List<Genre> findAll() {
        return new ArrayList<>(genreMap.values());
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        return Optional.ofNullable(genreMap.get(id));
    }

    @Override
    public boolean existsById(Integer id) {
        return genreMap.containsKey(id);
    }
}