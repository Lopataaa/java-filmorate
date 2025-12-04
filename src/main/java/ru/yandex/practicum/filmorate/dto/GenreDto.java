package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreDto {
    @NotNull(message = "ID жанра не может быть null")
    private Integer id;

    @NotBlank(message = "Название жанра не может быть пустым")
    private String name;
}