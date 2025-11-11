package ru.yandex.practicum.filmorate.controllerTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

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

    // Тесты для Film

    @Test
    void shouldFailValidationWhenFilmNameIsBlank() {
        Film film = Film.builder()
                .name("") // пустое название - должно не пройти валидацию
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Фильм с пустым названием должен быть невалидным");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void shouldFailValidationWhenFilmNameIsNull() {
        Film film = Film.builder()
                .name(null) // null название - должно не пройти валидацию
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Фильм с null названием должен быть невалидным");
    }

    @Test
    void shouldFailValidationWhenFilmDescriptionIsTooLong() {
        String longDescription = "A".repeat(201); // 201 символ - больше лимита
        Film film = Film.builder()
                .name("Valid Film")
                .description(longDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Фильм с описанием длиннее 200 символов должен быть невалидным");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    void shouldPassValidationWhenFilmDescriptionIsExactly200Characters() {
        String maxLengthDescription = "A".repeat(200); // граничное значение
        Film film = Film.builder()
                .name("Valid Film")
                .description(maxLengthDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Фильм с описанием длиной 200 символов должен быть валидным");
    }

    @Test
    void shouldFailValidationWhenFilmReleaseDateIsBeforeMinDate() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 27)) // день до минимальной даты
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Фильм с датой релиза раньше 28.12.1895 должен быть невалидным");
    }

    @Test
    void shouldPassValidationWhenFilmReleaseDateIsExactlyMinDate() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 28)) // граничное значение
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Фильм с датой релиза 28.12.1895 должен быть валидным");
    }

    @Test
    void shouldFailValidationWhenFilmDurationIsZero() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0) // граничное значение - должно не пройти
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Фильм с продолжительностью 0 должен быть невалидным");
    }

    @Test
    void shouldFailValidationWhenFilmDurationIsNegative() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-1) // отрицательное значение
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Фильм с отрицательной продолжительностью должен быть невалидным");
    }

    @Test
    void shouldPassValidationWhenFilmDurationIsOne() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(1) // минимальное положительное значение
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "Фильм с продолжительностью 1 должен быть валидным");
    }

    // Тесты для User

    @Test
    void shouldFailValidationWhenUserEmailIsBlank() {
        User user = User.builder()
                .email("") // пустой email
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Пользователь с пустым email должен быть невалидным");
    }

    @Test
    void shouldFailValidationWhenUserEmailHasNoAtSymbol() {
        User user = User.builder()
                .email("invalid-email.com") // email без @
                .login("validlogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Пользователь с email без @ должен быть невалидным");
    }

    @Test
    void shouldFailValidationWhenUserLoginIsBlank() {
        User user = User.builder()
                .email("valid@email.com")
                .login("") // пустой логин
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Пользователь с пустым логином должен быть невалидным");
    }

    @Test
    void shouldFailValidationWhenUserLoginContainsSpaces() {
        User user = User.builder()
                .email("valid@email.com")
                .login("login with spaces") // логин с пробелами
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Пользователь с логином содержащим пробелы должен быть невалидным");
    }

    @Test
    void shouldFailValidationWhenUserLoginIsTooShort() {
        User user = User.builder()
                .email("valid@email.com")
                .login("abc") // 3 символа - меньше минимума
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Пользователь с логином короче 4 символов должен быть невалидным");
    }

    @Test
    void shouldPassValidationWhenUserLoginIsExactlyMinLength() {
        User user = User.builder()
                .email("valid@email.com")
                .login("abcd") // 4 символа - граничное значение
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Пользователь с логином длиной 4 символа должен быть валидным");
    }

    @Test
    void shouldFailValidationWhenUserLoginIsTooLong() {
        String longLogin = "a".repeat(21); // 21 символ - больше максимума
        User user = User.builder()
                .email("valid@email.com")
                .login(longLogin)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Пользователь с логином длиннее 20 символов должен быть невалидным");
    }

    @Test
    void shouldPassValidationWhenUserLoginIsExactlyMaxLength() {
        String maxLengthLogin = "a".repeat(20); // 20 символов - граничное значение
        User user = User.builder()
                .email("valid@email.com")
                .login(maxLengthLogin)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Пользователь с логином длиной 20 символов должен быть валидным");
    }

    @Test
    void shouldFailValidationWhenUserBirthdayIsInFuture() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .birthday(LocalDate.now().plusDays(1)) // дата в будущем
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Пользователь с датой рождения в будущем должен быть невалидным");
    }

    @Test
    void shouldPassValidationWhenUserBirthdayIsToday() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .birthday(LocalDate.now()) // граничное значение
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Пользователь с сегодняшней датой рождения должен быть валидным");
    }

    @Test
    void shouldPassValidationWhenUserNameIsNull() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .name(null) // имя может быть null
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Пользователь с null именем должен быть валидным");
    }

    @Test
    void shouldPassValidationWhenUserNameIsBlank() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .name("") // имя может быть пустым
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Пользователь с пустым именем должен быть валидным");
    }
}