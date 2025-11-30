package ru.yandex.practicum.filmorate.storageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    @Order(1)
    void shouldAddNewUser() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.addNewUser(user);

        assertNotNull(createdUser.getId());
        assertEquals("test@mail.com", createdUser.getEmail());
        assertEquals("testuser", createdUser.getLogin());
        assertEquals("Test User", createdUser.getName());
        assertEquals(LocalDate.of(1990, 1, 1), createdUser.getBirthday());
    }

    @Test
    @Order(2)
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
    @Order(3)
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
    @Order(4)
    void shouldGetUserById() {
        User user = User.builder()
                .email("get@mail.com")
                .login("getuser")
                .name("Get User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.addNewUser(user);
        User foundUser = userStorage.getUser(createdUser.getId());

        assertNotNull(foundUser);
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals("get@mail.com", foundUser.getEmail());
        assertEquals("getuser", foundUser.getLogin());
    }

    @Test
    @Order(5)
    void shouldThrowExceptionWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.getUser(9999L));
    }

    @Test
    @Order(6)
    void shouldGetAllUsers() {
        int initialSize = userStorage.getAllUsers().size();

        User user1 = User.builder()
                .email("user1@mail.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        userStorage.addNewUser(user1);
        userStorage.addNewUser(user2);

        List<User> users = userStorage.getAllUsers();
        assertTrue(users.size() >= initialSize + 2);
    }

    @Test
    @Order(7)
    void shouldUpdateUser() {
        User user = User.builder()
                .email("original@mail.com")
                .login("originaluser")
                .name("Original User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.addNewUser(user);

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@mail.com")
                .login("updateduser")
                .name("Updated User")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        User result = userStorage.updateUser(updatedUser);

        assertEquals(createdUser.getId(), result.getId());
        assertEquals("updated@mail.com", result.getEmail());
        assertEquals("updateduser", result.getLogin());
        assertEquals("Updated User", result.getName());
        assertEquals(LocalDate.of(1991, 1, 1), result.getBirthday());
    }

    @Test
    @Order(8)
    void shouldUpdateUserPartially() {
        User user = User.builder()
                .email("partial@mail.com")
                .login("partialuser")
                .name("Partial User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.addNewUser(user);

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updatedpartial@mail.com")
                .build();

        User result = userStorage.updateUser(updatedUser);

        assertEquals(createdUser.getId(), result.getId());
        assertEquals("updatedpartial@mail.com", result.getEmail());
        assertEquals("partialuser", result.getLogin()); // Should remain unchanged
        assertEquals("Partial User", result.getName()); // Should remain unchanged
    }

    @Test
    @Order(9)
    void shouldCheckUserExists() {
        User user = User.builder()
                .email("exists@mail.com")
                .login("existsuser")
                .name("Exists User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.addNewUser(user);

        assertTrue(userStorage.userExists(createdUser.getId()));
        assertFalse(userStorage.userExists(9999L));
    }
}