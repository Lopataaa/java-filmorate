package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);

    public List<Film> findAll() {
        log.debug("Получение списка всех фильмов");
        List<Film> films = filmStorage.findAll();
        films.forEach(this::loadFilmDetails);
        return films;
    }

    public Film create(Film film) {
        log.debug("Создание фильма: {}", film.getName());
        validateFilm(film);
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());

        Film createdFilm = filmStorage.create(film);
        // Сохранение жанров
        filmStorage.saveFilmGenres(createdFilm.getId(), film.getGenres());

        loadFilmDetails(createdFilm);
        return createdFilm;
    }

    public Film update(Film film) {
        log.debug("Обновление фильма с ID: {}", film.getId());
        validateFilm(film);
        validateMpa(film.getMpa());
        validateGenres(film.getGenres());

        if (!filmStorage.existsById(film.getId())) {
            log.warn("Попытка обновления несуществующего фильма с ID: {}", film.getId());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Фильм с id=" + film.getId() + " не найден");
        }

        Film updatedFilm = filmStorage.update(film);
        // Обновление жанров
        filmStorage.updateFilmGenres(updatedFilm.getId(), film.getGenres());

        loadFilmDetails(updatedFilm);
        return updatedFilm;
    }

    public Film getById(Integer id) {
        log.debug("Поиск фильма по ID: {}", id);
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с ID={} не найден", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Фильм с id=" + id + " не найден");
                });
        loadFilmDetails(film);
        return film;
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

        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        log.debug("Удаление лайка: фильм {}, пользователь {}", filmId, userId);
        Film film = getById(filmId);
        validateUserExists(userId);

        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;
        log.debug("Получение {} популярных фильмов", limit);

        List<Film> popularFilms = filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        popularFilms.forEach(this::loadFilmDetails);
        log.debug("Найдено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    public List<Mpa> getAllMpa() {
        log.debug("Получение всех рейтингов MPA");
        return mpaStorage.findAll();
    }

    public Mpa getMpaById(Integer id) {
        log.debug("Получение рейтинга MPA по ID: {}", id);
        return mpaStorage.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Рейтинг MPA с id=" + id + " не найден"));
    }

    public List<Genre> getAllGenres() {
        log.debug("Получение всех жанров");
        return genreStorage.findAll();
    }

    public Genre getGenreById(Integer id) {
        log.debug("Получение жанра по ID: {}", id);
        return genreStorage.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Жанр с id=" + id + " не найден"));
    }

    public void clear() {
        log.info("Очистка данных фильмов");
        filmStorage.clear();
    }

    private void loadFilmDetails(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            Mpa mpa = mpaStorage.findById(film.getMpa().getId()).orElse(null);
            film.setMpa(mpa);
        }

        Set<Genre> filmGenres = filmStorage.getFilmGenres(film.getId());
        film.getGenres().clear();
        film.getGenres().addAll(filmGenres);
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

    private void validateMpa(Mpa mpa) {
        if (mpa != null && mpa.getId() != null) {
            if (!mpaStorage.existsById(mpa.getId())) {
                throw new ValidationException("Рейтинг MPA с id=" + mpa.getId() + " не найден");
            }
        }
    }

    private void validateGenres(Set<Genre> genres) {
        if (genres != null) {
            for (Genre genre : genres) {
                if (genre.getId() != null && !genreStorage.existsById(genre.getId())) {
                    throw new ValidationException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }
    }
}