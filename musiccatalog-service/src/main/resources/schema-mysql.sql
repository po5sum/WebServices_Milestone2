USE `musiccatalog-db`;

create table if not exists artists
(
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    artist_id VARCHAR(255) NOT NULL UNIQUE,
    artist_name VARCHAR(255) NOT NULL,
    country VARCHAR(100) NOT NULL,
    debut_year int,
    biography VARCHAR(255) NOT NULL
    );

create table if not exists albums
(
    id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
    album_id VARCHAR(255) NOT NULL UNIQUE,
    artist_id VARCHAR(255) NOT NULL,
    album_title VARCHAR(255) NOT NULL,
    release_date INTEGER NOT NULL,
    album_length VARCHAR(10),
    album_genre VARCHAR(25),
    status VARCHAR(25)
    );
