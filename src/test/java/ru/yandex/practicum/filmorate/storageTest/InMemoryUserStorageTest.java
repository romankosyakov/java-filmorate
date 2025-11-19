package ru.yandex.practicum.filmorate.storageTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserStorageTest {
    private InMemoryUserStorage userStorage;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();

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
    }

    @Test
    void shouldAddNewUser() {
        User createdUser = userStorage.addNewUser(user1);

        assertNotNull(createdUser.getId());
        assertEquals("user1@mail.com", createdUser.getEmail());
        assertEquals("user1", createdUser.getLogin());
        assertEquals("User One", createdUser.getName());
        assertEquals(LocalDate.of(1990, 1, 1), createdUser.getBirthday());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.addNewUser(user);

        assertEquals("testuser", createdUser.getName());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name(null)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.addNewUser(user);

        assertEquals("testuser", createdUser.getName());
    }

    @Test
    void shouldGetUserById() {
        User createdUser = userStorage.addNewUser(user1);
        User foundUser = userStorage.getUser(createdUser.getId());

        assertNotNull(foundUser);
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals(createdUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentUser() {
        assertThrows(NotFoundException.class, () -> {
            userStorage.getUser(999L);
        });
    }

    @Test
    void shouldThrowExceptionWhenGettingUserWithZeroId() {
        assertThrows(ValidationException.class, () -> {
            userStorage.getUser(0L);
        });
    }

    @Test
    void shouldThrowExceptionWhenGettingUserWithNegativeId() {
        assertThrows(ValidationException.class, () -> {
            userStorage.getUser(-1L);
        });
    }

    @Test
    void shouldGetAllUsers() {
        userStorage.addNewUser(user1);
        userStorage.addNewUser(user2);

        List<User> users = userStorage.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void shouldUpdateUser() {
        User createdUser = userStorage.addNewUser(user1);

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@mail.com")
                .login("updateduser")
                .name("Updated User")
                .birthday(LocalDate.of(1992, 1, 1))
                .build();

        User result = userStorage.updateUser(updatedUser);

        assertEquals(createdUser.getId(), result.getId());
        assertEquals("updated@mail.com", result.getEmail());
        assertEquals("updateduser", result.getLogin());
        assertEquals("Updated User", result.getName());
        assertEquals(LocalDate.of(1992, 1, 1), result.getBirthday());
    }

    @Test
    void shouldUpdateUserPartially() {
        User createdUser = userStorage.addNewUser(user1);

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@mail.com")
                .build();

        User result = userStorage.updateUser(updatedUser);

        assertEquals(createdUser.getId(), result.getId());
        assertEquals("updated@mail.com", result.getEmail());
        assertEquals("user1", result.getLogin());
        assertEquals("User One", result.getName());
        assertEquals(LocalDate.of(1990, 1, 1), result.getBirthday());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        User user = User.builder()
                .id(999L)
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(NotFoundException.class, () -> {
            userStorage.updateUser(user);
        });
    }

    @Test
    void shouldGenerateIncrementalIds() {
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        assertEquals(1L, createdUser1.getId());
        assertEquals(2L, createdUser2.getId());
    }

    @Test
    void shouldMaintainUserFriendsAfterUpdate() {
        User createdUser = userStorage.addNewUser(user1);

        // Добавляем друзей
        createdUser.getUserFriends().add(1L);
        createdUser.getUserFriends().add(2L);

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@mail.com")
                .login("updateduser")
                .name("Updated User")
                .birthday(LocalDate.of(1992, 1, 1))
                .build();

        User result = userStorage.updateUser(updatedUser);

        assertEquals(2, result.getUserFriends().size());
        assertTrue(result.getUserFriends().contains(1L));
        assertTrue(result.getUserFriends().contains(2L));
    }
}