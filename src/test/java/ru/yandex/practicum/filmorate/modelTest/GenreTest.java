package ru.yandex.practicum.filmorate.modelTest;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Genre;

import static org.junit.jupiter.api.Assertions.*;

class GenreTest {

    @Test
    void shouldCreateGenreWithIdAndName() {
        Genre genre = Genre.builder()
                .id(1)
                .name("Комедия")
                .build();

        assertEquals(1, genre.getId());
        assertEquals("Комедия", genre.getName());
    }

    @Test
    void shouldCreateGenreWithNoArgsConstructor() {
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Драма");

        assertEquals(1, genre.getId());
        assertEquals("Драма", genre.getName());
    }

    @Test
    void shouldBeEqualWithSameId() {
        Genre genre1 = Genre.builder().id(1).name("Комедия").build();
        Genre genre2 = Genre.builder().id(1).name("Комедия").build();

        assertEquals(genre1, genre2);
        assertEquals(genre1.hashCode(), genre2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentIds() {
        Genre genre1 = Genre.builder().id(1).name("Комедия").build();
        Genre genre2 = Genre.builder().id(2).name("Драма").build();

        assertNotEquals(genre1, genre2);
    }

    @Test
    void shouldReturnCorrectToString() {
        Genre genre = Genre.builder().id(1).name("Комедия").build();
        String toString = genre.toString();

        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name=Комедия"));
    }
}