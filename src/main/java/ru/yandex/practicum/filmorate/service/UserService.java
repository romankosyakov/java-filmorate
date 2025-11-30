package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Data
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipDbStorage friendshipDbStorage;

    public void addFriend(Long userID, Long friendID) {
        if (userID.equals(friendID)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья!");
        }
        friendshipDbStorage.addFriend(userID, friendID);
    }

    public void deleteFriend(Long userID, Long friendID) {
        if (userID.equals(friendID)) {
            throw new ValidationException("Пользователь не может удалить себя из друзей!");
        }
        friendshipDbStorage.removeFriend(userID, friendID);
    }

    public List<User> getAllUserFriends(Long userID) {
        return friendshipDbStorage.getFriends(userID);
    }

    public List<User> getCommonFriends(Long firstUserID, Long secondUserID) {
        return friendshipDbStorage.getCommonFriends(firstUserID, secondUserID);
    }
}