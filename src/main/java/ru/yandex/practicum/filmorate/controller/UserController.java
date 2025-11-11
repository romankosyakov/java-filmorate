package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import jakarta.validation.Valid;

import java.time.LocalDate;
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
    public User updateUser(@RequestBody User userUpdate) {
        if (userUpdate.getId() == null) {
            throw new ValidationException("ID пользователя обязателен для обновления");
        }

        User existingUser = users.get(userUpdate.getId());
        if (existingUser == null) {
            throw new NotFoundException("Пользователь с ID " + userUpdate.getId() + " не найден");
        }

        User updatedUser = User.builder()
                .id(existingUser.getId())
                .email(userUpdate.getEmail() != null ? userUpdate.getEmail() : existingUser.getEmail())
                .login(userUpdate.getLogin() != null ? userUpdate.getLogin() : existingUser.getLogin())
                .name(userUpdate.getName() != null ? userUpdate.getName() : existingUser.getName())
                .birthday(userUpdate.getBirthday() != null ? userUpdate.getBirthday() : existingUser.getBirthday())
                .build();

        validateUser(updatedUser);

        users.put(updatedUser.getId(), updatedUser);
        log.info("Обновлен пользователь: '{}' (ID: {})",
                updatedUser.getName() != null ? updatedUser.getName() : updatedUser.getLogin(),
                updatedUser.getId());
        return updatedUser;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email должен быть валидным и содержать @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Логин не может быть пустым");
        }

        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}