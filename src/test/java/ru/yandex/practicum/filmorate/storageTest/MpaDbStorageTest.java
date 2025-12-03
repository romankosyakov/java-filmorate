package ru.yandex.practicum.filmorate.storageTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaDbStorage;

    @Test
    void shouldGetAllMpa() {
        List<Mpa> mpaList = mpaDbStorage.getAllMpa();

        assertNotNull(mpaList);
        assertFalse(mpaList.isEmpty());
        assertEquals(5, mpaList.size()); // G, PG, PG-13, R, NC-17

        // Check order by ID
        for (int i = 0; i < mpaList.size() - 1; i++) {
            assertTrue(mpaList.get(i).getId() < mpaList.get(i + 1).getId());
        }
    }

    @Test
    void shouldGetMpaById() {
        Mpa mpa = mpaDbStorage.getMpaById(1);

        assertNotNull(mpa);
        assertEquals(1, mpa.getId());
        assertEquals("G", mpa.getName());
        assertNotNull(mpa.getDescription());
    }

    @Test
    void shouldGetAllMpaRatings() {
        List<Mpa> mpaList = mpaDbStorage.getAllMpa();

        // Check all MPA ratings exist
        assertThat(mpaList).extracting(Mpa::getName)
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void shouldThrowExceptionWhenMpaNotFound() {
        assertThrows(NotFoundException.class, () -> mpaDbStorage.getMpaById(999));
    }

    @Test
    void shouldReturnCorrectMpaData() {
        Mpa mpa1 = mpaDbStorage.getMpaById(1);
        Mpa mpa2 = mpaDbStorage.getMpaById(2);
        Mpa mpa3 = mpaDbStorage.getMpaById(3);

        assertEquals("G", mpa1.getName());
        assertEquals("PG", mpa2.getName());
        assertEquals("PG-13", mpa3.getName());
    }

    @Test
    void shouldHaveDescriptions() {
        List<Mpa> mpaList = mpaDbStorage.getAllMpa();

        for (Mpa mpa : mpaList) {
            assertNotNull(mpa.getDescription());
            assertFalse(mpa.getDescription().isEmpty());
        }
    }
}