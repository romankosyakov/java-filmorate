package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public User addNewUser(@Validated(CreateValidation.class) User user) {
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

    public User updateUser(@Validated(UpdateValidation.class) User userUpdate) {
        User existingUser = users.get(userUpdate.getId());
        if (existingUser == null) {
            throw new NotFoundException("Пользователь с ID " + userUpdate.getId() + " не найден");
        }

        // Сохраняем друзей из существующего пользователя
        Set<Long> existingFriends = existingUser.getUserFriends();

        User updatedUser = User.builder()
                .id(existingUser.getId())
                .email(userUpdate.getEmail() != null ? userUpdate.getEmail() : existingUser.getEmail())
                .login(userUpdate.getLogin() != null ? userUpdate.getLogin() : existingUser.getLogin())
                .name(userUpdate.getName() != null ? userUpdate.getName() : existingUser.getName())
                .birthday(userUpdate.getBirthday() != null ? userUpdate.getBirthday() : existingUser.getBirthday())
                .build();

        // Восстанавливаем друзей
        updatedUser.getUserFriends().addAll(existingFriends);

        users.put(updatedUser.getId(), updatedUser);
        log.info("Обновлен пользователь: '{}' (ID: {})",
                updatedUser.getName() != null ? updatedUser.getName() : updatedUser.getLogin(),
                updatedUser.getId());
        return updatedUser;
    }

    public void deleteUser(Long userID) {
        if (userID <= 0) {
            throw new ValidationException("ID должен быть больше нуля");
        }
        User existingUser = users.get(userID);
        if (existingUser == null) {
            throw new NotFoundException("Пользователь с ID " + userID + " не найден");
        }
        users.remove(userID);
        log.info("Удален пользователь с ID {}", userID);
    }

    public void deleteAllUsers() {
        if (users.isEmpty()) {
            throw new NotFoundException("Список пользователей пуст. Невозможно выполнить операцию");
        }
        users.clear();
        log.info("Список пользователей пуст, выполнена процедура очистки списка пользователей.");
    }
}
