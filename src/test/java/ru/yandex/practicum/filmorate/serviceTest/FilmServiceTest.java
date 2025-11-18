package ru.yandex.practicum.filmorate.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private Film film1;
    private Film film2;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(userStorage, filmStorage);

        film1 = Film.builder()
                .name("Film One")
                .description("Description one")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        film2 = Film.builder()
                .name("Film Two")
                .description("Description two")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .build();

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
    void shouldPutLike() {
        Film createdFilm = filmStorage.addNewFilm(film1);
        User createdUser = userStorage.addNewUser(user1);

        assertDoesNotThrow(() -> filmService.putLike(createdFilm.getId(), createdUser.getId()));

        Film filmAfterLike = filmStorage.getFilm(createdFilm.getId());
        assertEquals(1, filmAfterLike.getFilmLikes().size());
        assertTrue(filmAfterLike.getFilmLikes().contains(createdUser.getId()));
    }

    @Test
    void shouldThrowExceptionWhenPuttingDuplicateLike() {
        Film createdFilm = filmStorage.addNewFilm(film1);
        User createdUser = userStorage.addNewUser(user1);

        filmService.putLike(createdFilm.getId(), createdUser.getId());

        assertThrows(ValidationException.class, () -> {
            filmService.putLike(createdFilm.getId(), createdUser.getId());
        });
    }

    @Test
    void shouldThrowExceptionWhenPuttingLikeToNonExistentFilm() {
        User createdUser = userStorage.addNewUser(user1);

        assertThrows(NotFoundException.class, () -> {
            filmService.putLike(999, createdUser.getId());
        });
    }

    @Test
    void shouldThrowExceptionWhenPuttingLikeFromNonExistentUser() {
        Film createdFilm = filmStorage.addNewFilm(film1);

        assertThrows(NotFoundException.class, () -> {
            filmService.putLike(createdFilm.getId(), 999L);
        });
    }

    @Test
    void shouldDeleteLike() {
        Film createdFilm = filmStorage.addNewFilm(film1);
        User createdUser = userStorage.addNewUser(user1);

        filmService.putLike(createdFilm.getId(), createdUser.getId());

        assertDoesNotThrow(() -> filmService.deleteLike(createdFilm.getId(), createdUser.getId()));

        Film filmAfterDelete = filmStorage.getFilm(createdFilm.getId());
        assertEquals(0, filmAfterDelete.getFilmLikes().size());
        assertFalse(filmAfterDelete.getFilmLikes().contains(createdUser.getId()));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentLike() {
        Film createdFilm = filmStorage.addNewFilm(film1);
        User createdUser = userStorage.addNewUser(user1);

        assertThrows(ValidationException.class, () -> {
            filmService.deleteLike(createdFilm.getId(), createdUser.getId());
        });
    }

    @Test
    void shouldShowMostLikedFilms() {
        Film createdFilm1 = filmStorage.addNewFilm(film1);
        Film createdFilm2 = filmStorage.addNewFilm(film2);
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        filmService.putLike(createdFilm1.getId(), createdUser1.getId());
        filmService.putLike(createdFilm1.getId(), createdUser2.getId());
        filmService.putLike(createdFilm2.getId(), createdUser1.getId());

        List<Film> popularFilms = filmService.showMostLikedFilms(2);

        assertEquals(2, popularFilms.size());
        assertEquals(createdFilm1.getId(), popularFilms.get(0).getId());
        assertEquals(createdFilm2.getId(), popularFilms.get(1).getId());
    }

    @Test
    void shouldShowMostLikedFilmsWithDefaultCount() {
        for (int i = 0; i < 15; i++) {
            Film film = Film.builder()
                    .name("Film " + i)
                    .description("Description " + i)
                    .releaseDate(LocalDate.of(2000 + i, 1, 1))
                    .duration(100 + i)
                    .build();
            filmStorage.addNewFilm(film);
        }

        List<Film> popularFilms = filmService.showMostLikedFilms(10);

        assertEquals(10, popularFilms.size());
    }

    @Test
    void shouldShowMostLikedFilmsWhenNoLikes() {
        filmStorage.addNewFilm(film1);
        filmStorage.addNewFilm(film2);

        List<Film> popularFilms = filmService.showMostLikedFilms(2);

        assertEquals(2, popularFilms.size());
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsZero() {
        Film createdFilm = filmStorage.addNewFilm(film1);

        assertThrows(ValidationException.class, () -> {
            filmService.putLike(createdFilm.getId(), 0L);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNegative() {
        Film createdFilm = filmStorage.addNewFilm(film1);

        assertThrows(ValidationException.class, () -> {
            filmService.putLike(createdFilm.getId(), -1L);
        });
    }
}