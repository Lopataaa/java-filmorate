package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Film.
 */
//@Getter
//@Setter
@Data
public class Film {
    private Integer id;
    private  String name;
    private  String description;
    private LocalDate releaseDate;
    private Integer duration;

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public Integer getDuration() { return duration; }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public void setDuration(Integer duration) { this.duration = duration; }
}
