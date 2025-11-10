package ru.yandex.practicum.filmorate.modelTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private Validator validator;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validFilm = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
    }

    @Test
    void shouldCreateValidFilm() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Валидный фильм не должен иметь нарушений валидации");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = Film.builder()
                .id(1)
                .name("")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Фильм с пустым названием должен быть невалидным");
    }

    @Test
    void shouldFailWhenNameIsNull() {
        Film film = Film.builder()
                .id(1)
                .name(null)
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Фильм с null названием должен быть невалидным");
    }

    @Test
    void shouldFailWhenNameExceedsMaxLength() {
        String longName = "A".repeat(101);
        Film film = Film.builder()
                .id(1)
                .name(longName)
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Фильм с названием длиннее 100 символов должен быть невалидным");
    }

    @Test
    void shouldAllowMaxLengthName() {
        String maxLengthName = "A".repeat(100);
        Film film = Film.builder()
                .id(1)
                .name(maxLengthName)
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Фильм с названием длиной 100 символов должен быть валидным");
    }

    @Test
    void shouldFailWhenDescriptionExceedsMaxLength() {
        String longDescription = "A".repeat(201);
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description(longDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Фильм с описанием длиннее 200 символов должен быть невалидным");
    }

    @Test
    void shouldAllowMaxLengthDescription() {
        String maxLengthDescription = "A".repeat(200);
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description(maxLengthDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Фильм с описанием длиной 200 символов должен быть валидным");
    }

    @Test
    void shouldAllowEmptyDescription() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Фильм с пустым описанием должен быть валидным");
    }

    @Test
    void shouldAllowNullDescription() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description(null)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Фильм с null описанием должен быть валидным");
    }

    @Test
    void shouldFailWhenReleaseDateIsBeforeMinDate() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Фильм с датой релиза раньше 28.12.1895 должен быть невалидным");
    }

    @Test
    void shouldAllowMinReleaseDate() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Фильм с датой релиза 28.12.1895 должен быть валидным");
    }

    @Test
    void shouldFailWhenReleaseDateIsNull() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(null)
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Фильм с null датой релиза должен быть невалидным");
    }

    @Test
    void shouldFailWhenDurationIsZero() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Фильм с продолжительностью 0 должен быть невалидным");
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-1)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Фильм с отрицательной продолжительностью должен быть невалидным");
    }

    @Test
    void shouldAllowMinPositiveDuration() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(1)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Фильм с продолжительностью 1 должен быть валидным");
    }
}