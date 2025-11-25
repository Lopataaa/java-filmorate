package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FilmLike {
    private Integer filmId;
    private Integer userId;
    private LocalDateTime createdAt;
}