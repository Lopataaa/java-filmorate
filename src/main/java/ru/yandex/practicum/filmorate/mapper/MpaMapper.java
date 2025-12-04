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

    public Mpa toEntity(MpaDto dto) {
        if (dto == null) {
            return null;
        }

        Mpa mpa = new Mpa();
        mpa.setId(dto.getId().intValue());
        mpa.setName(dto.getName());

        return mpa;
    }
}