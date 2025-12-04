package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;

import java.util.Comparator;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    public FilmService(FilmStorage filmStorage, MpaDbStorage mpaStorage, GenreDbStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public Film getFilmById(int id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film createFilm(FilmCreateDto dto) {
        validateReleaseDate(dto.getReleaseDate());

        Mpa mpa = null;
        if (dto.getMpa() != null && dto.getMpa().getId() != null) {
            mpa = mpaStorage.getMpaRatingById(dto.getMpa().getId().intValue())
                    .orElseThrow(() -> new NotFoundException("Недопустимый рейтинг MPA"));
        }

        Set<Genre> genres = new LinkedHashSet<>();
        if (dto.getGenres() != null) {
            List<Genre> genreList = new ArrayList<>();
            for (var genreDto : dto.getGenres()) {
                if (genreDto.getId() == null) continue;
                Genre genre = genreStorage.getGenreById(genreDto.getId().intValue())
                        .orElseThrow(() -> new NotFoundException("Недопустимый ID жанра: " + genreDto.getId()));
                genreList.add(genre);
            }
            genreList.sort(Comparator.comparingInt(Genre::getId));
            genres = new LinkedHashSet<>(genreList);
        }

        Film film = new Film();
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());
        film.setMpa(mpa);
        film.setGenres(genres);

        return filmStorage.create(film);
    }

    public Film updateFilm(FilmUpdateDto dto) {
        Film existing = filmStorage.getById(dto.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + dto.getId() + " не найден"));

        validateReleaseDate(dto.getReleaseDate());

        Mpa mpa = null;
        if (dto.getMpa() != null && dto.getMpa().getId() != null) {
            mpa = mpaStorage.getMpaRatingById(dto.getMpa().getId().intValue())
                    .orElseThrow(() -> new NotFoundException("Недопустимый рейтинг MPA"));
        }

        Set<Genre> genres = new LinkedHashSet<>();
        if (dto.getGenres() != null) {
            List<Genre> genreList = new ArrayList<>();
            for (var genreDto : dto.getGenres()) {
                if (genreDto.getId() == null) continue;
                Genre genre = genreStorage.getGenreById(genreDto.getId().intValue())
                        .orElseThrow(() -> new NotFoundException("Недопустимый ID жанра: " + genreDto.getId()));
                genreList.add(genre);
            }
            genreList.sort(Comparator.comparingInt(Genre::getId));
            genres = new LinkedHashSet<>(genreList);
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setReleaseDate(dto.getReleaseDate());
        existing.setDuration(dto.getDuration());
        existing.setMpa(mpa);
        existing.setGenres(genres);

        return filmStorage.update(existing);
    }

    public void addLike(int filmId, int userId) {
        getFilmById(filmId); // проверка существования
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        getFilmById(filmId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate != null && releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    public List<Mpa> getAllMpaRatings() {
        log.debug("Получение списка всех рейтингов MPA");
        return mpaStorage.getAllMpaRatings();
    }

    public Mpa getMpaRatingById(int id) {
        log.debug("Получение рейтинга MPA с id {}", id);
        return mpaStorage.getMpaRatingById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + id + " не найден"));
    }

    public List<Genre> getAllGenres() {
        log.debug("Получение списка всех жанров");
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(int id) {
        log.debug("Получение жанра с id {}", id);
        return genreStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }
}