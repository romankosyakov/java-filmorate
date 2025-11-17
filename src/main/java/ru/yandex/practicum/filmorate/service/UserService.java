package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public void addFriend(Long userID, Long friendID) {
        if (userID.equals(friendID)) {
            throw new ValidationException("Пользователь не может добавить себя в друзья!");
        }

        User[] users = getTwoUsers(userID, friendID);
        User firstUser = users[0];
        User secondUser = users[1];

        Set<Long> firstUserFriends = firstUser.getUserFriends();
        if (firstUserFriends.contains(friendID)) {
            throw new ValidationException("Добавление не выполнено, пользователи уже являются друзьями!");
        } else {
            firstUserFriends.add(friendID);
            secondUser.getUserFriends().add(userID);
            log.info("Пользователя с ID {} и {} теперь являются друзьями!", userID, friendID);
        }
    }

    public void deleteFriend(Long userID, Long friendID) {
        if (userID.equals(friendID)) {
            throw new ValidationException("Пользователь не может удалить себя из друзей!");
        }

        User[] users = getTwoUsers(userID, friendID);
        User firstUser = users[0];
        User secondUser = users[1];

        Set<Long> firstUserFriends = firstUser.getUserFriends();
        if (!firstUserFriends.contains(friendID)) {
            throw new ValidationException("Удаление не выполнено, пользователи не являются друзьями.");
        } else {
            firstUserFriends.remove(friendID);
            secondUser.getUserFriends().remove(userID);
            log.info("Пользователь с ID {} удалил из списка друзей пользователя с ID {} и теперь они " +
                    "НЕ являются друзьями!", userID, friendID);
        }
    }

    public Set<Long> getAllUserFriends(Long userID) {
        User user = userStorage.getUser(userID);
        if (userID <= 0) {
            throw new ValidationException("ID должен быть больше нуля");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userID + " не найден");
        }

        // Возвращаем копию Set вместо исключения
        return new HashSet<>(user.getUserFriends());
    }

    public Set<Long> getCommonFriends(Long firstUserID, Long secondUserID) {
        User[] users = getTwoUsers(firstUserID, secondUserID);
        User firstUser = users[0];
        User secondUser = users[1];

        Set<Long> firstUserFriends = new HashSet<>(firstUser.getUserFriends());
        Set<Long> secondUserFriends = new HashSet<>(secondUser.getUserFriends());

        Set<Long> commonFriends = new HashSet<>(firstUserFriends);
        commonFriends.retainAll(secondUserFriends);

        return commonFriends; // Возвращаем пустой Set если нет общих друзей
    }

    private User[] getTwoUsers(Long firstID, Long secondID) {
        User firstUser = userStorage.getUser(firstID);
        User secondUser = userStorage.getUser(secondID);
        if (firstID <= 0 || secondID <= 0) {
            throw new ValidationException("ID должен быть больше нуля");
        }
        if (firstUser == null || secondUser == null) {
            long[] usersIDs = new long[2];
            if (firstUser == null) {
                usersIDs[0] = firstID;
            }
            if (secondUser == null) {
                usersIDs[1] = secondID;
            }
            long[] nullUsersIDs = Arrays.stream(usersIDs)
                    .filter(Objects::nonNull)
                    .toArray();
            throw new NotFoundException("Пользователи с ID " + Arrays.toString(nullUsersIDs) + " не найдены");
        }
        return new User[]{firstUser, secondUser};
    }
}