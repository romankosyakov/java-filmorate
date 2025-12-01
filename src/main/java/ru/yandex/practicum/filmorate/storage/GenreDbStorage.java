package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) -> Genre.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .build();

    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY id";
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper);
        log.debug("Получено {} жанров", genres.size());
        return genres;
    }

    public Genre getGenreById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper, id);

        if (genres.isEmpty()) {
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        }

        log.debug("Получен жанр: {}", genres.getFirst().getName());
        return genres.getFirst();
    }

    public Map<Integer, Genre> getGenresByIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object> params = new ArrayList<>(ids);

        String placeholders = String.join(",",
                Collections.nCopies(ids.size(), "?"));

        String sql = "SELECT * FROM genres WHERE id IN (" + placeholders + ")";

        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper, params.toArray());

        return genres.stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));
    }
}