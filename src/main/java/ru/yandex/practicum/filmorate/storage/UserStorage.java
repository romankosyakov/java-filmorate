package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User getUser(long id);

    List<User> getAllUsers();

    User addNewUser(User user);

    User updateUser(User userUpdate);

}
