package ru.yandex.practicum.filmorate.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.AutisticException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;
    private UserStorage userStorage;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);

        user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        user3 = User.builder()
                .email("user3@mail.com")
                .login("user3")
                .name("User Three")
                .birthday(LocalDate.of(1992, 1, 1))
                .build();
    }

    @Test
    void shouldAddFriend() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        assertDoesNotThrow(() -> userService.addFriend(createdUser1.getId(), createdUser2.getId()));

        Set<Long> user1Friends = userService.getAllUserFriends(createdUser1.getId());
        Set<Long> user2Friends = userService.getAllUserFriends(createdUser2.getId());

        assertEquals(1, user1Friends.size());
        assertEquals(1, user2Friends.size());
        assertTrue(user1Friends.contains(createdUser2.getId()));
        assertTrue(user2Friends.contains(createdUser1.getId()));
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateFriend() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        userService.addFriend(createdUser1.getId(), createdUser2.getId());

        assertThrows(ValidationException.class, () -> {
            userService.addFriend(createdUser1.getId(), createdUser2.getId());
        });
    }

    @Test
    void shouldThrowExceptionWhenAddingSelfAsFriend() {
        User createdUser1 = userStorage.addNewUser(user1);

        assertThrows(AutisticException.class, () -> {
            userService.addFriend(createdUser1.getId(), createdUser1.getId());
        });
    }

    @Test
    void shouldDeleteFriend() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        userService.addFriend(createdUser1.getId(), createdUser2.getId());

        // Проверяем, что друзья добавились
        Set<Long> user1FriendsBefore = createdUser1.getUserFriends();
        Set<Long> user2FriendsBefore = createdUser2.getUserFriends();
        assertEquals(1, user1FriendsBefore.size());
        assertEquals(1, user2FriendsBefore.size());

        assertDoesNotThrow(() -> userService.deleteFriend(createdUser1.getId(), createdUser2.getId()));

        // Проверяем напрямую через объекты, а не через сервис (чтобы избежать исключения)
        Set<Long> user1FriendsAfter = createdUser1.getUserFriends();
        Set<Long> user2FriendsAfter = createdUser2.getUserFriends();

        assertEquals(0, user1FriendsAfter.size());
        assertEquals(0, user2FriendsAfter.size());

        // Проверяем, что сервис бросает исключение при попытке получить пустой список друзей
        assertThrows(AutisticException.class, () -> {
            userService.getAllUserFriends(createdUser1.getId());
        });
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentFriend() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        assertThrows(ValidationException.class, () -> {
            userService.deleteFriend(createdUser1.getId(), createdUser2.getId());
        });
    }

    @Test
    void shouldGetAllUserFriends() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);
        User createdUser3 = userStorage.addNewUser(user3);

        userService.addFriend(createdUser1.getId(), createdUser2.getId());
        userService.addFriend(createdUser1.getId(), createdUser3.getId());

        Set<Long> friends = userService.getAllUserFriends(createdUser1.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.contains(createdUser2.getId()));
        assertTrue(friends.contains(createdUser3.getId()));
    }

    @Test
    void shouldThrowExceptionWhenGettingFriendsForNonExistentUser() {
        assertThrows(NotFoundException.class, () -> {
            userService.getAllUserFriends(999L);
        });
    }

    @Test
    void shouldThrowExceptionWhenGettingFriendsForUserWithNoFriends() {
        User createdUser1 = userStorage.addNewUser(user1);

        assertThrows(AutisticException.class, () -> {
            userService.getAllUserFriends(createdUser1.getId());
        });
    }

    @Test
    void shouldGetCommonFriends() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);
        User createdUser3 = userStorage.addNewUser(user3);

        userService.addFriend(createdUser1.getId(), createdUser3.getId());
        userService.addFriend(createdUser2.getId(), createdUser3.getId());

        Set<Long> commonFriends = userService.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(createdUser3.getId()));
    }

    @Test
    void shouldThrowExceptionWhenGettingCommonFriendsForNonExistentUser() {
        User createdUser1 = userStorage.addNewUser(user1);

        assertThrows(NotFoundException.class, () -> {
            userService.getCommonFriends(createdUser1.getId(), 999L);
        });
    }

    @Test
    void shouldThrowExceptionWhenGettingCommonFriendsForUserWithNoFriends() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        assertThrows(AutisticException.class, () -> {
            userService.getCommonFriends(createdUser1.getId(), createdUser2.getId());
        });
    }

    @Test
    void shouldReturnEmptyCommonFriendsWhenNoCommonFriends() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);
        User createdUser3 = userStorage.addNewUser(user3);
        User user4 = User.builder()
                .email("user4@mail.com")
                .login("user4")
                .name("User Four")
                .birthday(LocalDate.of(1993, 1, 1))
                .build();
        User createdUser4 = userStorage.addNewUser(user4);

        userService.addFriend(createdUser1.getId(), createdUser3.getId());
        userService.addFriend(createdUser2.getId(), createdUser4.getId());

        Set<Long> commonFriends = userService.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertEquals(0, commonFriends.size());
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsZero() {
        assertThrows(ValidationException.class, () -> {
            userService.getAllUserFriends(0L);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNegative() {
        assertThrows(ValidationException.class, () -> {
            userService.getAllUserFriends(-1L);
        });
    }
}