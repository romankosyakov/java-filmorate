package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        log.debug("Вызван метод получения списка всех пользователей");
        return userService.getUserStorage().getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable long id) {
        log.debug("Вызван метод получения пользователя с ID: {}", id);
        return userService.getUserStorage().getUser(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addNewUser(@Validated(CreateValidation.class) @RequestBody User user) {
        return userService.getUserStorage().addNewUser(user);
    }

    @PutMapping
    public User updateUser(@Validated(UpdateValidation.class) @RequestBody User userUpdate) {
        return userService.getUserStorage().updateUser(userUpdate);
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
    public List<User> getAllUserFriends(@PathVariable("id") Long userID) {
        return userService.getAllUserFriends(userID);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable("id") Long firstUserID,
                                      @PathVariable("otherId") Long secondUserID) {
        return userService.getCommonFriends(firstUserID, secondUserID);
    }
}