package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Data
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public void addFriend(Long userID, Long friendID) {
        if (userID.equals(friendID)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья!");
        }
        friendshipStorage.addFriend(userID, friendID);
    }

    public void deleteFriend(Long userID, Long friendID) {
        if (userID.equals(friendID)) {
            throw new ValidationException("Пользователь не может удалить себя из друзей!");
        }
        friendshipStorage.removeFriend(userID, friendID);
    }

    public List<User> getAllUserFriends(Long userID) {
        if (!userStorage.userExists(userID)) {
            throw new NotFoundException("Пользователь с ID " + userID + " не найден");
        }

        return friendshipStorage.getFriends(userID);
    }

    public List<User> getCommonFriends(Long firstUserID, Long secondUserID) {
        return friendshipStorage.getCommonFriends(firstUserID, secondUserID);
    }
}