package ru.yandex.practicum.filmorate.serviceTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmServiceTest {

    private final FilmService filmService;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final MpaDbStorage mpaDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        clearDatabase();
    }

    private void clearDatabase() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void shouldPutLike() {
        User user = User.builder()
                .email("like@mail.com")
                .login("likeuser")
                .name("Like User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userDbStorage.addNewUser(user);

        Mpa mpa = mpaDbStorage.getMpaById(1);
        Film film = Film.builder()
                .name("Film for Like")
                .description("Description for like")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();
        Film createdFilm = filmDbStorage.addNewFilm(film);

        assertDoesNotThrow(() -> filmService.putLike(createdFilm.getId(), createdUser.getId()));

        Film filmAfterLike = filmDbStorage.getFilm(createdFilm.getId());
        assertTrue(filmAfterLike.getLikes().contains(createdUser.getId()));
    }

    @Test
    void shouldDeleteLike() {
        User user = User.builder()
                .email("unlike@mail.com")
                .login("unlikeuser")
                .name("Unlike User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userDbStorage.addNewUser(user);

        Mpa mpa = mpaDbStorage.getMpaById(1);
        Film film = Film.builder()
                .name("Film for Unlike")
                .description("Description for unlike")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();
        Film createdFilm = filmDbStorage.addNewFilm(film);

        filmService.putLike(createdFilm.getId(), createdUser.getId());

        Film filmWithLike = filmDbStorage.getFilm(createdFilm.getId());
        assertTrue(filmWithLike.getLikes().contains(createdUser.getId()));

        assertDoesNotThrow(() -> filmService.deleteLike(createdFilm.getId(), createdUser.getId()));

        Film filmAfterUnlike = filmDbStorage.getFilm(createdFilm.getId());
        assertFalse(filmAfterUnlike.getLikes().contains(createdUser.getId()));
    }

    @Test
    void shouldShowMostLikedFilms() {
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
        User createdUser1 = userDbStorage.addNewUser(user1);
        User createdUser2 = userDbStorage.addNewUser(user2);

        Mpa mpa1 = mpaDbStorage.getMpaById(1);
        Mpa mpa2 = mpaDbStorage.getMpaById(2);
        Film film1 = Film.builder()
                .name("Popular Film")
                .description("Very popular film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa1)
                .build();
        Film film2 = Film.builder()
                .name("Less Popular Film")
                .description("Less popular film")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .mpa(mpa2)
                .build();
        Film createdFilm1 = filmDbStorage.addNewFilm(film1);
        Film createdFilm2 = filmDbStorage.addNewFilm(film2);

        filmService.putLike(createdFilm1.getId(), createdUser1.getId());
        filmService.putLike(createdFilm1.getId(), createdUser2.getId());
        filmService.putLike(createdFilm2.getId(), createdUser1.getId());

        List<Film> popularFilms = filmService.showMostLikedFilms(10);
        assertNotNull(popularFilms);
        assertTrue(popularFilms.size() >= 2);

        Film mostPopular = popularFilms.getFirst();
        assertEquals(createdFilm1.getId(), mostPopular.getId());
        assertEquals(2, mostPopular.getLikes().size());
    }

    @Test
    void shouldThrowExceptionWhenPuttingLikeToNonExistentFilm() {
        User user = User.builder()
                .email("test@mail.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userDbStorage.addNewUser(user);

        assertThrows(NotFoundException.class, () -> filmService.putLike(9999, createdUser.getId()));
    }

    @Test
    void shouldThrowExceptionWhenPuttingLikeFromNonExistentUser() {
        Mpa mpa = mpaDbStorage.getMpaById(1);
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();
        Film createdFilm = filmDbStorage.addNewFilm(film);

        assertThrows(NotFoundException.class, () -> filmService.putLike(createdFilm.getId(), 9999L));
    }

    @Test
    void shouldThrowExceptionWhenPuttingDuplicateLike() {
        User user = User.builder()
                .email("duplicate@mail.com")
                .login("duplicateuser")
                .name("Duplicate User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userDbStorage.addNewUser(user);

        Mpa mpa = mpaDbStorage.getMpaById(1);
        Film film = Film.builder()
                .name("Duplicate Like Film")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();
        Film createdFilm = filmDbStorage.addNewFilm(film);

        assertDoesNotThrow(() -> filmService.putLike(createdFilm.getId(), createdUser.getId()));
        assertThrows(ValidationException.class, () -> filmService.putLike(createdFilm.getId(), createdUser.getId()));
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentLike() {
        User user = User.builder()
                .email("nodelete@mail.com")
                .login("nodeleteuser")
                .name("No Delete User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userDbStorage.addNewUser(user);

        Mpa mpa = mpaDbStorage.getMpaById(1);
        Film film = Film.builder()
                .name("No Delete Film")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(Collections.emptyList())
                .build();
        Film createdFilm = filmDbStorage.addNewFilm(film);

        assertDoesNotThrow(() -> filmService.deleteLike(createdFilm.getId(), createdUser.getId()));
    }

    @Test
    void shouldHandleNullAndEmptyGenres() {
        Mpa mpa = mpaDbStorage.getMpaById(1);

        Film filmWithNullGenres = Film.builder()
                .name("Null Genres Film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .description("Description")
                .duration(100)
                .mpa(mpa)
                .build();

        filmWithNullGenres.setGenres(Collections.emptyList());

        Film createdFilm1 = filmDbStorage.addNewFilm(filmWithNullGenres);
        assertNotNull(createdFilm1.getGenres());
        assertTrue(createdFilm1.getGenres().isEmpty());
    }
}