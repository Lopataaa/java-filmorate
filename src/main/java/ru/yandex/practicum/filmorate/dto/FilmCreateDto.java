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
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmCreateDto {
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
}