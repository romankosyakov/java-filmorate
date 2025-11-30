package ru.yandex.practicum.filmorate.storageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreDbStorage;

    @Test
    void shouldGetAllGenres() {
        List<Genre> genres = genreDbStorage.getAllGenres();

        assertNotNull(genres);
        assertFalse(genres.isEmpty());
        assertEquals(6, genres.size()); // According to data.sql

        // Check first genre
        Genre firstGenre = genres.getFirst();
        assertNotNull(firstGenre.getId());
        assertNotNull(firstGenre.getName());
    }

    @Test
    void shouldGetGenreById() {
        Genre genre = genreDbStorage.getGenreById(1);

        assertNotNull(genre);
        assertEquals(1, genre.getId());
        assertEquals("Комедия", genre.getName());
    }

    @Test
    void shouldGetAllGenresInOrder() {
        List<Genre> genres = genreDbStorage.getAllGenres();

        // Check order by ID
        for (int i = 0; i < genres.size() - 1; i++) {
            assertTrue(genres.get(i).getId() < genres.get(i + 1).getId());
        }
    }

    @Test
    void shouldThrowExceptionWhenGenreNotFound() {
        assertThrows(NotFoundException.class, () -> genreDbStorage.getGenreById(999));
    }

    @Test
    void shouldReturnCorrectGenreNames() {
        Genre genre1 = genreDbStorage.getGenreById(1);
        Genre genre2 = genreDbStorage.getGenreById(2);
        Genre genre3 = genreDbStorage.getGenreById(3);

        assertEquals("Комедия", genre1.getName());
        assertEquals("Драма", genre2.getName());
        assertEquals("Мультфильм", genre3.getName());
    }
}