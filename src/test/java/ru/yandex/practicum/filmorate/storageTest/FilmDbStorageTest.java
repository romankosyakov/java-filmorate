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
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, GenreDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

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

        Film createdFilm = filmStorage.addNewFilm(film);

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
    void shouldGetFilmById() {
        Film film = Film.builder()
                .name("Film For Get")
                .description("Description for get")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmStorage.addNewFilm(film);
        Film foundFilm = filmStorage.getFilm(createdFilm.getId());

        assertNotNull(foundFilm);
        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Film For Get", foundFilm.getName());
    }

    @Test
    @Order(3)
    void shouldThrowExceptionWhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> filmStorage.getFilm(9999));
    }

    @Test
    @Order(4)
    void shouldGetAllFilms() {
        int initialSize = filmStorage.getAllFilms().size();

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

        filmStorage.addNewFilm(film1);
        filmStorage.addNewFilm(film2);

        List<Film> films = filmStorage.getAllFilms();
        assertTrue(films.size() >= initialSize + 2);
    }

    @Test
    @Order(5)
    void shouldUpdateFilm() {
        Film film = Film.builder()
                .name("Original Film")
                .description("Original description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film createdFilm = filmStorage.addNewFilm(film);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(150)
                .mpa(Mpa.builder().id(2).build())
                .build();

        Film result = filmStorage.updateFilm(updatedFilm);

        assertEquals(createdFilm.getId(), result.getId());
        assertEquals("Updated Film", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2001, 1, 1), result.getReleaseDate());
        assertEquals(150, result.getDuration());
        assertEquals(2, result.getMpa().getId());
    }

    @Test
    @Order(6)
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

        Film createdFilm = filmStorage.addNewFilm(film);

        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getGenres());
        assertEquals(2, createdFilm.getGenres().size());
    }

    @Test
    @Order(7)
    void shouldUpdateFilmGenres() {
        Film film = Film.builder()
                .name("Film For Genre Update")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .genres(List.of(Genre.builder().id(1).build()))
                .build();

        Film createdFilm = filmStorage.addNewFilm(film);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Film For Genre Update")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .genres(List.of(Genre.builder().id(2).build(), Genre.builder().id(3).build()))
                .build();

        Film result = filmStorage.updateFilm(updatedFilm);

        assertEquals(2, result.getGenres().size());
    }
}