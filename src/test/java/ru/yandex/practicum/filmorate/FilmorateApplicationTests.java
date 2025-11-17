package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    @Autowired
    private FilmController filmController;

    @Autowired
    private UserController userController;

    @Test
    void contextLoads() {
        assertNotNull(filmController);
        assertNotNull(userController);
    }

    @Test
    void shouldCreateAndRetrieveFilm() {
        Film film = Film.builder()
                .name("Integration Test Film")
                .description("Film for integration testing")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        Film createdFilm = filmController.addNewFilm(film);
        assertNotNull(createdFilm.getId());

        Film retrievedFilm = filmController.getFilm(createdFilm.getId());
        assertEquals(createdFilm.getId(), retrievedFilm.getId());
        assertEquals("Integration Test Film", retrievedFilm.getName());
    }

    @Test
    void shouldCreateAndRetrieveUser() {
        User user = User.builder()
                .email("integration@test.com")
                .login("integration_user")
                .name("Integration User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);
        assertNotNull(createdUser.getId());

        User retrievedUser = userController.getUser(createdUser.getId());
        assertEquals(createdUser.getId(), retrievedUser.getId());
        assertEquals("integration@test.com", retrievedUser.getEmail());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = Film.builder()
                .name("Original Film")
                .description("Original description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        Film createdFilm = filmController.addNewFilm(film);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(150)
                .build();

        Film result = filmController.updateFilm(updatedFilm);
        assertEquals("Updated Film", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(150, result.getDuration());
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder()
                .email("original@test.com")
                .login("original_user")
                .name("Original User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@test.com")
                .login("updated_user")
                .name("Updated User")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        User result = userController.updateUser(updatedUser);
        assertEquals("updated@test.com", result.getEmail());
        assertEquals("updated_user", result.getLogin());
        assertEquals("Updated User", result.getName());
    }

    @Test
    void shouldReturnAllFilms() {
        int initialCount = filmController.getAllFilms().size();

        Film film1 = Film.builder()
                .name("Film One")
                .description("Description one")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        Film film2 = Film.builder()
                .name("Film Two")
                .description("Description two")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(130)
                .build();

        filmController.addNewFilm(film1);
        filmController.addNewFilm(film2);

        int finalCount = filmController.getAllFilms().size();
        assertEquals(initialCount + 2, finalCount);
    }

    @Test
    void shouldReturnAllUsers() {
        int initialCount = userController.getAllUsers().size();

        User user1 = User.builder()
                .email("user1@test.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@test.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        userController.addNewUser(user1);
        userController.addNewUser(user2);

        int finalCount = userController.getAllUsers().size();
        assertEquals(initialCount + 2, finalCount);
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("")
                .birthday(LocalDate.of(1990, 1, 1))
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
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);
        assertEquals("testuser", createdUser.getName());
    }

    @Test
    void shouldGenerateUniqueIdsForFilms() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(130)
                .build();

        Film created1 = filmController.addNewFilm(film1);
        Film created2 = filmController.addNewFilm(film2);

        assertNotEquals(created1.getId(), created2.getId());
        assertTrue(created2.getId() > created1.getId());
    }

    @Test
    void shouldGenerateUniqueIdsForUsers() {
        User user1 = User.builder()
                .email("user1@test.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@test.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        User created1 = userController.addNewUser(user1);
        User created2 = userController.addNewUser(user2);

        assertNotEquals(created1.getId(), created2.getId());
        assertTrue(created2.getId() > created1.getId());
    }

    @Test
    void shouldMaintainDataConsistency() {
        Film film = Film.builder()
                .name("Consistency Test Film")
                .description("Test description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .build();

        User user = User.builder()
                .email("consistency@test.com")
                .login("consistency_user")
                .name("Consistency User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Film createdFilm = filmController.addNewFilm(film);
        User createdUser = userController.addNewUser(user);

        assertEquals(1, filmController.getAllFilms().stream()
                .filter(f -> Objects.equals(f.getId(), createdFilm.getId()))
                .count());

        assertEquals(1, userController.getAllUsers().stream()
                .filter(u -> Objects.equals(u.getId(), createdUser.getId()))
                .count());
    }
}