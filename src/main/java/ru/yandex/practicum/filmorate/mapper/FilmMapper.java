package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class FilmMapper {

    public FilmDto toDto(Film film) {
        if (film == null) return null;

        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpa() != null ? toMpaDto(film.getMpa()) : null)
                .genres(film.getGenres() != null ?
                        film.getGenres().stream()
                                .map(this::toGenreDto)
                                .collect(Collectors.toSet()) :
                        new HashSet<>())
                .likes(new HashSet<>())
                .build();
    }

    private MpaDto toMpaDto(Mpa mpa) {
        if (mpa == null) return null;

        return MpaDto.builder()
                .id((long) mpa.getId())
                .name(mpa.getName())
                .build();
    }

    private GenreDto toGenreDto(Genre genre) {
        if (genre == null) return null;

        return GenreDto.builder()
                .id((long) genre.getId())
                .name(genre.getName())
                .build();
    }
}