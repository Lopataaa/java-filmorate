package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Slf4j
@Component
public class InMemoryMpaStorage implements MpaStorage {
    private final Map<Integer, Mpa> mpaRatings = new HashMap<>();

    public InMemoryMpaStorage() {
        mpaRatings.put(1, new Mpa(1, "G", "у фильма нет возрастных ограничений"));
        mpaRatings.put(2, new Mpa(2, "PG", "детям рекомендуется смотреть фильм с родителями"));
        mpaRatings.put(3, new Mpa(3, "PG-13", "детям до 13 лет просмотр не желателен"));
        mpaRatings.put(4, new Mpa(4, "R", "лицам до 17 лет просматривать фильм можно только в присутствии взрослого"));
        mpaRatings.put(5, new Mpa(5, "NC-17", "лицам до 18 лет просмотр запрещён"));
        log.info("Инициализировано MPA рейтингов: {}", mpaRatings.size());
    }

    @Override
    public List<Mpa> findAll() {
        log.debug("Получение всех MPA рейтингов");
        return new ArrayList<>(mpaRatings.values());
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        log.debug("Поиск MPA рейтинга по ID: {}", id);
        return Optional.ofNullable(mpaRatings.get(id));
    }

    @Override
    public boolean existsById(Integer id) {
        log.debug("Проверка существования MPA рейтинга с ID: {}", id);
        return mpaRatings.containsKey(id);
    }
}