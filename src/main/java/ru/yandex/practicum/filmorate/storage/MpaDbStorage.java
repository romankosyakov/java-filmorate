package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Mpa> mpaRowMapper = (rs, rowNum) -> Mpa.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .description(rs.getString("description"))
            .build();

    @Override
    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM ratings ORDER BY id";
        List<Mpa> mpaList = jdbcTemplate.query(sql, mpaRowMapper);
        log.debug("Получено {} рейтингов MPA", mpaList.size());
        return mpaList;
    }

    @Override
    public Mpa getMpaById(int id) {
        String sql = "SELECT * FROM ratings WHERE id = ?";
        List<Mpa> mpaList = jdbcTemplate.query(sql, mpaRowMapper, id);

        if (mpaList.isEmpty()) {
            throw new NotFoundException("Рейтинг MPA с ID " + id + " не найден");
        }

        log.debug("Получен рейтинг MPA: {}", mpaList.getFirst().getName());
        return mpaList.getFirst();
    }

}