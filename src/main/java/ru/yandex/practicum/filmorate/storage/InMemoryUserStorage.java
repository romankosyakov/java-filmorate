package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 1;

    public User getUser(long id) {
        if (id <= 0) {
            throw new ValidationException("ID должен быть больше нуля");
        }
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        return user;
    }

    public List<User> getAllUsers() {
        return List.copyOf(users.values());
    }

    public User addNewUser(User user) {
        User newUser = User.builder()
                .id(id++)
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();

        users.put(newUser.getId(), newUser);
        log.info("Добавлен новый пользователь: '{}' (ID: {})",
                newUser.getName() != null ? newUser.getName() : newUser.getLogin(),
                newUser.getId());
        return newUser;
    }

    public User updateUser(User userUpdate) {
        User existingUser = users.get(userUpdate.getId());
        if (existingUser == null) {
            throw new NotFoundException("Пользователь с ID " + userUpdate.getId() + " не найден");
        }

        if (userUpdate.getEmail() != null) {
            existingUser.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.getLogin() != null) {
            existingUser.setLogin(userUpdate.getLogin());
        }
        if (userUpdate.getName() != null) {
            existingUser.setName(userUpdate.getName());
        }
        if (userUpdate.getBirthday() != null) {
            existingUser.setBirthday(userUpdate.getBirthday());
        }

        log.info("Обновлен пользователь: '{}' (ID: {})",
                existingUser.getName() != null ? existingUser.getName() : existingUser.getLogin(),
                existingUser.getId());
        return existingUser;
    }

}
