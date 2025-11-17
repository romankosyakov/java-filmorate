package ru.yandex.practicum.filmorate.controllerTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.CreateValidation;
import ru.yandex.practicum.filmorate.validation.UpdateValidation;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ControllerValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldFailValidationWhenFilmNameIsBlankForCreation() {
        Film film = Film.builder()
                .name("")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void shouldFailValidationWhenFilmNameIsNullForCreation() {
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
    void shouldFailValidationWhenFilmDescriptionIsTooLongForCreation() {
        String longDescription = "A".repeat(201);
        Film film = Film.builder()
                .name("Valid Film")
                .description(longDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, CreateValidation.class);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void shouldPassValidationWhenFilmDescriptionIsExactly200CharactersForCreation() {
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
    void shouldFailValidationWhenFilmReleaseDateIsBeforeMinDateForCreation() {
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
    void shouldPassValidationWhenFilmReleaseDateIsExactlyMinDateForCreation() {
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
    void shouldFailValidationWhenFilmDurationIsZeroForCreation() {
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
    void shouldFailValidationWhenFilmDurationIsNegativeForCreation() {
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
    void shouldPassValidationWhenFilmDurationIsOneForCreation() {
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
    void shouldFailValidationWhenFilmIdIsNullForUpdate() {
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
    void shouldPassValidationWhenFilmNameIsBlankForUpdate() {
        Film film = Film.builder()
                .id(1)
                .name("")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film, UpdateValidation.class);

        boolean hasNotBlankViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("не может быть пустым"));
        assertFalse(hasNotBlankViolation);
    }

    @Test
    void shouldFailValidationWhenUserEmailIsBlankForCreation() {
        User user = User.builder()
                .email("")
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenUserEmailHasNoAtSymbolForCreation() {
        User user = User.builder()
                .email("invalid-email.com")
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenUserLoginIsBlankForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenUserLoginContainsSpacesForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("login with spaces")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenUserLoginIsTooShortForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("abc")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenUserLoginIsExactlyMinLengthForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("abcd")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenUserLoginIsTooLongForCreation() {
        String longLogin = "a".repeat(21);
        User user = User.builder()
                .email("valid@email.com")
                .login(longLogin)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenUserLoginIsExactlyMaxLengthForCreation() {
        String maxLengthLogin = "a".repeat(20);
        User user = User.builder()
                .email("valid@email.com")
                .login(maxLengthLogin)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenUserBirthdayIsInFutureForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenUserBirthdayIsTodayForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .birthday(LocalDate.now())
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenUserNameIsNullForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .name(null)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenUserNameIsBlankForCreation() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .name("")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, CreateValidation.class);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenUserIdIsNullForUpdate() {
        User user = User.builder()
                .id(null)
                .email("valid@email.com")
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenUserEmailIsBlankForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("")
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);

        boolean hasNotBlankViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("не может быть пустым"));
        assertFalse(hasNotBlankViolation);
    }

    @Test
    void shouldPassValidationWhenUserLoginIsBlankForUpdate() {
        User user = User.builder()
                .id(1L)
                .email("valid@email.com")
                .login("")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user, UpdateValidation.class);

        boolean hasNotBlankViolation = violations.stream()
                .anyMatch(v -> v.getMessage().contains("не может быть пустым"));
        assertFalse(hasNotBlankViolation);
    }
}