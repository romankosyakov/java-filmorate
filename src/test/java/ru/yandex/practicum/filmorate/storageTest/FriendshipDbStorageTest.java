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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FriendshipDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FriendshipDbStorageTest {

    private final FriendshipDbStorage friendshipDbStorage;
    private final UserDbStorage userDbStorage;

    private Long userId1;
    private Long userId2;

    @Test
    @Order(1)
    void shouldAddFriend() {
        // Create users
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

        User createdUser1 = userDbStorage.addNewUser(user1);
        User createdUser2 = userDbStorage.addNewUser(user2);

        userId1 = createdUser1.getId();
        userId2 = createdUser2.getId();

        // Add friendship
        assertDoesNotThrow(() -> friendshipDbStorage.addFriend(userId1, userId2));

        // Check friendship exists
        List<User> friends = friendshipDbStorage.getFriends(userId1);
        assertEquals(1, friends.size());
        assertEquals(userId2, friends.getFirst().getId());
    }

    @Test
    @Order(2)
    void shouldRemoveFriend() {
        // Ensure users exist from previous test
        if (userId1 == null || userId2 == null) {
            shouldAddFriend(); // Create users if not exists
        }

        // Remove friendship
        assertDoesNotThrow(() -> friendshipDbStorage.removeFriend(userId1, userId2));

        // Check friendship removed
        List<User> friends = friendshipDbStorage.getFriends(userId1);
        assertEquals(0, friends.size());
    }

    @Test
    @Order(3)
    void shouldGetCommonFriends() {
        // Create three users
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

        User createdUser1 = userDbStorage.addNewUser(user1);
        User createdUser2 = userDbStorage.addNewUser(user2);
        User createdUser3 = userDbStorage.addNewUser(user3);

        Long user1Id = createdUser1.getId();
        Long user2Id = createdUser2.getId();
        Long user3Id = createdUser3.getId();

        // User1 and User2 both friend with User3
        friendshipDbStorage.addFriend(user1Id, user3Id);
        friendshipDbStorage.addFriend(user2Id, user3Id);

        List<User> commonFriends = friendshipDbStorage.getCommonFriends(user1Id, user2Id);

        assertEquals(1, commonFriends.size());
        assertEquals(user3Id, commonFriends.getFirst().getId());
    }

    @Test
    @Order(4)
    void shouldNotAddDuplicateFriendship() {
        // Ensure users exist
        if (userId1 == null || userId2 == null) {
            shouldAddFriend();
        }

        // Try to add duplicate friendship
        assertThrows(IllegalArgumentException.class,
                () -> friendshipDbStorage.addFriend(userId1, userId2));
    }

    @Test
    @Order(5)
    void shouldNotAddSelfAsFriend() {
        User user = User.builder()
                .email("self@mail.com")
                .login("selfuser")
                .name("Self User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userDbStorage.addNewUser(user);

        assertThrows(IllegalArgumentException.class,
                () -> friendshipDbStorage.addFriend(createdUser.getId(), createdUser.getId()));
    }
}