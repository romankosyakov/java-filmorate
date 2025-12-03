package ru.yandex.practicum.filmorate.modelTest;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Mpa;

import static org.junit.jupiter.api.Assertions.*;

class MpaTest {

    @Test
    void shouldCreateMpaWithAllFields() {
        Mpa mpa = Mpa.builder()
                .id(1)
                .name("G")
                .description("General Audiences")
                .build();

        assertEquals(1, mpa.getId());
        assertEquals("G", mpa.getName());
        assertEquals("General Audiences", mpa.getDescription());
    }

    @Test
    void shouldCreateMpaWithNoArgsConstructor() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("PG");
        mpa.setDescription("Parental Guidance Suggested");

        assertEquals(1, mpa.getId());
        assertEquals("PG", mpa.getName());
        assertEquals("Parental Guidance Suggested", mpa.getDescription());
    }

    @Test
    void shouldBeEqualWithSameId() {
        Mpa mpa1 = Mpa.builder().id(1).name("G").description("Desc").build();
        Mpa mpa2 = Mpa.builder().id(1).name("G").description("Desc").build();

        assertEquals(mpa1, mpa2);
        assertEquals(mpa1.hashCode(), mpa2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentIds() {
        Mpa mpa1 = Mpa.builder().id(1).name("G").build();
        Mpa mpa2 = Mpa.builder().id(2).name("PG").build();

        assertNotEquals(mpa1, mpa2);
    }

    @Test
    void shouldReturnCorrectToString() {
        Mpa mpa = Mpa.builder().id(1).name("G").description("General").build();
        String toString = mpa.toString();

        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name=G"));
        assertTrue(toString.contains("description=General"));
    }
}