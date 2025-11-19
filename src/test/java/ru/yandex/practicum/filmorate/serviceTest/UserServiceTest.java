package ru.yandex.practicum.filmorate.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

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

        List<User> user1Friends = userService.getAllUserFriends(createdUser1.getId());
        List<User> user2Friends = userService.getAllUserFriends(createdUser2.getId());

        assertEquals(1, user1Friends.size());
        assertEquals(1, user2Friends.size());
        assertTrue(user1Friends.contains(createdUser2));
        assertTrue(user2Friends.contains(createdUser1));
    }

    @Test
    void shouldThrowExceptionWhenAddingSelfAsFriend() {
        User createdUser1 = userStorage.addNewUser(user1);

        assertThrows(ValidationException.class, () -> {
            userService.addFriend(createdUser1.getId(), createdUser1.getId());
        });
    }

    @Test
    void shouldDeleteFriend() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        userService.addFriend(createdUser1.getId(), createdUser2.getId());

        assertDoesNotThrow(() -> userService.deleteFriend(createdUser1.getId(), createdUser2.getId()));

        List<User> user1Friends = userService.getAllUserFriends(createdUser1.getId());
        List<User> user2Friends = userService.getAllUserFriends(createdUser2.getId());

        assertEquals(0, user1Friends.size());
        assertEquals(0, user2Friends.size());
    }

    @Test
    void shouldGetAllUserFriends() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);
        User createdUser3 = userStorage.addNewUser(user3);

        userService.addFriend(createdUser1.getId(), createdUser2.getId());
        userService.addFriend(createdUser1.getId(), createdUser3.getId());

        List<User> friends = userService.getAllUserFriends(createdUser1.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.contains(createdUser2));
        assertTrue(friends.contains(createdUser3));
    }

    @Test
    void shouldReturnEmptyFriendsList() {
        User createdUser1 = userStorage.addNewUser(user1);

        List<User> friends = userService.getAllUserFriends(createdUser1.getId());

        assertEquals(0, friends.size());
        assertTrue(friends.isEmpty());
    }

    @Test
    void shouldGetCommonFriends() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);
        User createdUser3 = userStorage.addNewUser(user3);

        userService.addFriend(createdUser1.getId(), createdUser3.getId());
        userService.addFriend(createdUser2.getId(), createdUser3.getId());

        List<User> commonFriends = userService.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(createdUser3));
    }

    @Test
    void shouldReturnEmptyCommonFriends() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        List<User> commonFriends = userService.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertEquals(0, commonFriends.size());
        assertTrue(commonFriends.isEmpty());
    }
}