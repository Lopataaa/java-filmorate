package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmUpdateDto {
    @NotNull(message = "ID фильма не может быть null")
    private int id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть null")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private Integer duration;

    @NotNull(message = "MPA рейтинг не может быть null")
    private int mpaId;

    private Set<Integer> genreIds;

    public Film toEntity(FilmUpdateDto dto) {
        if (dto == null) {
            return null;
        }

        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getMpaId() != 0) {
            Mpa mpa = new Mpa();
            mpa.setId(dto.getMpaId());
            film.setMpa(mpa);
        }

        // Жанры
        if (dto.getGenreIds() != null) {
            Set<Genre> genres = dto.getGenreIds().stream()
                    .map(genreId -> {
                        Genre genre = new Genre();
                        genre.setId(genreId);  // ← уже int, не нужен .intValue()
                        return genre;
                    })
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        }

        return film;
    }
}