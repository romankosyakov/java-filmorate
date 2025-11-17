package ru.yandex.practicum.filmorate.storageTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryFilmStorageTest {
    private InMemoryFilmStorage filmStorage;
    private Film film1;
    private Film film2;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();

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
    }

    @Test
    void shouldAddNewFilm() {
        Film createdFilm = filmStorage.addNewFilm(film1);

        assertNotNull(createdFilm.getId());
        assertEquals("Film One", createdFilm.getName());
        assertEquals("Description one", createdFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
        assertEquals(120, createdFilm.getDuration());
    }

    @Test
    void shouldGetFilmById() {
        Film createdFilm = filmStorage.addNewFilm(film1);
        Film foundFilm = filmStorage.getFilm(createdFilm.getId());

        assertNotNull(foundFilm);
        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals(createdFilm.getName(), foundFilm.getName());
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentFilm() {
        assertThrows(NotFoundException.class, () -> {
            filmStorage.getFilm(999);
        });
    }

    @Test
    void shouldThrowExceptionWhenGettingFilmWithZeroId() {
        assertThrows(ValidationException.class, () -> {
            filmStorage.getFilm(0);
        });
    }

    @Test
    void shouldThrowExceptionWhenGettingFilmWithNegativeId() {
        assertThrows(ValidationException.class, () -> {
            filmStorage.getFilm(-1);
        });
    }

    @Test
    void shouldGetAllFilms() {
        filmStorage.addNewFilm(film1);
        filmStorage.addNewFilm(film2);

        List<Film> films = filmStorage.getAllFilms();

        assertEquals(2, films.size());
    }

    @Test
    void shouldUpdateFilm() {
        Film createdFilm = filmStorage.addNewFilm(film1);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2002, 1, 1))
                .duration(150)
                .build();

        Film result = filmStorage.updateFilm(updatedFilm);

        assertEquals(createdFilm.getId(), result.getId());
        assertEquals("Updated Film", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(LocalDate.of(2002, 1, 1), result.getReleaseDate());
        assertEquals(150, result.getDuration());
    }

    @Test
    void shouldUpdateFilmPartially() {
        Film createdFilm = filmStorage.addNewFilm(film1);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film Only Name")
                .build();

        Film result = filmStorage.updateFilm(updatedFilm);

        assertEquals(createdFilm.getId(), result.getId());
        assertEquals("Updated Film Only Name", result.getName());
        assertEquals("Description one", result.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), result.getReleaseDate());
        assertEquals(120, result.getDuration());
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
            filmStorage.updateFilm(film);
        });
    }

    @Test
    void shouldDeleteFilm() {
        Film createdFilm = filmStorage.addNewFilm(film1);
        int initialCount = filmStorage.getAllFilms().size();

        filmStorage.deleteFilm(createdFilm.getId());

        int finalCount = filmStorage.getAllFilms().size();
        assertEquals(initialCount - 1, finalCount);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentFilm() {
        assertThrows(NotFoundException.class, () -> {
            filmStorage.deleteFilm(999);
        });
    }

    @Test
    void shouldThrowExceptionWhenDeletingFilmWithZeroId() {
        assertThrows(ValidationException.class, () -> {
            filmStorage.deleteFilm(0);
        });
    }

    @Test
    void shouldDeleteAllFilms() {
        filmStorage.addNewFilm(film1);
        filmStorage.addNewFilm(film2);

        filmStorage.deleteAllFilms();

        List<Film> films = filmStorage.getAllFilms();
        assertEquals(0, films.size());
    }

    @Test
    void shouldThrowExceptionWhenDeletingAllFilmsFromEmptyStorage() {
        assertThrows(NotFoundException.class, () -> {
            filmStorage.deleteAllFilms();
        });
    }

    @Test
    void shouldGenerateIncrementalIds() {
        Film createdFilm1 = filmStorage.addNewFilm(film1);
        Film createdFilm2 = filmStorage.addNewFilm(film2);

        assertEquals(1, createdFilm1.getId());
        assertEquals(2, createdFilm2.getId());
    }

    @Test
    void shouldMaintainFilmLikesAfterUpdate() {
        Film createdFilm = filmStorage.addNewFilm(film1);

        // Добавляем лайки через сервис или напрямую
        createdFilm.getFilmLikes().add(1L);
        createdFilm.getFilmLikes().add(2L);

        Film updatedFilm = Film.builder()
                .id(createdFilm.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2002, 1, 1))
                .duration(150)
                .build();

        Film result = filmStorage.updateFilm(updatedFilm);

        assertEquals(2, result.getFilmLikes().size());
        assertTrue(result.getFilmLikes().contains(1L));
        assertTrue(result.getFilmLikes().contains(2L));
    }
}