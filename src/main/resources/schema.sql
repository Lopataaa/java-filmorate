---- Рейтинги MPA
--CREATE TABLE mpa_ratings (
--    id INT PRIMARY KEY AUTO_INCREMENT,
--    name VARCHAR(10) UNIQUE NOT NULL,
--    description VARCHAR(255) NOT NULL
--);
--
---- Жанры
--CREATE TABLE genres (
--    id INT PRIMARY KEY AUTO_INCREMENT,
--    name VARCHAR(255) UNIQUE NOT NULL
--);
--
---- Пользователи
--CREATE TABLE users (
--    id INT PRIMARY KEY AUTO_INCREMENT,
--    email VARCHAR(255) UNIQUE NOT NULL,
--    login VARCHAR(255) NOT NULL,
--    name VARCHAR(255),
--    birthday DATE NOT NULL
--);
--
---- Фильмы
--CREATE TABLE films (
--    id INT PRIMARY KEY AUTO_INCREMENT,
--    name VARCHAR(255) NOT NULL,
--    description TEXT,
--    release_date DATE NOT NULL,
--    duration INT NOT NULL,
--    mpa_id INT NOT NULL,
--    FOREIGN KEY (mpa_id) REFERENCES mpa_ratings(id)
--);
--
---- Связь фильмов и жанров
--CREATE TABLE film_genres (
--    film_id INT,
--    genre_id INT,
--    PRIMARY KEY (film_id, genre_id),
--    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
--    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
--);
--
---- Лайки фильмов
--CREATE TABLE film_likes (
--    film_id INT,
--    user_id INT,
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    PRIMARY KEY (film_id, user_id),
--    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
--    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
--);
--
---- Дружба между пользователями
--CREATE TABLE friendships (
--    user_id INT,
--    friend_id INT,
--    status VARCHAR(20) DEFAULT 'PENDING',
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    PRIMARY KEY (user_id, friend_id),
--    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
--    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
--);

-- Рейтинги MPA
CREATE TABLE IF NOT EXISTS mpa_ratings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(10) UNIQUE NOT NULL,
    description VARCHAR(255) NOT NULL
);

-- Жанры
CREATE TABLE IF NOT EXISTS genres (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Пользователи
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    login VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    birthday DATE NOT NULL
);

-- Фильмы
CREATE TABLE IF NOT EXISTS films (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    mpa_id INT NOT NULL,
    FOREIGN KEY (mpa_id) REFERENCES mpa_ratings(id)
);

-- Связь фильмов и жанров
CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT,
    genre_id INT,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

-- Лайки фильмов
CREATE TABLE IF NOT EXISTS film_likes (
    film_id INT,
    user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Дружба между пользователями
CREATE TABLE IF NOT EXISTS friendships (
    user_id INT,
    friend_id INT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Индексы для оптимизации
CREATE INDEX IF NOT EXISTS idx_films_mpa_id ON films(mpa_id);
CREATE INDEX IF NOT EXISTS idx_film_genres_film_id ON film_genres(film_id);
CREATE INDEX IF NOT EXISTS idx_film_genres_genre_id ON film_genres(genre_id);
CREATE INDEX IF NOT EXISTS idx_film_likes_film_id ON film_likes(film_id);
CREATE INDEX IF NOT EXISTS idx_film_likes_user_id ON film_likes(user_id);
CREATE INDEX IF NOT EXISTS idx_friendships_user_id ON friendships(user_id);
CREATE INDEX IF NOT EXISTS idx_friendships_friend_id ON friendships(friend_id);