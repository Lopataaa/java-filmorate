package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpaDto {
    @NotNull(message = "ID MPA не может быть null")
    private Long id;

    @NotBlank(message = "Название MPA не может быть пустым")
    private String name;
}