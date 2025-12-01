package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Primary
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Mpa mpa = Mpa.builder()
                .id(rs.getInt("r_id"))
                .name(rs.getString("r_name"))
                .description(rs.getString("r_description"))
                .build();

        return Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .build();
    };

    @Override
    public Film getFilm(int id) {
        String filmSql = """
                SELECT f.*, r.id as r_id, r.name as r_name, r.description as r_description
                FROM films f
                LEFT JOIN ratings r ON f.rating_id = r.id
                WHERE f.id = ?
                """;

        List<Film> films = jdbcTemplate.query(filmSql, filmRowMapper, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }

        Film film = films.getFirst();
        film.setGenres(getGenresForFilm(id));
        film.getLikes().addAll(getLikesForFilm(id));

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String filmsSql = """
                SELECT f.*, r.id as r_id, r.name as r_name, r.description as r_description
                FROM films f
                LEFT JOIN ratings r ON f.rating_id = r.id
                ORDER BY f.id
                """;

        List<Film> films = jdbcTemplate.query(filmsSql, filmRowMapper);

        if (films.isEmpty()) {
            return films;
        }

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Integer, List<Genre>> genresByFilmId = getGenresForFilms(filmIds);
        Map<Integer, Set<Long>> likesByFilmId = getLikesForFilms(filmIds);

        films.forEach(film -> {
            List<Genre> filmGenres = genresByFilmId.get(film.getId());
            film.setGenres(filmGenres != null ? filmGenres : Collections.emptyList());

            Set<Long> filmLikes = likesByFilmId.get(film.getId());
            if (filmLikes != null) {
                film.getLikes().addAll(filmLikes);
            }
        });

        return films;
    }

    private Map<Integer, List<Genre>> getGenresForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));

        String sql = """
                SELECT fg.film_id, fg.genre_id
                FROM film_genres fg
                WHERE fg.film_id IN (%s)
                ORDER BY fg.film_id
                """.formatted(placeholders);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        Set<Integer> genreIds = rows.stream()
                .map(row -> (Integer) row.get("genre_id"))
                .collect(Collectors.toSet());

        Map<Integer, Genre> genresMap = genreIds.isEmpty()
                ? Collections.emptyMap()
                : genreDbStorage.getGenresByIds(genreIds);

        Map<Integer, List<Genre>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");
            Integer genreId = (Integer) row.get("genre_id");

            Genre genre = genresMap.get(genreId);
            if (genre != null) {
                result.computeIfAbsent(filmId, k -> new ArrayList<>())
                        .add(genre);
            }
        }

        return result;
    }

    private List<Genre> getGenresForFilm(int filmId) {
        String sql = """
                SELECT g.*
                FROM genres g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        Genre.builder()
                                .id(rs.getInt("id"))
                                .name(rs.getString("name"))
                                .build(),
                filmId
        );
    }

    private Map<Integer, Set<Long>> getLikesForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));

        String sql = """
                SELECT film_id, user_id
                FROM likes
                WHERE film_id IN (%s)
                """.formatted(placeholders);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        Map<Integer, Set<Long>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");
            Long userId = ((Number) row.get("user_id")).longValue();

            result.computeIfAbsent(filmId, k -> new HashSet<>())
                    .add(userId);
        }

        return result;
    }

    private Set<Long> getLikesForFilm(int filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("user_id"),
                filmId
        );
        return new HashSet<>(likes);
    }

    @Override
    public Film addNewFilm(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("MPA rating is required");
        }

        Mpa fullMpa = mpaDbStorage.getMpaById(film.getMpa().getId());
        film.setMpa(fullMpa);

        List<Genre> genresToSave = processGenres(film.getGenres());
        film.setGenres(genresToSave);

        String nextIdSql = "SELECT COALESCE(MAX(id), 0) + 1 FROM films";
        Integer nextId = jdbcTemplate.queryForObject(nextIdSql, Integer.class);

        String sql = """
                INSERT INTO films (id, name, description, release_date, duration, rating_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql, nextId, film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa().getId());

        film.setId(nextId);

        addGenresToFilm(nextId, genresToSave);

        log.info("Добавлен новый фильм: '{}' (ID: {})", film.getName(), film.getId());

        Film savedFilm = getFilm(nextId);
        savedFilm.setMpa(fullMpa);
        savedFilm.setGenres(genresToSave);
        return savedFilm;
    }

    @Override
    public Film updateFilm(Film filmUpdate) {
        Film existingFilm = getFilm(filmUpdate.getId());

        if (filmUpdate.getMpa() == null || filmUpdate.getMpa().getId() == null) {
            throw new ValidationException("MPA rating is required");
        }

        Mpa fullMpa = mpaDbStorage.getMpaById(filmUpdate.getMpa().getId());
        filmUpdate.setMpa(fullMpa);

        List<Genre> genresToSave;
        if (filmUpdate.getGenres() != null) {
            genresToSave = processGenres(filmUpdate.getGenres());
        } else {
            genresToSave = existingFilm.getGenres();
        }

        String sql = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                filmUpdate.getName(),
                filmUpdate.getDescription(),
                Date.valueOf(filmUpdate.getReleaseDate()),
                filmUpdate.getDuration(),
                filmUpdate.getMpa().getId(),
                filmUpdate.getId());

        addGenresToFilm(filmUpdate.getId(), genresToSave);

        log.info("Обновлен фильм: '{}' (ID: {})", filmUpdate.getName(), filmUpdate.getId());

        Film updatedFilm = getFilm(filmUpdate.getId());
        updatedFilm.setMpa(fullMpa);
        updatedFilm.setGenres(genresToSave);
        return updatedFilm;
    }

    private List<Genre> processGenres(List<Genre> genres) {
        if (genres == null) {
            return new ArrayList<>();
        }

        Set<Integer> uniqueGenreIds = new HashSet<>();
        List<Genre> result = new ArrayList<>();

        for (Genre genre : genres) {
            if (genre != null && genre.getId() != null) {
                if (!uniqueGenreIds.contains(genre.getId())) {
                    Genre fullGenre = genreDbStorage.getGenreById(genre.getId());
                    result.add(fullGenre);
                    uniqueGenreIds.add(genre.getId());
                }
            }
        }

        result.sort(Comparator.comparing(Genre::getId));
        return result;
    }

    private void addGenresToFilm(int filmId, List<Genre> genres) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        if (!genres.isEmpty()) {
            String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

            List<Object[]> batchArgs = new ArrayList<>();
            for (Genre genre : genres) {
                if (genre != null && genre.getId() != null) {
                    batchArgs.add(new Object[]{filmId, genre.getId()});
                }
            }

            if (!batchArgs.isEmpty()) {
                jdbcTemplate.batchUpdate(insertSql, batchArgs);
            }
        }
    }

    public List<Film> getMostLikedFilms(int count) {
        String sql = """
                SELECT f.*, r.id as r_id, r.name as r_name, r.description as r_description,
                       COUNT(l.user_id) as like_count
                FROM films f
                LEFT JOIN ratings r ON f.rating_id = r.id
                LEFT JOIN likes l ON f.id = l.film_id
                GROUP BY f.id, r.id
                ORDER BY COUNT(l.user_id) DESC, f.id
                LIMIT ?
                """;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);

        if (films.isEmpty()) {
            return films;
        }

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Integer, List<Genre>> genresByFilmId = getGenresForFilms(filmIds);
        Map<Integer, Set<Long>> likesByFilmId = getLikesForFilms(filmIds);

        films.forEach(film -> {
            List<Genre> filmGenres = genresByFilmId.get(film.getId());
            film.setGenres(filmGenres != null ? filmGenres : Collections.emptyList());

            Set<Long> filmLikes = likesByFilmId.get(film.getId());
            if (filmLikes != null) {
                film.getLikes().addAll(filmLikes);
            }
        });

        return films;
    }
}