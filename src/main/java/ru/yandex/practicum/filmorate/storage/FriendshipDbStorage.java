package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userDbStorage;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .email(rs.getString("email"))
            .login(rs.getString("login"))
            .name(rs.getString("name"))
            .birthday(rs.getDate("birthday").toLocalDate())
            .build();

    @Override
    public void addFriend(Long userId, Long friendId) {
        validateUsers(userId, friendId);

        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Пользователь не может добавить себя в друзья");
        }

        if (friendshipExists(userId, friendId)) {
            throw new IllegalArgumentException("Пользователь уже в друзьях");
        }

        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        validateUsers(userId, friendId);

        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        int deleted = jdbcTemplate.update(sql, userId, friendId);

        if (deleted == 0) {
            log.info("Пользователь {} не был в друзьях у пользователя {}", friendId, userId);
            return;
        }

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {


        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.id = f.friend_id " +
                "WHERE f.user_id = ? " +
                "ORDER BY u.id";

        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        validateUsers(userId1, userId2);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id " +
                "JOIN friendships f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? " +
                "ORDER BY u.id";

        return jdbcTemplate.query(sql, userRowMapper, userId1, userId2);
    }

    @Override
    public void validateUsers(Long userId, Long friendId) {
        if (!userDbStorage.userExists(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (!userDbStorage.userExists(friendId)) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }
    }

    @Override
    public boolean friendshipExists(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count > 0;
    }
}