package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    public List<Film> findAll() {
        log.debug("Получение списка всех фильмов");
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        log.debug("Создание фильма: {}", film.getName());
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        log.debug("Обновление фильма с ID: {}", film.getId());
        validateFilm(film);
        if (!filmStorage.existsById(film.getId())) {
            log.warn("Попытка обновления несуществующего фильма с ID: {}", film.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Фильм с id=" + film.getId() + " не найден");
        }
        return filmStorage.update(film);
    }

    public Film getById(Integer id) {
        log.debug("Поиск фильма по ID: {}", id);
        return filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с ID={} не найден", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Фильм с id=" + id + " не найден");
                });
    }

    public void validateUserExists(Integer userId) {
        log.debug("Проверка существования пользователя с ID: {}", userId);
        if (!userStorage.existsById(userId)) {
            log.warn("Пользователь с ID={} не найден", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Пользователь с id=" + userId + " не найден");
        }
    }

    public void addLike(Integer filmId, Integer userId) {
        log.debug("Добавление лайка: фильм {}, пользователь {}", filmId, userId);
        Film film = getById(filmId);
        validateUserExists(userId);

        film.addLike(userId);
        filmStorage.update(film);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        log.debug("Удаление лайка: фильм {}, пользователь {}", filmId, userId);
        Film film = getById(filmId);
        validateUserExists(userId);

        film.removeLike(userId);
        filmStorage.update(film);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;
        log.debug("Получение {} популярных фильмов", limit);

        List<Film> popularFilms = filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        log.debug("Найдено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    private void validateFilm(Film film) {
        log.debug("Валидация фильма: {}", film.getName());

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Описание фильма слишком длинное: {} символов", film.getDescription().length());
            throw new ValidationException("Описание не может превышать 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.warn("Дата релиза не указана для фильма: {}", film.getName());
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.warn("Дата релиза слишком ранняя: {} для фильма: {}", film.getReleaseDate(), film.getName());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Невалидная продолжительность фильма: {} для фильма: {}", film.getDuration(), film.getName());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        log.debug("Валидация фильма пройдена успешно: {}", film.getName());
    }

    public void clear() {
        log.info("Очистка данных фильмов");
        filmStorage.clear();
    }
}