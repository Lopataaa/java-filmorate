package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
import java.util.stream.Collectors;

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

    public Film createFilm(Map<String, Object> filmData) {
        Film film = new Film();
        film.setName((String) filmData.get("name"));
        film.setDescription((String) filmData.get("description"));

        if (filmData.get("releaseDate") != null) {
            film.setReleaseDate(LocalDate.parse((String) filmData.get("releaseDate")));
        }

        if (filmData.get("duration") != null) {
            film.setDuration(((Number) filmData.get("duration")).intValue());
        }

        if (filmData.get("mpa") != null) {
            Map<String, Object> mpaMap = (Map<String, Object>) filmData.get("mpa");
            if (mpaMap.get("id") != null) {
                Mpa mpa = new Mpa();
                mpa.setId(((Number) mpaMap.get("id")).intValue());
                film.setMpa(mpa);
            }
        }

        if (filmData.get("genres") != null) {
            List<Map<String, Object>> genresList = (List<Map<String, Object>>) filmData.get("genres");
            Set<Genre> genres = new HashSet<>();
            for (Map<String, Object> genreMap : genresList) {
                if (genreMap.get("id") != null) {
                    Genre genre = new Genre();
                    genre.setId(((Number) genreMap.get("id")).intValue());
                    genres.add(genre);
                }
            }
            film.setGenres(genres);
        }

        return createFilmFromModel(film);
    }

    public Film createFilm(FilmCreateDto dto) {
        log.debug("Создание нового фильма: {}", dto.getName());

        Film film = convertToFilm(dto);

        return createFilmFromModel(film);
    }

    public Film updateFilm(FilmUpdateDto dto) {
        log.debug("Обновление фильма с id {}", dto.getId());

        Film film = convertToFilm(dto);

        return updateFilmFromModel(film);
    }

    private Film convertToFilm(FilmCreateDto dto) {
        Film film = new Film();
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getMpaId() != null) {
            Mpa mpa = new Mpa();
            mpa.setId(dto.getMpaId());
            film.setMpa(mpa);
        }

        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>();
            for (Integer genreId : dto.getGenreIds()) {
                Genre genre = new Genre();
                genre.setId(genreId);
                genres.add(genre);
            }
            film.setGenres(genres);
        }

        return film;
    }

    private Film convertToFilm(FilmUpdateDto dto) {
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getMpaId() != null) {
            Mpa mpa = new Mpa();
            mpa.setId(dto.getMpaId());
            film.setMpa(mpa);
        }

        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>();
            for (Integer genreId : dto.getGenreIds()) {
                Genre genre = new Genre();
                genre.setId(genreId);
                genres.add(genre);
            }
            film.setGenres(genres);
        }

        return film;
    }

    private Film createFilmFromModel(Film film) {
        validateFilm(film);

        if (film.getMpa() == null || film.getMpa().getId() <= 0) {
            throw new ValidationException("MPA рейтинг обязателен и должен быть указан");
        }

        Mpa mpa = mpaStorage.getMpaRatingById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден"));
        film.setMpa(mpa);

        Set<Genre> validGenres = new LinkedHashSet<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                Genre fullGenre = genreStorage.getGenreById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден"));
                validGenres.add(fullGenre);
            }
            List<Genre> sortedGenres = validGenres.stream()
                    .sorted(Comparator.comparingInt(Genre::getId))
                    .collect(Collectors.toList());
            film.setGenres(new LinkedHashSet<>(sortedGenres));
        } else {
            film.setGenres(new LinkedHashSet<>());
        }

        Film createdFilm = filmStorage.create(film);
        log.info("Создан новый фильм: '{}' (id: {})", createdFilm.getName(), createdFilm.getId());
        return createdFilm;
    }

    private Film updateFilmFromModel(Film film) {
        Film existingFilm = filmStorage.getById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));

        validateFilm(film);

        if (film.getMpa() == null || film.getMpa().getId() <= 0) {
            throw new ValidationException("MPA рейтинг обязателен и должен быть указан");
        }

        Mpa mpa = mpaStorage.getMpaRatingById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден"));
        film.setMpa(mpa);

        Set<Genre> validGenres = new LinkedHashSet<>();
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                Genre fullGenre = genreStorage.getGenreById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден"));
                validGenres.add(fullGenre);
            }
            List<Genre> sortedGenres = validGenres.stream()
                    .sorted(Comparator.comparingInt(Genre::getId))
                    .collect(Collectors.toList());
            film.setGenres(new LinkedHashSet<>(sortedGenres));
        } else {
            film.setGenres(new LinkedHashSet<>());
        }

        Film updatedFilm = filmStorage.update(film);
        log.info("Обновлен фильм: '{}' (id: {})", updatedFilm.getName(), updatedFilm.getId());
        return updatedFilm;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание не должно превышать 200 символов");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }

    public List<Film> getAllFilms() {
        log.debug("Получение списка всех фильмов");
        List<Film> films = filmStorage.getAll();
        log.debug("Получено {} фильмов", films.size());
        return films;
    }

    public Film getFilmById(int id) {
        log.debug("Поиск фильма с id {}", id);

        Film film = filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));

        log.debug("Найден фильм: {} (id: {})", film.getName(), film.getId());
        return film;
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