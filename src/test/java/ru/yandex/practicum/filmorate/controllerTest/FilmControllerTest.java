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
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class FilmControllerTest {

    private final FilmController filmController;
    private final UserController userController;

    @Test
    @Order(1)
    void shouldAddNewFilm() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmController.addNewFilm(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        assertEquals("Test Description", createdFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
        assertEquals(120, createdFilm.getDuration());
        assertNotNull(createdFilm.getMpa());
        assertEquals(1, createdFilm.getMpa().getId());
    }

    @Test
    @Order(2)
    void shouldAddFilmWithGenres() {
        Film film = Film.builder()
                .name("Film With Genres")
                .description("Film with multiple genres")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .genres(List.of(
                        Genre.builder().id(1).build(),
                        Genre.builder().id(2).build()
                ))
                .build();

        Film createdFilm = filmController.addNewFilm(film);

        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getGenres());
        assertEquals(2, createdFilm.getGenres().size());
    }

    @Test
    @Order(3)
    void shouldGetAllFilms() {
        Film film1 = Film.builder()
                .name("Film One")
                .description("Description one")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film film2 = Film.builder()
                .name("Film Two")
                .description("Description two")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .mpa(Mpa.builder().id(2).build())
                .build();

        filmController.addNewFilm(film1);
        filmController.addNewFilm(film2);

        List<Film> films = filmController.getAllFilms();
        assertTrue(films.size() >= 3); // Including previous tests
    }

    @Test
    @Order(4)
    void shouldGetFilmById() {
        Film film = Film.builder()
                .name("Film For Get")
                .description("Description for get")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmController.addNewFilm(film);
        Film foundFilm = filmController.getFilm(createdFilm.getId());

        assertNotNull(foundFilm);
        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Film For Get", foundFilm.getName());
    }

    @Test
    @Order(5)
    void shouldThrowExceptionWhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> filmController.getFilm(9999));
    }

    @Test
    @Order(6)
    void shouldUpdateFilm() {
        Film film = Film.builder()
                .name("Original Film")
                .description("Original description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmController.addNewFilm(film);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .mpa(Mpa.builder().id(2).build())
                .build();

        Film result = filmController.updateFilm(updatedFilm);

        assertEquals(createdFilm.getId(), result.getId());
        assertEquals("Updated Film", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2001, 1, 1), result.getReleaseDate());
        assertEquals(150, result.getDuration());
        assertEquals(2, result.getMpa().getId());
    }

    @Test
    @Order(7)
    void shouldPutLike() {
        Film film = Film.builder()
                .name("Film for Like")
                .description("Description for like")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        User user = User.builder()
                .email("like@mail.com")
                .login("likeuser")
                .name("Like User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Film createdFilm = filmController.addNewFilm(film);
        User createdUser = userController.addNewUser(user);

        assertDoesNotThrow(() -> filmController.putLike(createdFilm.getId(), createdUser.getId()));
    }

    @Test
    @Order(8)
    void shouldDeleteLike() {
        Film film = Film.builder()
                .name("Film for Unlike")
                .description("Description for unlike")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        User user = User.builder()
                .email("unlike@mail.com")
                .login("unlikeuser")
                .name("Unlike User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Film createdFilm = filmController.addNewFilm(film);
        User createdUser = userController.addNewUser(user);

        filmController.putLike(createdFilm.getId(), createdUser.getId());
        assertDoesNotThrow(() -> filmController.deleteLike(createdFilm.getId(), createdUser.getId()));
    }

    @Test
    @Order(9)
    void shouldShowMostLikedFilms() {
        Film film1 = Film.builder()
                .name("Popular Film")
                .description("Very popular film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film film2 = Film.builder()
                .name("Less Popular Film")
                .description("Less popular film")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .mpa(Mpa.builder().id(2).build())
                .build();

        User user1 = User.builder()
                .email("popular1@mail.com")
                .login("popular1")
                .name("Popular One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("popular2@mail.com")
                .login("popular2")
                .name("Popular Two")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        Film createdFilm1 = filmController.addNewFilm(film1);
        Film createdFilm2 = filmController.addNewFilm(film2);
        User createdUser1 = userController.addNewUser(user1);
        User createdUser2 = userController.addNewUser(user2);

        // Film1 gets 2 likes, Film2 gets 1 like
        filmController.putLike(createdFilm1.getId(), createdUser1.getId());
        filmController.putLike(createdFilm1.getId(), createdUser2.getId());
        filmController.putLike(createdFilm2.getId(), createdUser1.getId());

        List<Film> popularFilms = filmController.getPopularFilms(2);

        assertEquals(2, popularFilms.size());
    }

    @Test
    @Order(10)
    void shouldThrowExceptionWhenPuttingLikeToNonExistentFilm() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userController.addNewUser(user);

        assertThrows(NotFoundException.class, () -> filmController.putLike(9999, createdUser.getId()));
    }

    @Test
    @Order(11)
    void shouldThrowExceptionWhenPuttingLikeFromNonExistentUser() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmController.addNewFilm(film);

        assertThrows(NotFoundException.class, () -> filmController.putLike(createdFilm.getId(), 9999L));
    }

    @Test
    @Order(12)
    void shouldThrowExceptionWhenPuttingDuplicateLike() {
        Film film = Film.builder()
                .name("Duplicate Like Film")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        User user = User.builder()
                .email("duplicate@mail.com")
                .login("duplicateuser")
                .name("Duplicate User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Film createdFilm = filmController.addNewFilm(film);
        User createdUser = userController.addNewUser(user);

        filmController.putLike(createdFilm.getId(), createdUser.getId());

        assertThrows(ValidationException.class, () -> filmController.putLike(createdFilm.getId(), createdUser.getId()));
    }
}