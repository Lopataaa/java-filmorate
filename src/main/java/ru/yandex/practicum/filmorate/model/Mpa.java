package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Mpa {
    private final Integer id;
    private final String name;
    private final String description;

    public Mpa(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}