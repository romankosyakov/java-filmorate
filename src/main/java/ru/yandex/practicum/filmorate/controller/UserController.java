package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Вызван метод получения списка всех пользователей");
        return userStorage.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable long id) {
        log.debug("Вызван метод получения пользователя с ID: {}", id);
        return userStorage.getUser(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addNewUser(@Validated(CreateValidation.class) @RequestBody User user) {
        return userStorage.addNewUser(user);
    }

    @PutMapping
    public User updateUser(@Validated(UpdateValidation.class) @RequestBody User userUpdate) {
        return userStorage.updateUser(userUpdate);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") Long userID,
                          @PathVariable("friendId") Long friendID) {
        userService.addFriend(userID, friendID);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") Long userID,
                             @PathVariable("friendId") Long friendID) {
        userService.deleteFriend(userID, friendID);
    }

    @GetMapping("/{id}/friends")
    public Set<Long> getAllUserFriends(@PathVariable("id") Long userID) {
        return userService.getAllUserFriends(userID);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<Long> getCommonFriends(@PathVariable("id") Long firstUserID,
                                      @PathVariable("otherId") Long secondUserID) {
        return userService.getCommonFriends(firstUserID, secondUserID);
    }
}