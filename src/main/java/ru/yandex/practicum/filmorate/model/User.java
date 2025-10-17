package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Integer id;
    private  String email;
    private  String login;
    private String name;
    private LocalDate birthday;

    public Integer getId() { return id; }
    public String getEmail() { return email; }
    public String getLogin() { return login; }
    public String getName() { return name; }
    public LocalDate getBirthday() { return birthday; }

    public void setId(Integer id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setLogin(String login) { this.login = login; }
    public void setName(String name) { this.name = name; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
}
