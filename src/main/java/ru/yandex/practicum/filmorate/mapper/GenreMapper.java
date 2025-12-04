package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;
import org.springframework.stereotype.Component;

@Component
public class GenreMapper {

    public static GenreDto toDto(Genre genre) {
        if (genre == null) {
            return null;
        }

        return GenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }

    public static Genre toEntity(GenreDto dto) {
        if (dto == null) {
            return null;
        }

        return Genre.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}