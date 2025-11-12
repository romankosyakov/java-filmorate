package ru.yandex.practicum.filmorate.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
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

        assertNotNull(createdUser.getId(), "Пользователь должен получить ID");
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

        assertEquals("testuser", createdUser.getName(), "Должен использоваться логин, когда имя пустое");
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

        assertEquals("testuser", createdUser.getName(), "Должен использоваться логин, когда имя null");
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
        assertEquals("user1", users.get(0).getLogin());
        assertEquals("user2", users.get(1).getLogin());
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
        }, "Должно выбрасываться исключение при поиске несуществующего пользователя");
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

        // Обновляем только email
        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@mail.com")
                .build();

        User result = userController.updateUser(updatedUser);

        assertEquals(createdUser.getId(), result.getId());
        assertEquals("updated@mail.com", result.getEmail());
        assertEquals("originaluser", result.getLogin()); // остался прежним
        assertEquals("Original User", result.getName()); // осталось прежним
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
        }, "Должно выбрасываться исключение при обновлении несуществующего пользователя");
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

        assertEquals(initialCount, finalCount, "Количество пользователей не должно меняться при обновлении");
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
}