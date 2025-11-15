package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mpa {
    private Integer id;
    private String name;
    private String description;

    public Mpa(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}