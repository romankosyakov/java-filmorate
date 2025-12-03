package ru.yandex.practicum.filmorate.serviceTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserService.class, UserDbStorage.class, FriendshipDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    private final UserService userService;
    private final UserDbStorage userStorage;

    @Test
    @Order(1)
    void shouldAddFriend() {
        User user1 = User.builder()
                .email("friend1@mail.com")
                .login("friend1")
                .name("Friend One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("friend2@mail.com")
                .login("friend2")
                .name("Friend Two")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        assertDoesNotThrow(() -> userService.addFriend(createdUser1.getId(), createdUser2.getId()));

        List<User> friends = userService.getAllUserFriends(createdUser1.getId());
        assertEquals(1, friends.size());
        assertEquals(createdUser2.getId(), friends.getFirst().getId());
    }

    @Test
    @Order(2)
    void shouldDeleteFriend() {
        User user1 = User.builder()
                .email("delete1@mail.com")
                .login("delete1")
                .name("Delete One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("delete2@mail.com")
                .login("delete2")
                .name("Delete Two")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        userService.addFriend(createdUser1.getId(), createdUser2.getId());

        assertDoesNotThrow(() -> userService.deleteFriend(createdUser1.getId(), createdUser2.getId()));

        List<User> friends = userService.getAllUserFriends(createdUser1.getId());
        assertEquals(0, friends.size());
    }

    @Test
    @Order(3)
    void shouldGetAllUserFriends() {
        User user1 = User.builder()
                .email("allfriends1@mail.com")
                .login("allfriends1")
                .name("All Friends One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("allfriends2@mail.com")
                .login("allfriends2")
                .name("All Friends Two")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        User user3 = User.builder()
                .email("allfriends3@mail.com")
                .login("allfriends3")
                .name("All Friends Three")
                .birthday(LocalDate.of(1992, 1, 1))
                .build();

        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);
        User createdUser3 = userStorage.addNewUser(user3);

        userService.addFriend(createdUser1.getId(), createdUser2.getId());
        userService.addFriend(createdUser1.getId(), createdUser3.getId());

        List<User> friends = userService.getAllUserFriends(createdUser1.getId());

        assertEquals(2, friends.size());
    }

    @Test
    @Order(4)
    void shouldGetCommonFriends() {
        User user1 = User.builder()
                .email("common1@mail.com")
                .login("common1")
                .name("Common One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("common2@mail.com")
                .login("common2")
                .name("Common Two")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        User user3 = User.builder()
                .email("common3@mail.com")
                .login("common3")
                .name("Common Three")
                .birthday(LocalDate.of(1992, 1, 1))
                .build();

        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);
        User createdUser3 = userStorage.addNewUser(user3);

        userService.addFriend(createdUser1.getId(), createdUser3.getId());
        userService.addFriend(createdUser2.getId(), createdUser3.getId());

        List<User> commonFriends = userService.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(createdUser3.getId(), commonFriends.getFirst().getId());
    }

    @Test
    @Order(5)
    void shouldThrowExceptionWhenAddingSelfAsFriend() {
        User user = User.builder()
                .email("self@mail.com")
                .login("selfuser")
                .name("Self User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.addNewUser(user);

        assertThrows(ValidationException.class,
                () -> userService.addFriend(createdUser.getId(), createdUser.getId()));
    }

    @Test
    @Order(6)
    void shouldReturnEmptyFriendsList() {
        User user = User.builder()
                .email("nofriends@mail.com")
                .login("nofriends")
                .name("No Friends")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.addNewUser(user);

        List<User> friends = userService.getAllUserFriends(createdUser.getId());
        assertTrue(friends.isEmpty());
    }

    @Test
    @Order(7)
    void shouldReturnEmptyCommonFriends() {
        User user1 = User.builder()
                .email("nocommon1@mail.com")
                .login("nocommon1")
                .name("No Common One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("nocommon2@mail.com")
                .login("nocommon2")
                .name("No Common Two")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        List<User> commonFriends = userService.getCommonFriends(createdUser1.getId(), createdUser2.getId());
        assertTrue(commonFriends.isEmpty());
    }
}