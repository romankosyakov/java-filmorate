package ru.yandex.practicum.filmorate.serviceTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmServiceTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(userDbStorage, filmDbStorage);
    }

    @Test
    @Order(1)
    void shouldPutLike() {
        User user = User.builder()
                .email("like@mail.com")
                .login("likeuser")
                .name("Like User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userDbStorage.addNewUser(user);

        Film film = Film.builder()
                .name("Film for Like")
                .description("Description for like")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmDbStorage.addNewFilm(film);

        assertDoesNotThrow(() -> filmService.putLike(createdFilm.getId(), createdUser.getId()));

        Film filmAfterLike = filmDbStorage.getFilm(createdFilm.getId());
        assertTrue(filmAfterLike.getLikes().contains(createdUser.getId()));
    }

    @Test
    @Order(2)
    void shouldDeleteLike() {
        User user = User.builder()
                .email("unlike@mail.com")
                .login("unlikeuser")
                .name("Unlike User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userDbStorage.addNewUser(user);

        Film film = Film.builder()
                .name("Film for Unlike")
                .description("Description for unlike")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmDbStorage.addNewFilm(film);

        filmService.putLike(createdFilm.getId(), createdUser.getId());

        assertDoesNotThrow(() -> filmService.deleteLike(createdFilm.getId(), createdUser.getId()));

        Film filmAfterUnlike = filmDbStorage.getFilm(createdFilm.getId());
        assertFalse(filmAfterUnlike.getLikes().contains(createdUser.getId()));
    }

    @Test
    @Order(3)
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
        User dbUser1 = userDbStorage.getUser(1);
        User dbUser2 = userDbStorage.getUser(2);

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

        Film createdFilm1 = filmDbStorage.addNewFilm(film1);
        Film createdFilm2 = filmDbStorage.addNewFilm(film2);

        filmService.putLike(createdFilm1.getId(), createdUser1.getId());
        filmService.putLike(createdFilm1.getId(), createdUser2.getId());
        filmService.putLike(createdFilm1.getId(), dbUser1.getId());
        filmService.putLike(createdFilm1.getId(), dbUser2.getId());
        filmService.putLike(createdFilm2.getId(), createdUser1.getId());
        filmService.putLike(createdFilm2.getId(), dbUser1.getId());
        filmService.putLike(createdFilm2.getId(), dbUser2.getId());

        List<Film> popularFilms = filmService.showMostLikedFilms(10);

        assertEquals(6, popularFilms.size());

        assertEquals(5, popularFilms.getFirst().getId()); //5й фильм самый популярный
        assertEquals(4, popularFilms.getFirst().getRate()); // у него 4 лайка

        assertEquals(6, popularFilms.get(1).getId()); //потом 6й фильм
        assertEquals(3, popularFilms.get(1).getRate()); //у него 3 лайка
    }

    @Test
    @Order(4)
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
    @Order(5)
    void shouldThrowExceptionWhenPuttingLikeFromNonExistentUser() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmDbStorage.addNewFilm(film);

        assertThrows(NotFoundException.class, () -> filmService.putLike(createdFilm.getId(), 9999L));
    }

    @Test
    @Order(6)
    void shouldThrowExceptionWhenPuttingDuplicateLike() {
        User user = User.builder()
                .email("duplicate@mail.com")
                .login("duplicateuser")
                .name("Duplicate User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userDbStorage.addNewUser(user);

        Film film = Film.builder()
                .name("Duplicate Like Film")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmDbStorage.addNewFilm(film);

        // Первый лайк должен пройти успешно
        assertDoesNotThrow(() -> filmService.putLike(createdFilm.getId(), createdUser.getId()));

        // Второй лайк должен бросить исключение
        assertThrows(ValidationException.class, () -> filmService.putLike(createdFilm.getId(), createdUser.getId()));
    }

    @Test
    @Order(7)
    void shouldThrowExceptionWhenDeletingNonExistentLike() {
        User user = User.builder()
                .email("nodelete@mail.com")
                .login("nodeleteuser")
                .name("No Delete User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userDbStorage.addNewUser(user);

        Film film = Film.builder()
                .name("No Delete Film")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmDbStorage.addNewFilm(film);

        assertThrows(ValidationException.class, () -> filmService.deleteLike(createdFilm.getId(), createdUser.getId()));
    }

    @Test
    @Order(8)
    void shouldRemoveDuplicateGenresWhenCreatingFilm() {
        Film film = Film.builder()
                .name("New film")
                .releaseDate(LocalDate.of(1999, 4, 30))
                .description("New film about friends")
                .duration(120)
                .mpa(Mpa.builder().id(3).build())
                .genres(List.of(
                        Genre.builder().id(1).build(),
                        Genre.builder().id(2).build(),
                        Genre.builder().id(1).build()  // Дубликат жанра
                ))
                .build();

        Film createdFilm = filmDbStorage.addNewFilm(film);

        // Проверяем, что фильм создан
        assertNotNull(createdFilm);
        assertEquals("New film", createdFilm.getName());
        assertEquals("New film about friends", createdFilm.getDescription());
        assertEquals(LocalDate.of(1999, 4, 30), createdFilm.getReleaseDate());
        assertEquals(120, createdFilm.getDuration());

        // Проверяем MPA
        assertNotNull(createdFilm.getMpa());
        assertEquals(3, createdFilm.getMpa().getId());
        assertEquals("PG-13", createdFilm.getMpa().getName());

        // Проверяем жанры - должно быть только 2 уникальных жанра, а не 3
        assertNotNull(createdFilm.getGenres());
        assertEquals(2, createdFilm.getGenres().size(), "Должно быть 2 уникальных жанра");

        // Проверяем, что жанры отсортированы по id и содержат правильные данные
        assertEquals(1, createdFilm.getGenres().get(0).getId());
        assertEquals("Комедия", createdFilm.getGenres().get(0).getName());
        assertEquals(2, createdFilm.getGenres().get(1).getId());
        assertEquals("Драма", createdFilm.getGenres().get(1).getName());
    }

    @Test
    @Order(9)
    void shouldRemoveDuplicateGenresWhenUpdatingFilm() {
        // Сначала создаем фильм без дубликатов
        Film film = Film.builder()
                .name("Test Film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .description("Test Description")
                .duration(100)
                .mpa(Mpa.builder().id(1).build())
                .genres(List.of(
                        Genre.builder().id(1).build()
                ))
                .build();

        Film createdFilm = filmDbStorage.addNewFilm(film);

        // Обновляем фильм с дублирующимися жанрами
        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .description("Updated Description")
                .duration(110)
                .mpa(Mpa.builder().id(2).build())
                .genres(List.of(
                        Genre.builder().id(1).build(),
                        Genre.builder().id(2).build(),
                        Genre.builder().id(1).build(),  // Дубликат
                        Genre.builder().id(3).build(),
                        Genre.builder().id(2).build()   // Дубликат
                ))
                .build();

        Film resultFilm = filmDbStorage.updateFilm(updatedFilm);

        // Проверяем, что осталось только 3 уникальных жанра вместо 5
        assertNotNull(resultFilm.getGenres());
        assertEquals(3, resultFilm.getGenres().size(), "Должно быть 3 уникальных жанра");

        // Проверяем, что жанры отсортированы и содержат правильные id
        assertEquals(1, resultFilm.getGenres().get(0).getId());
        assertEquals(2, resultFilm.getGenres().get(1).getId());
        assertEquals(3, resultFilm.getGenres().get(2).getId());
    }

    @Test
    @Order(10)
    void shouldHandleNullAndEmptyGenres() {
        // Тест с null жанрами
        Film filmWithNullGenres = Film.builder()
                .name("Null Genres Film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .description("Description")
                .duration(100)
                .mpa(Mpa.builder().id(1).build())
                .genres(null)
                .build();

        Film createdFilm1 = filmDbStorage.addNewFilm(filmWithNullGenres);
        assertNotNull(createdFilm1.getGenres());
        assertTrue(createdFilm1.getGenres().isEmpty());

        // Тест с пустым списком жанров
        Film filmWithEmptyGenres = Film.builder()
                .name("Empty Genres Film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .description("Description")
                .duration(100)
                .mpa(Mpa.builder().id(1).build())
                .genres(List.of())
                .build();

        Film createdFilm2 = filmDbStorage.addNewFilm(filmWithEmptyGenres);
        assertNotNull(createdFilm2.getGenres());
        assertTrue(createdFilm2.getGenres().isEmpty());
    }
}