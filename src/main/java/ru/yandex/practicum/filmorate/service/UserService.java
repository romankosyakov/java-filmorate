package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Data
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public void addFriend(Long userID, Long friendID) {
        if (userID.equals(friendID)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья!");
        }

        User user = userStorage.getUser(userID);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userID + " не найден.");
        }
        User friend = userStorage.getUser(friendID);
        if (friend == null) {
            throw new NotFoundException("Пользователь с ID " + friendID + " не найден.");
        }


        Set<Long> firstUserFriendsIDs = user.getUserFriends();
        if (firstUserFriendsIDs.contains(friendID)) {
            throw new ValidationException("Добавление не выполнено, пользователи уже являются друзьями!");
        } else {
            firstUserFriendsIDs.add(friendID);
            friend.getUserFriends().add(userID);
            log.info("Пользователя с ID {} и {} теперь являются друзьями!", userID, friendID);
        }
    }

    public void deleteFriend(Long userID, Long friendID) {
        if (userID.equals(friendID)) {
            throw new ValidationException("Пользователь не может удалить себя из друзей!");
        }

        User user = userStorage.getUser(userID);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userID + " не найден.");
        }
        User friend = userStorage.getUser(friendID);
        if (friend == null) {
            throw new NotFoundException("Пользователь с ID " + friendID + " не найден.");
        }

        Set<Long> userFriends = user.getUserFriends();
        Set<Long> friendFriends = friend.getUserFriends();

        if (!userFriends.contains(friendID)) {
            log.info("Пользователь {} не был в друзьях у {}", friendID, userID);
            return; // успешное выполнение без исключения
        }

        userFriends.remove(friendID);
        friendFriends.remove(userID);

        log.info("Пользователь с ID {} удалил из списка друзей пользователя с ID {} и теперь они " +
                "НЕ являются друзьями!", userID, friendID);
    }

    public List<User> getAllUserFriends(Long userID) {
        User user = userStorage.getUser(userID);

        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userID + " не найден");
        }

        return user.getUserFriends().stream()
                .map(userStorage::getUser)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<User> getCommonFriends(Long firstUserID, Long secondUserID) {
        User firstUser = userStorage.getUser(firstUserID);
        if (firstUser == null) {
            throw new NotFoundException("Пользователь с ID " + firstUserID + " не найден.");
        }
        User secondUser = userStorage.getUser(secondUserID);
        if (secondUser == null) {
            throw new NotFoundException("Пользователь с ID " + secondUserID + " не найден.");
        }

        Set<Long> firstUserFriendsIDs = firstUser.getUserFriends();
        Set<Long> secondUserFriendsIDs = secondUser.getUserFriends();

        Set<Long> commonFriendsIDs = new HashSet<>(firstUserFriendsIDs);
        commonFriendsIDs.retainAll(secondUserFriendsIDs);

        return commonFriendsIDs.stream()
                .map(userStorage::getUser)
                .filter(Objects::nonNull)
                .toList();
    }

}