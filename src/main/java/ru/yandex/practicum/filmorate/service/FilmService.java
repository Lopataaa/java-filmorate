package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;

import java.time.LocalDate;
import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService, MpaDbStorage mpaStorage,
                       GenreDbStorage genreStorage, JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Film> getAllFilms() {
        log.debug("Получение списка всех фильмов");
        List<Film> films = filmStorage.getAll();
        log.debug("Получено {} фильмов", films.size());
        return films;
    }

    public Film getFilmById(int id) {
        log.debug("Поиск фильма с id {}", id);
        Film film = filmStorage.getById(id).orElseThrow(() -> {
            log.error("Фильм с id {} не найден", id);
            return new IllegalArgumentException("Фильм с id " + id + " не найден");
        });
        return film;
    }

    public Film createFilm(FilmCreateDto dto) {
        log.debug("Создание нового фильма: {}", dto.getName());

        if (dto.getReleaseDate() != null &&
                dto.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        Mpa mpa = null;
        if (dto.getMpaId() != null && dto.getMpaId() > 0) {
            mpa = mpaStorage.getMpaRatingById(dto.getMpaId())
                    .orElseThrow(() -> new ValidationException("Недопустимый рейтинг MPA"));
        }

        Set<Genre> genres = new LinkedHashSet<>();
        if (dto.getGenreIds() != null) {
            for (Integer id : dto.getGenreIds()) {
                if (id == null || id <= 0) continue;
                Genre g = genreStorage.getGenreById(id)
                        .orElseThrow(() -> new ValidationException("Недопустимый ID жанра: " + id));
                genres.add(g);
            }
        }

        Film film = new Film();
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());
        film.setMpa(mpa);
        film.setGenres(genres);

        Film saved = filmStorage.create(film);
        log.info("Создан новый фильм: '{}' (id: {})", saved.getName(), saved.getId());
        return saved;
    }

    public Film updateFilm(FilmUpdateDto dto) {
        log.debug("Обновление фильма с id {}", dto.getId());

        Film existing = filmStorage.getById(dto.getId())
                .orElseThrow(() -> new ValidationException("Фильм с id " + dto.getId() + " не найден"));

        if (dto.getReleaseDate() != null &&
                dto.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        Mpa mpa = null;
        if (dto.getMpaId() != null && dto.getMpaId() > 0) {
            mpa = mpaStorage.getMpaRatingById(dto.getMpaId())
                    .orElseThrow(() -> new ValidationException("Недопустимый рейтинг MPA"));
        }

        Set<Genre> genres = new LinkedHashSet<>();
        if (dto.getGenreIds() != null) {
            for (Integer id : dto.getGenreIds()) {
                if (id == null || id <= 0) continue;
                Genre g = genreStorage.getGenreById(id)
                        .orElseThrow(() -> new ValidationException("Недопустимый ID жанра: " + id));
                genres.add(g);
            }
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setReleaseDate(dto.getReleaseDate());
        existing.setDuration(dto.getDuration());
        existing.setMpa(mpa);
        existing.setGenres(genres);

        Film updated = filmStorage.update(existing);
        log.info("Обновлен фильм: '{}' (id: {})", updated.getName(), updated.getId());
        return updated;
    }

    public void addLike(int filmId, int userId) {
        log.debug("Добавление лайка: пользователь {} ставит лайк фильму {}", userId, filmId);

        userService.getUserById(userId);
        getFilmById(filmId);

        if (filmStorage instanceof FilmDbStorage) {
            FilmDbStorage filmDbStorage = (FilmDbStorage) filmStorage;
            filmDbStorage.addLike(filmId, userId);
        }

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        log.debug("Удаление лайка: пользователь {} удаляет лайк с фильма {}", userId, filmId);

        userService.getUserById(userId);
        getFilmById(filmId);

        if (filmStorage instanceof FilmDbStorage) {
            FilmDbStorage filmDbStorage = (FilmDbStorage) filmStorage;
            filmDbStorage.removeLike(filmId, userId);
        }

        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        log.debug("Получение {} популярных фильмов", count);

        if (filmStorage instanceof FilmDbStorage) {
            FilmDbStorage filmDbStorage = (FilmDbStorage) filmStorage;
            String sql = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description, " +
                    "COUNT(l.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                    "LEFT JOIN likes l ON f.id = l.film_id " +
                    "GROUP BY f.id, m.id, m.name, m.description " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";

            List<Film> films = filmDbStorage.getJdbcTemplate().query(sql, (rs, rowNum) -> {
                Film film = filmDbStorage.mapFilm(rs, rowNum);
                return film;
            }, count);

            if (!films.isEmpty()) {
                filmDbStorage.loadGenresForFilms(films);
            }

            log.info("Возвращено {} популярных фильмов", films.size());
            return films;
        }

        log.warn("FilmStorage не является FilmDbStorage");
        return List.of();
    }

    public boolean filmExists(int id) {
        log.debug("Проверка существования фильма с id {}", id);
        return filmStorage.exists(id);
    }

    public List<Mpa> getAllMpaRatings() {
        log.debug("Получение списка всех рейтингов MPA");
        return mpaStorage.getAllMpaRatings();
    }

    public Mpa getMpaRatingById(int id) {
        log.debug("Получение рейтинга MPA с id {}", id);
        return mpaStorage.getMpaRatingById(id)
                .orElseThrow(() -> new IllegalArgumentException("Рейтинг MPA с id " + id + " не найден"));
    }

    public List<Genre> getAllGenres() {
        log.debug("Получение списка всех жанров");
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(int id) {
        log.debug("Получение жанра с id {}", id);
        return genreStorage.getGenreById(id)
                .orElseThrow(() -> new IllegalArgumentException("Жанр с id " + id + " не найден"));
    }
}