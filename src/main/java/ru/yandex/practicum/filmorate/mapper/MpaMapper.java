package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;
import org.springframework.stereotype.Component;

@Component
public class MpaMapper {

    public static MpaDto toDto(Mpa mpa) {
        if (mpa == null) {
            return null;
        }

        return MpaDto.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }

    public static Mpa toEntity(MpaDto dto) {
        if (dto == null) {
            return null;
        }

        return Mpa.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}