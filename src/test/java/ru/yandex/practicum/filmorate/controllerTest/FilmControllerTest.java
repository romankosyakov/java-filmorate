package ru.yandex.practicum.filmorate.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.filmorate.controller.FilmController;
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

@SpringBootTest
@TestPropertySource(properties = {"mostLikedCount=2"})
class FilmControllerTest {
    private FilmController filmController;
    private FilmService filmService;
    private UserStorage userStorage;
    private FilmStorage filmStorage;

    @Value("${mostLikedCount}")
    private int defaultCount;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(userStorage, filmStorage);
        filmController = new FilmController(defaultCount, filmService);
    }

    @Test
    void shouldAddNewFilm() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film createdFilm = filmController.addNewFilm(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        assertEquals(1, filmController.getAllFilms().size());
    }

    @Test
    void shouldGetAllFilms() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .build();

        filmController.addNewFilm(film1);
        filmController.addNewFilm(film2);

        List<Film> films = filmController.getAllFilms();

        assertEquals(2, films.size());
    }

    @Test
    void shouldGetFilmById() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film createdFilm = filmController.addNewFilm(film);
        Film foundFilm = filmController.getFilm(createdFilm.getId());

        assertNotNull(foundFilm);
        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Test Film", foundFilm.getName());
    }

    @Test
    void shouldThrowExceptionWhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> {
            filmController.getFilm(999);
        });
    }

    @Test
    void shouldUpdateFilm() {
        Film film = Film.builder()
                .name("Original Film")
                .description("Original Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film createdFilm = filmController.addNewFilm(film);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .description("Updated Description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .build();

        Film result = filmController.updateFilm(updatedFilm);

        assertEquals(createdFilm.getId(), result.getId());
        assertEquals("Updated Film", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(150, result.getDuration());
    }


    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentFilm() {
        Film film = Film.builder()
                .id(999)
                .name("Non-existent Film")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        assertThrows(NotFoundException.class, () -> {
            filmController.updateFilm(film);
        });
    }

    @Test
    void shouldGenerateIncrementalIds() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .build();

        Film created1 = filmController.addNewFilm(film1);
        Film created2 = filmController.addNewFilm(film2);

        assertEquals(1, created1.getId());
        assertEquals(2, created2.getId());
    }

    @Test
    void shouldNotChangeFilmCountWhenUpdating() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film createdFilm = filmController.addNewFilm(film);
        int initialCount = filmController.getAllFilms().size();

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .description("Updated Description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .build();

        filmController.updateFilm(updatedFilm);
        int finalCount = filmController.getAllFilms().size();

        assertEquals(initialCount, finalCount);
    }

    @Test
    void shouldAcceptValidFilmForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description within 200 chars")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film createdFilm = filmController.addNewFilm(film);

        assertNotNull(createdFilm);
        assertEquals("Valid Film", createdFilm.getName());
    }

    @Test
    void shouldAcceptValidFilmForUpdate() {
        Film film = Film.builder()
                .name("Original Film")
                .description("Original Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film createdFilm = filmController.addNewFilm(film);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .build();

        Film result = filmController.updateFilm(updatedFilm);

        assertNotNull(result);
        assertEquals("Updated Film", result.getName());
        assertEquals("Updated description", result.getDescription());
    }

    @Test
    void shouldReturnCreatedStatusForNewFilm() {
        Film film = Film.builder()
                .name("New Film")
                .description("New Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film createdFilm = filmController.addNewFilm(film);

        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getId());
    }

    // Тесты для новых методов

    @Test
    void shouldPutLike() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film createdFilm = filmController.addNewFilm(film);

        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser = userStorage.addNewUser(user);

        assertDoesNotThrow(() -> filmController.putLike(createdFilm.getId(), createdUser.getId()));

        Film filmAfterLike = filmController.getFilm(createdFilm.getId());
        assertEquals(1, filmAfterLike.getFilmLikes().size());
        assertTrue(filmAfterLike.getFilmLikes().contains(createdUser.getId()));
    }

    @Test
    void shouldThrowExceptionWhenPuttingLikeToNonExistentFilm() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser = userStorage.addNewUser(user);

        assertThrows(NotFoundException.class, () -> {
            filmController.putLike(999, createdUser.getId());
        });
    }

    @Test
    void shouldThrowExceptionWhenPuttingLikeFromNonExistentUser() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film createdFilm = filmController.addNewFilm(film);

        assertThrows(NotFoundException.class, () -> {
            filmController.putLike(createdFilm.getId(), 999L);
        });
    }

    @Test
    void shouldDeleteLike() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film createdFilm = filmController.addNewFilm(film);

        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser = userStorage.addNewUser(user);

        filmController.putLike(createdFilm.getId(), createdUser.getId());

        assertDoesNotThrow(() -> filmController.deleteLike(createdFilm.getId(), createdUser.getId()));

        Film filmAfterDelete = filmController.getFilm(createdFilm.getId());
        assertEquals(0, filmAfterDelete.getFilmLikes().size());
        assertFalse(filmAfterDelete.getFilmLikes().contains(createdUser.getId()));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentLike() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film createdFilm = filmController.addNewFilm(film);

        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser = userStorage.addNewUser(user);

        assertThrows(ValidationException.class, () -> {
            filmController.deleteLike(createdFilm.getId(), createdUser.getId());
        });
    }

    @Test
    void shouldShowMostLikedFilmsWithDefaultCount() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .build();
        Film film3 = Film.builder()
                .name("Film 3")
                .description("Description 3")
                .releaseDate(LocalDate.of(2002, 1, 1))
                .duration(140)
                .build();

        Film createdFilm1 = filmController.addNewFilm(film1);
        Film createdFilm2 = filmController.addNewFilm(film2);

        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        User createdUser = userStorage.addNewUser(user);

        filmController.putLike(createdFilm1.getId(), createdUser.getId());
        filmController.putLike(createdFilm2.getId(), createdUser.getId());

        List<Film> popularFilms = filmController.showMostLikedFilms(null);

        assertEquals(defaultCount, popularFilms.size());
    }

    @Test
    void shouldShowMostLikedFilmsWithCustomCount() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .build();
        Film film3 = Film.builder()
                .name("Film 3")
                .description("Description 3")
                .releaseDate(LocalDate.of(2002, 1, 1))
                .duration(140)
                .build();

        Film createdFilm1 = filmController.addNewFilm(film1);
        Film createdFilm2 = filmController.addNewFilm(film2);

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
        User createdUser1 = userStorage.addNewUser(user1);
        User createdUser2 = userStorage.addNewUser(user2);

        filmController.putLike(createdFilm1.getId(), createdUser1.getId());
        filmController.putLike(createdFilm1.getId(), createdUser2.getId());
        filmController.putLike(createdFilm2.getId(), createdUser1.getId());

        List<Film> popularFilms = filmController.showMostLikedFilms(1);

        assertEquals(1, popularFilms.size());
        assertEquals(createdFilm1.getId(), popularFilms.getFirst().getId());
    }

    @Test
    void shouldShowMostLikedFilmsWhenNoLikes() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .build();

        filmController.addNewFilm(film1);
        filmController.addNewFilm(film2);

        List<Film> popularFilms = filmController.showMostLikedFilms(2);

        assertEquals(2, popularFilms.size());
    }

}