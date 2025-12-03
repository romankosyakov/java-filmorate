package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

@Repository
@Primary
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .email(rs.getString("email"))
            .login(rs.getString("login"))
            .name(rs.getString("name"))
            .birthday(rs.getDate("birthday").toLocalDate())
            .build();

    @Override
    public User getUser(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        return users.getFirst();
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY id";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public User addNewUser(User user) {
        String nextIdSql = "SELECT COALESCE(MAX(id), 0) + 1 FROM users";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);

        String sql = "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                nextId,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()));

        User newUser = User.builder()
                .id(nextId)
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();

        log.info("Добавлен новый пользователь: '{}' (ID: {})", newUser.getName(), newUser.getId());
        return newUser;
    }

    @Override
    public User updateUser(User userUpdate) {
        User existingUser = getUser(userUpdate.getId());

        String email = userUpdate.getEmail() != null ? userUpdate.getEmail() : existingUser.getEmail();
        String login = userUpdate.getLogin() != null ? userUpdate.getLogin() : existingUser.getLogin();
        String name = userUpdate.getName() != null ? userUpdate.getName() : existingUser.getName();
        LocalDate birthday = userUpdate.getBirthday() != null ? userUpdate.getBirthday() : existingUser.getBirthday();

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                email,
                login,
                name,
                Date.valueOf(birthday),
                userUpdate.getId());

        log.info("Обновлен пользователь: '{}' (ID: {})", name, userUpdate.getId());
        return getUser(userUpdate.getId());
    }

    @Override
    public boolean userExists(long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }
}