package ru.yandex.practicum.filmorate.storageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class,
        MpaDbStorage.class,
        GenreDbStorage.class,
        LikeDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final MpaDbStorage mpaDbStorage;

    @BeforeEach
    void setUp() {
        filmStorage.getAllFilms().forEach(film -> {
        });
        initTestData();
    }

    private void initTestData() {
        mpaDbStorage.getMpaById(1);
        mpaDbStorage.getMpaById(2);
    }

    @Test
    @Order(1)
    void shouldAddNewFilm() {

        Mpa mpa = mpaDbStorage.getMpaById(1);

        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();

        Film createdFilm = filmStorage.addNewFilm(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
        assertEquals("Test Description", createdFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
        assertEquals(120, createdFilm.getDuration());
        assertNotNull(createdFilm.getMpa());
        assertEquals(1, createdFilm.getMpa().getId());
        assertEquals("G", createdFilm.getMpa().getName()); // Проверяем имя рейтинга
    }

    @Test
    @Order(2)
    void shouldGetFilmById() {
        Mpa mpa = mpaDbStorage.getMpaById(1);

        Film film = Film.builder()
                .name("Film For Get")
                .description("Description for get")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();

        Film createdFilm = filmStorage.addNewFilm(film);
        Film foundFilm = filmStorage.getFilm(createdFilm.getId());

        assertNotNull(foundFilm);
        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Film For Get", foundFilm.getName());
        assertEquals(mpa.getId(), foundFilm.getMpa().getId());
        assertEquals(mpa.getName(), foundFilm.getMpa().getName());
    }

    @Test
    @Order(3)
    void shouldThrowExceptionWhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.getFilm(9999));
    }

    @Test
    @Order(4)
    void shouldGetAllFilms() {
        Mpa mpa1 = mpaDbStorage.getMpaById(1);
        Mpa mpa2 = mpaDbStorage.getMpaById(2);

        int initialSize = filmStorage.getAllFilms().size();

        Film film1 = Film.builder()
                .name("Film One")
                .description("Description one")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa1)
                .build();

        Film film2 = Film.builder()
                .name("Film Two")
                .description("Description two")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .mpa(mpa2)
                .build();

        filmStorage.addNewFilm(film1);
        filmStorage.addNewFilm(film2);

        List<Film> films = filmStorage.getAllFilms();
        assertEquals(initialSize + 2, films.size());

        // Проверяем, что фильмы добавлены
        boolean foundFilm1 = films.stream()
                .anyMatch(f -> f.getName().equals("Film One"));
        boolean foundFilm2 = films.stream()
                .anyMatch(f -> f.getName().equals("Film Two"));

        assertTrue(foundFilm1);
        assertTrue(foundFilm2);
    }

    @Test
    @Order(5)
    void shouldUpdateFilm() {
        Mpa mpa1 = mpaDbStorage.getMpaById(1);
        Mpa mpa2 = mpaDbStorage.getMpaById(2);

        Film film = Film.builder()
                .name("Original Film")
                .description("Original description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa1)
                .build();

        Film createdFilm = filmStorage.addNewFilm(film);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .mpa(mpa2)
                .build();

        Film result = filmStorage.updateFilm(updatedFilm);

        assertEquals(createdFilm.getId(), result.getId());
        assertEquals("Updated Film", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2001, 1, 1), result.getReleaseDate());
        assertEquals(150, result.getDuration());
        assertEquals(2, result.getMpa().getId());
        assertEquals("PG", result.getMpa().getName());
    }

    @Test
    @Order(6)
    void shouldAddFilmWithGenres() {
        Mpa mpa = mpaDbStorage.getMpaById(1);

        Film film = Film.builder()
                .name("Film With Genres")
                .description("Film with multiple genres")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(List.of(
                        Genre.builder().id(1).build(),
                        Genre.builder().id(2).build()
                ))
                .build();

        Film createdFilm = filmStorage.addNewFilm(film);

        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getGenres());
        assertEquals(2, createdFilm.getGenres().size());

        // Проверяем, что жанры отсортированы
        assertEquals(1, createdFilm.getGenres().get(0).getId());
        assertEquals(2, createdFilm.getGenres().get(1).getId());
    }

    @Test
    @Order(7)
    void shouldUpdateFilmGenres() {
        Mpa mpa = mpaDbStorage.getMpaById(1);

        Film film = Film.builder()
                .name("Film For Genre Update")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(List.of(Genre.builder().id(1).build()))
                .build();

        Film createdFilm = filmStorage.addNewFilm(film);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Film For Genre Update")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .genres(List.of(
                        Genre.builder().id(2).build(),
                        Genre.builder().id(3).build()
                ))
                .build();

        Film result = filmStorage.updateFilm(updatedFilm);

        assertEquals(2, result.getGenres().size());
        assertEquals(2, result.getGenres().get(0).getId());
        assertEquals(3, result.getGenres().get(1).getId());
    }

    @Test
    @Order(8)
    void shouldGetMostLikedFilms() {
        Mpa mpa = mpaDbStorage.getMpaById(1);

        Film film1 = Film.builder()
                .name("Popular Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .build();

        Film film2 = Film.builder()
                .name("Popular Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(130)
                .mpa(mpa)
                .build();

        filmStorage.addNewFilm(film1);
        filmStorage.addNewFilm(film2);

        // Получаем 10 самых популярных фильмов
        List<Film> popularFilms = filmStorage.getMostLikedFilms(10);

        assertNotNull(popularFilms);

    }
}