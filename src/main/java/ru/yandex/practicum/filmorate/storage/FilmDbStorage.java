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

import java.sql.Date;
import java.util.*;

@Repository
@Primary
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> Film.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .description(rs.getString("description"))
            .releaseDate(rs.getDate("release_date").toLocalDate())
            .duration(rs.getInt("duration"))
            .build();

    @Override
    public Film getFilm(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }

        Film film = films.getFirst();
        film.setMpa(getMpaForFilm(id));
        film.setGenres(getGenresForFilms(Collections.singletonList(id)).getOrDefault(id, Collections.emptyList()));
        film.getLikes().addAll(getLikesForFilms(Collections.singletonList(id)).getOrDefault(id, Collections.emptySet()));
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT * FROM films ORDER BY id";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        if (films.isEmpty()) {
            return films;
        }

        List<Integer> filmIds = new ArrayList<>();
        for (Film film : films) {
            filmIds.add(film.getId());
        }

        Map<Integer, Mpa> mpaByFilmId = getMpaForFilms(filmIds);
        Map<Integer, List<Genre>> genresByFilmId = getGenresForFilms(filmIds);
        Map<Integer, Set<Long>> likesByFilmId = getLikesForFilms(filmIds);

        for (Film film : films) {
            film.setMpa(mpaByFilmId.get(film.getId()));
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), Collections.emptyList()));
            film.getLikes().addAll(likesByFilmId.getOrDefault(film.getId(), Collections.emptySet()));
        }

        return films;
    }

    @Override
    public Map<Integer, List<Genre>> getGenresForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fg.film_id, fg.genre_id FROM film_genres fg WHERE fg.film_id IN (" + placeholders + ") ORDER BY fg.genre_id";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        Set<Integer> genreIds = new HashSet<>();
        for (Map<String, Object> row : rows) {
            genreIds.add((Integer) row.get("genre_id"));
        }

        Map<Integer, Genre> genresMap = new HashMap<>();
        if (!genreIds.isEmpty()) {
            String genrePlaceholders = String.join(",", Collections.nCopies(genreIds.size(), "?"));
            String genreSql = "SELECT * FROM genres WHERE id IN (" + genrePlaceholders + ") ORDER BY id";
            List<Genre> genres = jdbcTemplate.query(genreSql, (rs, rowNum) ->
                            Genre.builder()
                                    .id(rs.getInt("id"))
                                    .name(rs.getString("name"))
                                    .build(),
                    genreIds.toArray());

            for (Genre genre : genres) {
                genresMap.put(genre.getId(), genre);
            }
        }

        Map<Integer, List<Genre>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");
            Integer genreId = (Integer) row.get("genre_id");
            Genre genre = genresMap.get(genreId);
            if (genre != null) {
                result.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
            }
        }

        return result;
    }

    @Override
    public Map<Integer, Set<Long>> getLikesForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT film_id, user_id FROM likes WHERE film_id IN (" + placeholders + ")";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        Map<Integer, Set<Long>> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");
            Long userId = ((Number) row.get("user_id")).longValue();
            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }

        return result;
    }

    @Override
    public Film addNewFilm(Film film) {
        String nextIdSql = "SELECT COALESCE(MAX(id), 0) + 1 FROM films";
        Integer nextId = jdbcTemplate.queryForObject(nextIdSql, Integer.class);

        String sql = "INSERT INTO films (id, name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, nextId, film.getName(), film.getDescription(),
                Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa().getId());

        film.setId(nextId);
        return film;
    }

    @Override
    public Film updateFilm(Film filmUpdate) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, filmUpdate.getName(), filmUpdate.getDescription(),
                Date.valueOf(filmUpdate.getReleaseDate()), filmUpdate.getDuration(),
                filmUpdate.getMpa().getId(), filmUpdate.getId());
        return filmUpdate;
    }

    @Override
    public List<Genre> processGenres(List<Genre> genres) {
        if (genres == null) {
            return new ArrayList<>();
        }

        Set<Integer> inputGenreIds = new LinkedHashSet<>();
        for (Genre genre : genres) {
            if (genre != null && genre.getId() != null) {
                inputGenreIds.add(genre.getId());
            }
        }

        if (inputGenreIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> uniqueGenreIds = new ArrayList<>(inputGenreIds);
        String placeholders = String.join(",", Collections.nCopies(uniqueGenreIds.size(), "?"));
        String sql = "SELECT id, name FROM genres WHERE id IN (" + placeholders + ") ORDER BY id";

        List<Genre> foundGenres = jdbcTemplate.query(sql, (rs, rowNum) ->
                        Genre.builder()
                                .id(rs.getInt("id"))
                                .name(rs.getString("name"))
                                .build(),
                uniqueGenreIds.toArray());

        if (foundGenres.size() != uniqueGenreIds.size()) {
            Set<Integer> foundIds = new HashSet<>();
            for (Genre genre : foundGenres) {
                foundIds.add(genre.getId());
            }

            Set<Integer> missingIds = new HashSet<>(uniqueGenreIds);
            missingIds.removeAll(foundIds);

            throw new NotFoundException("Жанры с ID " + missingIds + " не найдены");
        }

        return foundGenres;
    }

    @Override
    public void addGenresToFilm(int filmId, List<Genre> genres) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        if (!genres.isEmpty()) {
            Set<Integer> uniqueGenreIds = new HashSet<>();
            List<Object[]> batchArgs = new ArrayList<>();

            for (Genre genre : genres) {
                if (genre != null && genre.getId() != null && !uniqueGenreIds.contains(genre.getId())) {
                    uniqueGenreIds.add(genre.getId());
                    batchArgs.add(new Object[]{filmId, genre.getId()});
                }
            }

            if (!batchArgs.isEmpty()) {
                String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
                jdbcTemplate.batchUpdate(insertSql, batchArgs);
            }
        }
    }

    @Override
    public List<Film> getMostLikedFilms(int count) {
        String sql = "SELECT f.* FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY COUNT(l.user_id) DESC, f.id " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);

        if (films.isEmpty()) {
            return films;
        }

        List<Integer> filmIds = new ArrayList<>();
        for (Film film : films) {
            filmIds.add(film.getId());
        }

        Map<Integer, Mpa> mpaByFilmId = getMpaForFilms(filmIds);
        Map<Integer, List<Genre>> genresByFilmId = getGenresForFilms(filmIds);
        Map<Integer, Set<Long>> likesByFilmId = getLikesForFilms(filmIds);

        for (Film film : films) {
            film.setMpa(mpaByFilmId.get(film.getId()));
            film.setGenres(genresByFilmId.getOrDefault(film.getId(), Collections.emptyList()));
            film.getLikes().addAll(likesByFilmId.getOrDefault(film.getId(), Collections.emptySet()));
        }

        return films;
    }

    private Mpa getMpaForFilm(int filmId) {
        String sql = "SELECT r.* FROM ratings r JOIN films f ON r.id = f.rating_id WHERE f.id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, (rs, rowNum) ->
                        Mpa.builder()
                                .id(rs.getInt("id"))
                                .name(rs.getString("name"))
                                .description(rs.getString("description"))
                                .build(),
                filmId);
        return mpaList.isEmpty() ? null : mpaList.getFirst();
    }

    private Map<Integer, Mpa> getMpaForFilms(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT f.id as film_id, r.* FROM films f JOIN ratings r ON f.rating_id = r.id WHERE f.id IN (" + placeholders + ")";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        Map<Integer, Mpa> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");
            Mpa mpa = Mpa.builder()
                    .id((Integer) row.get("id"))
                    .name((String) row.get("name"))
                    .description((String) row.get("description"))
                    .build();
            result.put(filmId, mpa);
        }

        return result;
    }
}