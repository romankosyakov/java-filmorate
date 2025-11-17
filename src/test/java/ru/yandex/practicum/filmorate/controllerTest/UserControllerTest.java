package ru.yandex.practicum.filmorate.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.AutisticException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private UserStorage userStorage;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userStorage, userService);
    }

    @Test
    void shouldAddNewUser() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);

        assertNotNull(createdUser.getId());
        assertEquals("test@mail.com", createdUser.getEmail());
        assertEquals("testuser", createdUser.getLogin());
        assertEquals(1, userController.getAllUsers().size());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);

        assertEquals("testuser", createdUser.getName());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name(null)
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);

        assertEquals("testuser", createdUser.getName());
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        userController.addNewUser(user1);
        userController.addNewUser(user2);

        List<User> users = userController.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void shouldGetUserById() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);
        User foundUser = userController.getUser(createdUser.getId());

        assertNotNull(foundUser);
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals("test@mail.com", foundUser.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> {
            userController.getUser(999L);
        });
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder()
                .email("original@mail.com")
                .login("originaluser")
                .name("Original User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@mail.com")
                .login("updateduser")
                .name("Updated User")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User result = userController.updateUser(updatedUser);

        assertEquals(createdUser.getId(), result.getId());
        assertEquals("updated@mail.com", result.getEmail());
        assertEquals("updateduser", result.getLogin());
        assertEquals("Updated User", result.getName());
    }

    @Test
    void shouldUpdateUserPartially() {
        User user = User.builder()
                .email("original@mail.com")
                .login("originaluser")
                .name("Original User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@mail.com")
                .build();

        User result = userController.updateUser(updatedUser);

        assertEquals(createdUser.getId(), result.getId());
        assertEquals("updated@mail.com", result.getEmail());
        assertEquals("originaluser", result.getLogin());
        assertEquals("Original User", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        User user = User.builder()
                .id(999L)
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        assertThrows(NotFoundException.class, () -> {
            userController.updateUser(user);
        });
    }

    @Test
    void shouldGenerateIncrementalIds() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User created1 = userController.addNewUser(user1);
        User created2 = userController.addNewUser(user2);

        assertEquals(1L, created1.getId());
        assertEquals(2L, created2.getId());
    }

    @Test
    void shouldNotChangeUserCountWhenUpdating() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);
        int initialCount = userController.getAllUsers().size();

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@mail.com")
                .login("updateduser")
                .name("Updated User")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        userController.updateUser(updatedUser);
        int finalCount = userController.getAllUsers().size();

        assertEquals(initialCount, finalCount);
    }

    @Test
    void shouldAcceptValidUserForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validuser")
                .name("Valid User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);

        assertNotNull(createdUser);
        assertEquals("valid@email.com", createdUser.getEmail());
    }

    @Test
    void shouldAcceptValidUserForUpdate() {
        User user = User.builder()
                .email("original@mail.com")
                .login("originaluser")
                .name("Original User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@mail.com")
                .login("updateduser")
                .name("Updated User")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User result = userController.updateUser(updatedUser);

        assertNotNull(result);
        assertEquals("updated@mail.com", result.getEmail());
    }

    @Test
    void shouldAddFriend() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);

        assertDoesNotThrow(() -> {
            userController.addFriend(createdUser1.getId(), createdUser2.getId());
        });
    }

    @Test
    void shouldGetUserFriends() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);

        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        Set<Long> friends = userController.getAllUserFriends(createdUser1.getId());
        assertEquals(1, friends.size());
        assertTrue(friends.contains(createdUser2.getId()));
    }

    // Тесты для новых методов

    @Test
    void shouldDeleteFriend() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);

        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        assertDoesNotThrow(() -> {
            userController.deleteFriend(createdUser1.getId(), createdUser2.getId());
        });

        // Проверяем, что друзья удалились
        assertFalse(createdUser1.getUserFriends().contains(createdUser2.getId()));
        assertFalse(createdUser2.getUserFriends().contains(createdUser1.getId()));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentFriend() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);

        assertThrows(ValidationException.class, () -> {
            userController.deleteFriend(createdUser1.getId(), createdUser2.getId());
        });
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User user3 = User.builder()
                .email("user3@mail.com")
                .login("user3")
                .name("User Three")
                .birthday(LocalDate.of(2002, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);
        User createdUser3 = userController.addNewUser(user3);

        userController.addFriend(createdUser1.getId(), createdUser3.getId());
        userController.addFriend(createdUser2.getId(), createdUser3.getId());

        Set<Long> commonFriends = userController.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(createdUser3.getId()));
    }

    @Test
    void shouldReturnEmptyCommonFriendsWhenNoCommonFriends() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User user3 = User.builder()
                .email("user3@mail.com")
                .login("user3")
                .name("User Three")
                .birthday(LocalDate.of(2002, 1, 1))
                .build();

        User user4 = User.builder()
                .email("user4@mail.com")
                .login("user4")
                .name("User Four")
                .birthday(LocalDate.of(2003, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);
        User createdUser3 = userController.addNewUser(user3);
        User createdUser4 = userController.addNewUser(user4);

        userController.addFriend(createdUser1.getId(), createdUser3.getId());
        userController.addFriend(createdUser2.getId(), createdUser4.getId());

        Set<Long> commonFriends = userController.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertEquals(0, commonFriends.size());
    }

    @Test
    void shouldThrowExceptionWhenGettingCommonFriendsForNonExistentUser() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);

        assertThrows(NotFoundException.class, () -> {
            userController.getCommonFriends(createdUser1.getId(), 999L);
        });
    }

    @Test
    void shouldThrowExceptionWhenGettingCommonFriendsForUserWithNoFriends() {
        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);

        assertThrows(AutisticException.class, () -> {
            userController.getCommonFriends(createdUser1.getId(), createdUser2.getId());
        });
    }
}