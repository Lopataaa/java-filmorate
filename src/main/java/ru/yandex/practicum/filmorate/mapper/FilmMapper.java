package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;


import java.util.Collection;
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
                                .collect(Collectors.toList()) : null)  // Изменяем на Collectors.toList()
                .likes(null)
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
}