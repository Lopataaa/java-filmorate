package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Mpa mpa;
    private Set<Genre> genres = new HashSet<>();
    private Set<Integer> likes = new HashSet<>();

    // Конструкторы
    public Film() {
    }

    public Film(Integer id, String name, String description, LocalDate releaseDate,
                Integer duration, Mpa mpa) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
    }

    // Методы для likes
    public void addLike(Integer userId) {
        likes.add(userId);
    }

    public void removeLike(Integer userId) {
        likes.remove(userId);
    }

    public Set<Integer> getLikes() {
        return new HashSet<>(likes);
    }

    public int getLikesCount() {
        return likes.size();
    }

    // Методы для genres
    public void addGenre(Genre genre) {
        genres.add(genre);
    }

    public void removeGenre(Genre genre) {
        genres.remove(genre);
    }

    public Set<Genre> getGenres() {
        return new HashSet<>(genres);
    }

    // Сеттер для genres (важно для Spring/JDBC)
    public void setGenres(Set<Genre> genres) {
        this.genres.clear();
        if (genres != null) {
            this.genres.addAll(genres);
        }
    }

    // Сеттер для likes (важно для Spring/JDBC)
    public void setLikes(Set<Integer> likes) {
        this.likes.clear();
        if (likes != null) {
            this.likes.addAll(likes);
        }
    }

    // Метод для удобного вывода (опционально)
    @Override
    public String toString() {
        return "Film{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", releaseDate=" + releaseDate +
                ", duration=" + duration +
                ", mpa=" + mpa +
                ", genres=" + genres +
                ", likes=" + likes +
                '}';
    }
}