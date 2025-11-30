package ru.yandex.practicum.filmorate.controllerTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreControllerTest {

    private final GenreController genreController;

    @Test
    void shouldGetAllGenres() {
        List<Genre> genres = genreController.getAllGenres();

        assertNotNull(genres);
        assertEquals(6, genres.size()); // Комедия, Драма, Мультфильм, Триллер, Документальный, Боевик

        // Check all genres exist
        assertTrue(genres.stream().anyMatch(genre -> genre.getName().equals("Комедия")));
        assertTrue(genres.stream().anyMatch(genre -> genre.getName().equals("Драма")));
        assertTrue(genres.stream().anyMatch(genre -> genre.getName().equals("Мультфильм")));
        assertTrue(genres.stream().anyMatch(genre -> genre.getName().equals("Триллер")));
        assertTrue(genres.stream().anyMatch(genre -> genre.getName().equals("Документальный")));
        assertTrue(genres.stream().anyMatch(genre -> genre.getName().equals("Боевик")));
    }

    @Test
    void shouldGetGenreById() {
        Genre genre = genreController.getGenreById(1);

        assertNotNull(genre);
        assertEquals(1, genre.getId());
        assertEquals("Комедия", genre.getName());
    }

    @Test
    void shouldGetAllGenresInOrder() {
        List<Genre> genres = genreController.getAllGenres();

        // Check order by ID
        for (int i = 0; i < genres.size() - 1; i++) {
            assertTrue(genres.get(i).getId() < genres.get(i + 1).getId());
        }
    }

    @Test
    void shouldThrowExceptionWhenGenreNotFound() {
        assertThrows(NotFoundException.class, () -> genreController.getGenreById(999));
    }

    @Test
    void shouldReturnCorrectGenreNames() {
        Genre genre1 = genreController.getGenreById(1);
        Genre genre2 = genreController.getGenreById(2);
        Genre genre3 = genreController.getGenreById(3);

        assertEquals("Комедия", genre1.getName());
        assertEquals("Драма", genre2.getName());
        assertEquals("Мультфильм", genre3.getName());
    }
}