package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "ORDER BY f.id";

        List<Film> films = jdbcTemplate.query(sql, this::mapFilm);

        if (!films.isEmpty()) {
            loadGenresForFilms(films);
        }

        return films;
    }

    @Override
    public Optional<Film> getById(int id) {
        String sql = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "WHERE f.id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapFilm, id);
            if (film != null) {
                loadGenresForFilms(List.of(film));
                return Optional.of(film);
            }
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        Integer mpaId = (film.getMpa() != null && film.getMpa().getId() > 0) ? film.getMpa().getId() : null;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (mpaId != null) {
                ps.setInt(5, mpaId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            film.setId(keyHolder.getKey().intValue());

            if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                for (Genre genre : film.getGenres()) {
                    if (genre.getId() > 0) {
                        addGenre(film.getId(), genre.getId());
                    }
                }
                loadGenresForFilms(List.of(film));
            }

            if (mpaId != null) {
                String mpaSql = "SELECT id, name, description FROM mpa_ratings WHERE id = ?";
                jdbcTemplate.query(mpaSql, rs -> {
                    if (rs.next()) {
                        Mpa mpa = new Mpa();
                        mpa.setId(rs.getInt("id"));
                        mpa.setName(rs.getString("name"));
                        mpa.setDescription(rs.getString("description"));
                        film.setMpa(mpa);
                    }
                }, mpaId);
            }
        }

        return film;
    }

    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE id = ?";

        Integer mpaId = (film.getMpa() != null && film.getMpa().getId() > 0) ? film.getMpa().getId() : null;

        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaId,
                film.getId());

        if (rowsUpdated == 0) {
            throw new IllegalArgumentException("Фильм с id " + film.getId() + " не найден");
        }

        if (film.getGenres() != null) {
            String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
            jdbcTemplate.update(deleteSql, film.getId());

            for (Genre genre : film.getGenres()) {
                if (genre.getId() > 0) {
                    addGenre(film.getId(), genre.getId());
                }
            }
        }

        film.setGenres(new HashSet<>());
        loadGenresForFilms(List.of(film));

        if (mpaId != null) {
            String mpaSql = "SELECT id, name, description FROM mpa_ratings WHERE id = ?";
            jdbcTemplate.query(mpaSql, rs -> {
                if (rs.next()) {
                    Mpa mpa = new Mpa();
                    mpa.setId(rs.getInt("id"));
                    mpa.setName(rs.getString("name"));
                    mpa.setDescription(rs.getString("description"));
                    film.setMpa(mpa);
                }
            }, mpaId);
        } else {
            film.setMpa(null);
        }

        return film;
    }

    @Override
    public void delete(int id) {
        String deleteLikesSql = "DELETE FROM likes WHERE film_id = ?";
        jdbcTemplate.update(deleteLikesSql, id);

        String deleteFilmGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteFilmGenresSql, id);

        String deleteFilmSql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(deleteFilmSql, id);
    }

    @Override
    public boolean exists(int id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public Mpa getMpaRatingById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapMpaRating, id);
    }

    public List<Mpa> getAllMpaRatings() {
        String sql = "SELECT * FROM mpa_ratings";
        return jdbcTemplate.query(sql, this::mapMpaRating);
    }

    public Genre getGenreById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapGenre, id);
    }

    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres";
        return jdbcTemplate.query(sql, this::mapGenre);
    }

    public void addLike(int filmId, int userId) {
        if (!exists(filmId)) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }

        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        if (!exists(filmId)) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }

        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public List<Integer> getLikes(int filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return jdbcTemplate.queryForList(sql, Integer.class, filmId);
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sql, film.getId(), genre.getId());
            }
        }
    }

    private void updateFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());
        saveFilmGenres(film);
    }

    public Film mapFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        java.sql.Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        film.setDuration(rs.getInt("duration"));

        int mpaId = rs.getInt("mpa_id");
        if (!rs.wasNull() && mpaId > 0) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
            mpa.setDescription(rs.getString("mpa_description"));
            film.setMpa(mpa);
        } else {
            film.setMpa(null);
        }

        film.setGenres(new HashSet<>());

        return film;
    }

    private Mpa mapMpaRating(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("id"));
        mpa.setName(rs.getString("name"));
        mpa.setDescription(rs.getString("description"));
        return mpa;
    }

    private Genre mapGenre(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("id"));
        genre.setName(rs.getString("name"));
        return genre;
    }

    public void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        List<Integer> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        String sql = "SELECT fg.film_id, g.id, g.name " + "FROM film_genres fg " + "JOIN genres g ON fg.genre_id = g.id " + "WHERE fg.film_id IN (" + String.join(",", Collections.nCopies(filmIds.size(), "?")) + ") " + "ORDER BY fg.film_id, g.id";

        Map<Integer, List<Genre>> genresByFilmId = new HashMap<>();

        jdbcTemplate.query(sql, filmIds.toArray(), (rs, rowNum) -> {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));

            genresByFilmId.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
            return null;
        });

        for (Film film : films) {
            List<Genre> genres = genresByFilmId.get(film.getId());
            if (genres != null) {
                film.setGenres(new LinkedHashSet<>(genres));
            } else {
                film.setGenres(new LinkedHashSet<>());
            }
        }
    }

    public void addGenre(int filmId, int genreId) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, genreId);
    }

    public void removeAllGenres(int filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}