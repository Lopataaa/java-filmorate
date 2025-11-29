-- Очистка таблиц (в правильном порядке из-за foreign keys)
DELETE FROM film_likes;
DELETE FROM film_genres;
DELETE FROM friendships;
DELETE FROM films;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM mpa_ratings;

-- MPA рейтинги для тестов
MERGE INTO mpa_ratings (id, name, description) KEY(id) VALUES
(1, 'G', 'Нет возрастных ограничений'),
(2, 'PG', 'Рекомендуется присутствие родителей');

-- Жанры для тестов
MERGE INTO genres (id, name) KEY(id) VALUES
(1, 'Комедия'),
(2, 'Драма');

-- Тестовые пользователи
INSERT INTO users (id, email, login, name, birthday) VALUES
(1, 'user1@example.com', 'user1', 'User One', '1990-01-01'),
(2, 'user2@example.com', 'user2', 'User Two', '1995-05-15'),
(3, 'user3@example.com', 'user3', 'User Three', '2000-10-20');

-- Тестовые фильмы
INSERT INTO films (id, name, description, release_date, duration, mpa_id) VALUES
(1, 'Film 1', 'Description 1', '2020-01-01', 120, 1),
(2, 'Film 2', 'Description 2', '2021-01-01', 130, 2),
(3, 'Film 3', 'Description 3', '2022-01-01', 140, 1);