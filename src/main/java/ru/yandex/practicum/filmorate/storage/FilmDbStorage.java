package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Primary
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Mpa mpa = Mpa.builder()
                .id(rs.getInt("r_id"))
                .name(rs.getString("r_name"))
                .description(rs.getString("r_description"))
                .build();

        Film film = Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .build();

        film.getLikes().addAll(getLikes(film.getId()));
        return film;
    };

    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) -> Genre.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .build();

    @Override
    public Film getFilm(int id) {
        String sql = "SELECT f.*, r.id as r_id, r.name as r_name, r.description as r_description " +
                "FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id " +
                "WHERE f.id = ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }

        Film film = films.getFirst();
        film.setGenres(getFilmGenres(id));
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, r.id as r_id, r.name as r_name, r.description as r_description " +
                "FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.id " +
                "ORDER BY f.id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        for (Film film : films) {
            film.setGenres(getFilmGenres(film.getId()));
        }

        return films;
    }

    @Override
    public Film addNewFilm(Film film) {
        String nextIdSql = "SELECT COALESCE(MAX(id), 0) + 1 FROM films";
        Integer nextId = jdbcTemplate.queryForObject(nextIdSql, Integer.class);

        String sql = "INSERT INTO films (id, name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                nextId,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId());

        film.setId(nextId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenresToFilm(nextId, film.getGenres());
        }

        log.info("Добавлен новый фильм: '{}' (ID: {})", film.getName(), film.getId());
        return getFilm(nextId);
    }

    @Override
    public Film updateFilm(Film filmUpdate) {
        Film existingFilm = getFilm(filmUpdate.getId());

        String name = filmUpdate.getName() != null ? filmUpdate.getName() : existingFilm.getName();
        String description = filmUpdate.getDescription() != null ? filmUpdate.getDescription() : existingFilm.getDescription();
        LocalDate releaseDate = filmUpdate.getReleaseDate() != null ? filmUpdate.getReleaseDate() : existingFilm.getReleaseDate();
        Integer duration = filmUpdate.getDuration() != null ? filmUpdate.getDuration() : existingFilm.getDuration();
        Mpa mpa = filmUpdate.getMpa() != null ? filmUpdate.getMpa() : existingFilm.getMpa();
        List<Genre> genres = filmUpdate.getGenres() != null ? filmUpdate.getGenres() : existingFilm.getGenres();

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                name,
                description,
                Date.valueOf(releaseDate),
                duration,
                mpa.getId(),
                filmUpdate.getId());

        addGenresToFilm(filmUpdate.getId(), genres);

        log.info("Обновлен фильм: '{}' (ID: {})", name, filmUpdate.getId());
        return getFilm(filmUpdate.getId());
    }

    public void addGenresToFilm(int filmId, List<Genre> genres) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        if (genres != null && !genres.isEmpty()) {

            List<Genre> uniqueGenres = genres.stream()
                    .filter(genre -> genre != null && genre.getId() != null)
                    .distinct()
                    .toList();

            String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : uniqueGenres) {
                jdbcTemplate.update(insertSql, filmId, genre.getId());
            }
        }
    }

    public List<Genre> getFilmGenres(int filmId) {
        String sql = "SELECT g.* FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";
        return jdbcTemplate.query(sql, genreRowMapper, filmId);
    }

    public void addLike(int filmId, long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Добавлен лайк: фильм {}, пользователь {}", filmId, userId);
    }

    public void removeLike(int filmId, long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Удален лайк: фильм {}, пользователь {}", filmId, userId);
    }

    public Set<Long> getLikes(int filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("user_id"),
                filmId
        ));
    }
}