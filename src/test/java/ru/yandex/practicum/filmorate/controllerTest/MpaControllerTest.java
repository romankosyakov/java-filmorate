package ru.yandex.practicum.filmorate.controllerTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.MpaController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaControllerTest {

    private final MpaController mpaController;

    @Test
    void shouldGetAllMpa() {
        List<Mpa> mpaList = mpaController.getAllMpa();

        assertNotNull(mpaList);
        assertEquals(5, mpaList.size()); // G, PG, PG-13, R, NC-17

        // Check all MPA ratings exist
        assertTrue(mpaList.stream().anyMatch(mpa -> mpa.getName().equals("G")));
        assertTrue(mpaList.stream().anyMatch(mpa -> mpa.getName().equals("PG")));
        assertTrue(mpaList.stream().anyMatch(mpa -> mpa.getName().equals("PG-13")));
        assertTrue(mpaList.stream().anyMatch(mpa -> mpa.getName().equals("R")));
        assertTrue(mpaList.stream().anyMatch(mpa -> mpa.getName().equals("NC-17")));
    }

    @Test
    void shouldGetMpaById() {
        Mpa mpa = mpaController.getMpaById(1);

        assertNotNull(mpa);
        assertEquals(1, mpa.getId());
        assertEquals("G", mpa.getName());
        assertNotNull(mpa.getDescription());
    }

    @Test
    void shouldGetAllMpaRatingsInOrder() {
        List<Mpa> mpaList = mpaController.getAllMpa();

        // Check order by ID
        for (int i = 0; i < mpaList.size() - 1; i++) {
            assertTrue(mpaList.get(i).getId() < mpaList.get(i + 1).getId());
        }
    }

    @Test
    void shouldThrowExceptionWhenMpaNotFound() {
        assertThrows(NotFoundException.class, () -> mpaController.getMpaById(999));
    }

    @Test
    void shouldReturnCorrectMpaData() {
        Mpa mpa1 = mpaController.getMpaById(1);
        Mpa mpa2 = mpaController.getMpaById(2);
        Mpa mpa3 = mpaController.getMpaById(3);

        assertEquals("G", mpa1.getName());
        assertEquals("PG", mpa2.getName());
        assertEquals("PG-13", mpa3.getName());
    }

    @Test
    void shouldHaveDescriptionsForAllMpa() {
        List<Mpa> mpaList = mpaController.getAllMpa();

        for (Mpa mpa : mpaList) {
            assertNotNull(mpa.getDescription());
            assertFalse(mpa.getDescription().isEmpty());
        }
    }
}