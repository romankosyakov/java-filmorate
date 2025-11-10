package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long id = 1;

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Вызван метод получения списка всех пользователей");
        return List.copyOf(users.values());
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable long id) {
        log.debug("Вызван метод получения пользователя с ID: {}", id);
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        return user;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addNewUser(@Valid @RequestBody User user) {
        validateUser(user);

        User newUser = User.builder()
                .id(id++)
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName()) // name может быть null - логика в геттере
                .birthday(user.getBirthday())
                .build();

        users.put(newUser.getId(), newUser);
        log.info("Добавлен новый пользователь: '{}' (ID: {})",
                newUser.getName() != null ? newUser.getName() : newUser.getLogin(),
                newUser.getId());
        return newUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            throw new ValidationException("ID пользователя обязателен для обновления");
        }

        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        validateUser(user);

        User updatedUser = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();

        users.put(updatedUser.getId(), updatedUser);
        log.info("Обновлен пользователь: '{}' (ID: {})",
                updatedUser.getName() != null ? updatedUser.getName() : updatedUser.getLogin(),
                updatedUser.getId());
        return updatedUser;
    }

    private void validateUser(User user) {
        if (user.getLogin() != null && user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getName() != null && user.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }
    }
}