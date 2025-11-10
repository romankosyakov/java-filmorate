package ru.yandex.practicum.filmorate.controllerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
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

        assertNotNull(createdFilm.getId(), "Фильм должен получить ID");
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
        assertEquals("Film 1", films.get(0).getName());
        assertEquals("Film 2", films.get(1).getName());
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
        }, "Должно выбрасываться исключение при поиске несуществующего фильма");
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
        }, "Должно выбрасываться исключение при обновлении несуществующего фильма");
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

        assertEquals(initialCount, finalCount, "Количество фильмов не должно меняться при обновлении");
    }
}