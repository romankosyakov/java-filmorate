package ru.yandex.practicum.filmorate.modelTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private Validator validator;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        validFilm = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
    }

    @Test
    void shouldCreateValidFilmForCreation() {
        Film filmForCreate = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(filmForCreate, CreateValidation.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldCreateValidFilmForUpdate() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm, UpdateValidation.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameIsBlankForCreation() {
        Film film = Film.builder()
                .name("")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldNotFailWhenNameIsBlankForUpdate() {
        Film film = Film.builder()
                .id(1)
                .name("")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);
        boolean hasNameBlankViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                        v.getMessage().contains("не может быть пустым"));
        assertFalse(hasNameBlankViolation);
    }

    @Test
    void shouldFailWhenNameIsNullForCreation() {
        Film film = Film.builder()
                .name(null)
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameExceedsMaxLengthForCreation() {
        String longName = "A".repeat(101);
        Film film = Film.builder()
                .name(longName)
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameExceedsMaxLengthForUpdate() {
        String longName = "A".repeat(101);
        Film film = Film.builder()
                .id(1)
                .name(longName)
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenDescriptionExceedsMaxLengthForCreation() {
        String longDescription = "A".repeat(201);
        Film film = Film.builder()
                .name("Valid Film")
                .description(longDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldAllowMaxLengthDescriptionForCreation() {
        String maxLengthDescription = "A".repeat(200);
        Film film = Film.builder()
                .name("Valid Film")
                .description(maxLengthDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenReleaseDateIsBeforeMinDateForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenReleaseDateIsBeforeMinDateForUpdate() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldAllowMinReleaseDateForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenReleaseDateIsNullForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(null)
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldNotFailWhenReleaseDateIsNullForUpdate() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(null)
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);
        boolean hasNotNullViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate") &&
                        v.getMessage().contains("обязательна"));
        assertFalse(hasNotNullViolation);
    }

    @Test
    void shouldFailWhenDurationIsZeroForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenDurationIsNegativeForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-1)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldAllowMinPositiveDurationForCreation() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(1)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenIdIsNullForUpdate() {
        Film film = Film.builder()
                .id(null)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldInitializeFilmLikesAsEmptySet() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        assertNotNull(film.getFilmLikes());
        assertTrue(film.getFilmLikes().isEmpty());
    }
}