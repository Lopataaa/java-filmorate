CREATE TABLE IF NOT EXISTS users (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    birthday DATE
);

CREATE TABLE IF NOT EXISTS mpa_ratings (
    id INTEGER PRIMARY KEY,
    name VARCHAR(10) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS films (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(200),
    release_date DATE,
    duration INTEGER,
    mpa_id INTEGER REFERENCES mpa_ratings(id)
);