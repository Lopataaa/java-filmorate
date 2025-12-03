package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FilmMapper {

    public FilmDto toDto(Film film) {
        if (film == null) {
            return null;
        }

        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpa() != null ?
                        MpaMapper.toDto(film.getMpa()) : null)
                .genres(film.getGenres() != null ?
                        film.getGenres().stream()
                                .map(GenreMapper::toDto)
                                .collect(Collectors.toSet()) : null)
                .build();
    }

    public Collection<FilmDto> toDtoCollection(Collection<Film> films) {
        if (films == null) {
            return null;
        }

        return films.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Film toEntity(FilmCreateDto dto) {
        if (dto == null) {
            return null;
        }

        Film film = new Film();
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        validateReleaseDate(film.getReleaseDate());

        if (dto.getMpaId() != 0) {
            Mpa mpa = new Mpa();
            mpa.setId(dto.getMpaId());
            film.setMpa(mpa);
        }

        if (dto.getGenreIds() != null) {
            Set<Genre> genres = dto.getGenreIds().stream()
                    .map(genreId -> {
                        Genre genre = new Genre();
                        genre.setId(genreId.intValue());
                        return genre;
                    })
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        }

        return film;
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (releaseDate != null && releaseDate.isBefore(minReleaseDate)) {
            throw new IllegalArgumentException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}