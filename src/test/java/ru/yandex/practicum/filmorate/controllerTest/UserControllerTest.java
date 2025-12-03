package ru.yandex.practicum.filmorate.controllerTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserControllerTest {

    private final UserController userController;

    @Test
    @Order(1)
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
        assertEquals("Test User", createdUser.getName());
        assertEquals(5, userController.getAllUsers().size());
    }

    @Test
    @Order(2)
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = User.builder()
                .email("empty@mail.com")
                .login("emptyuser")
                .name("")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);
        assertEquals("emptyuser", createdUser.getName());
    }

    @Test
    @Order(3)
    void shouldUseLoginAsNameWhenNameIsNull() {
        User user = User.builder()
                .email("null@mail.com")
                .login("nulluser")
                .name(null)
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);
        assertEquals("nulluser", createdUser.getName());
    }

    @Test
    @Order(4)
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
        assertTrue(users.size() >= 3); // Including previous tests
    }

    @Test
    @Order(5)
    void shouldGetUserById() {
        User user = User.builder()
                .email("get@mail.com")
                .login("getuser")
                .name("Get User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);
        User foundUser = userController.getUser(createdUser.getId());

        assertNotNull(foundUser);
        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals("get@mail.com", foundUser.getEmail());
    }

    @Test
    @Order(6)
    void shouldThrowExceptionWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> userController.getUser(9999L));
    }

    @Test
    @Order(7)
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
    @Order(8)
    void shouldAddFriend() {
        User user1 = User.builder()
                .email("friend1@mail.com")
                .login("friend1")
                .name("Friend One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("friend2@mail.com")
                .login("friend2")
                .name("Friend Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);

        assertDoesNotThrow(() -> {
            userController.addFriend(createdUser1.getId(), createdUser2.getId());
        });
    }

    @Test
    @Order(9)
    void shouldGetUserFriends() {
        User user1 = User.builder()
                .email("friends1@mail.com")
                .login("friends1")
                .name("Friends One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("friends2@mail.com")
                .login("friends2")
                .name("Friends Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);

        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        List<User> friends = userController.getAllUserFriends(createdUser1.getId());
        assertEquals(1, friends.size());
        assertEquals(createdUser2.getId(), friends.getFirst().getId());
    }

    @Test
    @Order(10)
    void shouldDeleteFriend() {
        User user1 = User.builder()
                .email("delete1@mail.com")
                .login("delete1")
                .name("Delete One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("delete2@mail.com")
                .login("delete2")
                .name("Delete Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);

        userController.addFriend(createdUser1.getId(), createdUser2.getId());

        assertDoesNotThrow(() -> {
            userController.deleteFriend(createdUser1.getId(), createdUser2.getId());
        });

        List<User> friends = userController.getAllUserFriends(createdUser1.getId());
        assertEquals(0, friends.size());
    }

    @Test
    @Order(11)
    void shouldGetCommonFriends() {
        User user1 = User.builder()
                .email("common1@mail.com")
                .login("common1")
                .name("Common One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("common2@mail.com")
                .login("common2")
                .name("Common Two")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        User user3 = User.builder()
                .email("common3@mail.com")
                .login("common3")
                .name("Common Three")
                .birthday(LocalDate.of(2002, 1, 1))
                .build();

        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);
        User createdUser3 = userController.addNewUser(user3);

        userController.addFriend(createdUser1.getId(), createdUser3.getId());
        userController.addFriend(createdUser2.getId(), createdUser3.getId());

        List<User> commonFriends = userController.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(createdUser3.getId(), commonFriends.getFirst().getId());
    }

    @Test
    @Order(12)
    void shouldThrowExceptionWhenAddingSelfAsFriend() {
        User user = User.builder()
                .email("self@mail.com")
                .login("selfuser")
                .name("Self User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);

        assertThrows(ValidationException.class, () -> {
            userController.addFriend(createdUser.getId(), createdUser.getId());
        });
    }
}