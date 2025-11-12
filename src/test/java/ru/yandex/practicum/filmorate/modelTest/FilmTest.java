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
        assertTrue(violations.isEmpty(), "Валидный фильм для создания не должен иметь нарушений валидации");
    }

    @Test
    void shouldCreateValidFilmForUpdate() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm, UpdateValidation.class);
        assertTrue(violations.isEmpty(), "Валидный фильм для обновления не должен иметь нарушений валидации");
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
        assertFalse(violations.isEmpty(), "Фильм с пустым названием должен быть невалидным для создания");
    }

    @Test
    void shouldNotFailWhenNameIsBlankForUpdate() {
        Film film = Film.builder()
                .id(1)
                .name("") // Для обновления пустое название не проверяется @NotBlank
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);
        // Проверяем только что нет ошибок связанных с именем из-за @NotBlank
        boolean hasNameBlankViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                        v.getMessage().contains("не может быть пустым"));
        assertFalse(hasNameBlankViolation, "Для обновления пустое название не должно проверяться на @NotBlank");
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
        assertFalse(violations.isEmpty(), "Фильм с null названием должен быть невалидным для создания");
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
        assertFalse(violations.isEmpty(), "Фильм с названием длиннее 100 символов должен быть невалидным");
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
        assertFalse(violations.isEmpty(), "Фильм с названием длиннее 100 символов должен быть невалидным для обновления");
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
        assertFalse(violations.isEmpty(), "Фильм с описанием длиннее 200 символов должен быть невалидным");
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
        assertTrue(violations.isEmpty(), "Фильм с описанием длиной 200 символов должен быть валидным");
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
        assertFalse(violations.isEmpty(), "Фильм с датой релиза раньше 28.12.1895 должен быть невалидным");
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
        assertFalse(violations.isEmpty(), "Фильм с датой релиза раньше 28.12.1895 должен быть невалидным для обновления");
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
        assertTrue(violations.isEmpty(), "Фильм с датой релиза 28.12.1895 должен быть валидным");
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
        assertFalse(violations.isEmpty(), "Фильм с null датой релиза должен быть невалидным для создания");
    }

    @Test
    void shouldNotFailWhenReleaseDateIsNullForUpdate() {
        Film film = Film.builder()
                .id(1)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(null) // Для обновления может быть null
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);
        // Проверяем только что нет ошибок связанных с @NotNull
        boolean hasNotNullViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("releaseDate") &&
                        v.getMessage().contains("обязательна"));
        assertFalse(hasNotNullViolation, "Для обновления null дата релиза не должна проверяться на @NotNull");
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
        assertFalse(violations.isEmpty(), "Фильм с продолжительностью 0 должен быть невалидным");
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
        assertFalse(violations.isEmpty(), "Фильм с отрицательной продолжительностью должен быть невалидным");
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
        assertTrue(violations.isEmpty(), "Фильм с продолжительностью 1 должен быть валидным");
    }

    @Test
    void shouldFailWhenIdIsNullForUpdate() {
        Film film = Film.builder()
                .id(null) // Для обновления id обязателен
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);
        assertFalse(violations.isEmpty(), "Фильм с null ID должен быть невалидным для обновления");
    }
}