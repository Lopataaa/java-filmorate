# java-filmorate
Template repository for Filmorate project.

# Filmorate

## Схема базы данных

![Database Schema](database-schema%20java-filmorate.png)

## Основные таблицы:

- **films** - фильмы
- **users** - пользователи  
- **genres** - жанры
- **mpa_ratings** - рейтинги (G, PG, PG-13, R, NC-17)

## Связи:
- **film_genres** - к каким жанрам относится фильм
- **film_likes** - какие фильмы лайкнули пользователи
- **friendships** - кто с кем дружит

## Примеры запросов:

### Популярные фильмы:
```sql
SELECT f.*, COUNT(l.user_id) as likes
FROM films f
LEFT JOIN film_likes l ON f.id = l.film_id
GROUP BY f.id
ORDER BY likes DESC
LIMIT 10;
```

### Фильмы по жанру:
```sql
SELECT f.* 
FROM films f
JOIN film_genres fg ON f.id = fg.film_id
WHERE fg.genre_id = 3; -- комедия
```

### Друзья пользователя:
```sql
SELECT u.* 
FROM users u
JOIN friendships f ON u.id = f.friend_id
WHERE f.user_id = 1 AND f.status = 'confirmed';
```

### Добавить лайк:
```sql
INSERT INTO film_likes (film_id, user_id) VALUES (1, 5);
```

### Добавить друга:
```sql
INSERT INTO friendships (user_id, friend_id, status) 
VALUES (1, 2, 'pending');
```
